package com.Jeka8833.packetVelocityGuesser.game.tntpvprun;

import com.Jeka8833.packetVelocityGuesser.ServerConstants;
import com.Jeka8833.packetVelocityGuesser.composer.Composer;
import com.Jeka8833.packetVelocityGuesser.composer.RawJump;
import com.Jeka8833.packetVelocityGuesser.composer.RawJumpFilter;
import com.Jeka8833.packetVelocityGuesser.guesser.FoundedSolution;
import com.Jeka8833.packetVelocityGuesser.guesser.Guesser;
import com.Jeka8833.packetVelocityGuesser.output.WolframMathematica;
import com.Jeka8833.packetVelocityGuesser.parser.CsvFileParser;
import com.Jeka8833.packetVelocityGuesser.parser.FilePackets;
import com.Jeka8833.packetVelocityGuesser.parser.ServerStorageFileParser;
import com.Jeka8833.packetVelocityGuesser.parser.filter.FileFilter;
import com.Jeka8833.packetVelocityGuesser.parser.filter.GameInfoFilter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TNTRunXZTest {
    private static final Path PATH = Path.of("D:\\TNTClientAnalytics\\jumpInfoV4\\");
    private static final ServerStorageFileParser SERVER_PARSER = new ServerStorageFileParser();
    private static final Guesser GUESSER = new Guesser(null, new TNTRunVerticalGuesser());


    private static final FileFilter PACKET_FILTER = FileFilter.create()
            .add(GameInfoFilter.create()
                    .mode()
                    .blockIfAbsent()
                    .require(ServerConstants.Hypixel.Mode.TNTRun, ServerConstants.Hypixel.Mode.PVPRun)
                    .build()

                    .serverBrand()
                    .blockIfAbsent()
                    .require(ServerConstants.HYPIXEL_SERVER)
                    .build()

                    .build())
            .build();

    private static RawJump[] readTestFiles() throws IOException, ExecutionException {
        Path[] files = CsvFileParser.getAllFilesInFolder(PATH);
        FilePackets[] packets = SERVER_PARSER.parseAllFiles(files, true);

        packets = PACKET_FILTER.filter(packets);

        RawJump[] jumps = Composer.toRawJump(packets);

        //jumps = RawJumpFilter.filterUncompletedJumps(jumps);
        jumps = RawJumpFilter.filterZeroRotation(jumps);
        //jumps = RawJumpFilter.filterFutureCamera(jumps);
        //jumps = RawJumpFilter.filterDuplicates(jumps);

        return jumps;
    }

    private static String printXZTable(Collection<FoundedSolution> foundedSolutions) {
        String table1 = WolframMathematica.toTable(foundedSolutions,
                v -> v.position().yaw().orElseThrow(),
                v -> {
                    double val = v.receiver().velZ().orElseThrow() / v.position().pitchCos();
                    if (Double.isFinite(val) && Math.abs(val) < 10_000) return val;

                    throw new RuntimeException("Not finite value");
                }, e -> {
                });
        String table2 = WolframMathematica.toTable(foundedSolutions,
                v -> v.position().yaw().orElseThrow() + 90,
                v -> {
                    double val = v.receiver().velX().orElseThrow() / v.position().pitchCos();
                    if (Double.isFinite(val) && Math.abs(val) < 10_000) return val;

                    throw new RuntimeException("Not finite value");
                }, e -> {
                });

        return table1.substring(0, table1.length() - 1) + "," + table2.substring(1);
    }

    @Test
    public void allJumpFit() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        FoundedSolution[] solutions = GUESSER.solveBestOrFirstVertical(jumps, 1, "Unknown");

        solutions = TNTRunVerticalGuesser.filterDuplicatesPositionAndResults(solutions);

        Map<String, List<FoundedSolution>> grouped = Arrays.stream(solutions)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(FoundedSolution::jumpName));

        grouped.forEach((s, foundedSolutions) -> {
            String table = printXZTable(foundedSolutions);

            System.out.println("Table for " + s + "(" + foundedSolutions.size() / 2 + "): " + table);
        });
    }
}
