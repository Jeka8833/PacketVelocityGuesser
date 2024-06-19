package com.Jeka8833.packetVelocityGuesser.game.bowspleef;

import com.Jeka8833.packetVelocityGuesser.ServerConstants;
import com.Jeka8833.packetVelocityGuesser.composer.Composer;
import com.Jeka8833.packetVelocityGuesser.composer.RawJump;
import com.Jeka8833.packetVelocityGuesser.composer.RawJumpFilter;
import com.Jeka8833.packetVelocityGuesser.game.tntpvprun.TNTRunVerticalGuesser;
import com.Jeka8833.packetVelocityGuesser.guesser.FoundedSolution;
import com.Jeka8833.packetVelocityGuesser.guesser.Guesser;
import com.Jeka8833.packetVelocityGuesser.output.WolframMathematica;
import com.Jeka8833.packetVelocityGuesser.parser.CsvFileParser;
import com.Jeka8833.packetVelocityGuesser.parser.FilePackets;
import com.Jeka8833.packetVelocityGuesser.parser.ServerStorageFileParser;
import com.Jeka8833.packetVelocityGuesser.parser.filter.FileFilter;
import com.Jeka8833.packetVelocityGuesser.parser.filter.GameInfoFilter;
import com.Jeka8833.packetVelocityGuesser.parser.packet.ReceivedJump;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class BowSpleefYTest {
    private static final Path PATH = Path.of("D:\\User\\Download\\jumpBackup\\analytic\\jumpInfoV4\\");
    private static final ServerStorageFileParser SERVER_PARSER = new ServerStorageFileParser();
    private static final Guesser GUESSER = new Guesser(null, new BowSpleefVerticalGuesser());


    private static final FileFilter PACKET_FILTER = FileFilter.create()
            .add(GameInfoFilter.create()
                    .mode()
                    .blockIfAbsent()
                    .require(ServerConstants.Hypixel.Mode.BowSpleef)
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

    @Test
    public void firstJump() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        FoundedSolution[] solutions = GUESSER.solveFirstVertical(jumps);

        solutions = TNTRunVerticalGuesser.filterDuplicatesPositionAndResults(solutions);

        var wolframMathematica = new WolframMathematica()
                .processAndAddArray(v -> new Object[]{
                        v.position().pitch().orElseThrow(),
                        v.receiver().velY().orElseThrow()
                }, solutions);

        System.out.println("Table(" + wolframMathematica.getArraySize() + "): " + wolframMathematica);
    }


    @Test
    public void splitAndPrintJumps() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        FoundedSolution[] solutions = GUESSER.solveBestOrFirstVertical(jumps, 1, "Unknown");

        solutions = TNTRunVerticalGuesser.filterDuplicatesPositionAndResults(solutions);

        Map<String, List<FoundedSolution>> grouped = Arrays.stream(solutions)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(FoundedSolution::jumpName));

        grouped.forEach((s, foundedSolutions) -> {
            var wolframMathematica = new WolframMathematica()
                    .processAndAddArray(v -> new Object[]{
                            v.position().pitch().orElseThrow(),
                            v.receiver().velY().orElseThrow()
                    }, foundedSolutions);

            System.out.println("Table for " + s + "(" + wolframMathematica.getArraySize() + "): " + wolframMathematica);
        });
    }

    @Test
    public void hitMove() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        Collection<ReceivedJump> receivedJumps = new HashSet<>();
        for (RawJump jump : jumps) {
            ReceivedJump[] received = jump.jumps();
            receivedJumps.addAll(Arrays.asList(received));
        }

        FoundedSolution[] solutions = GUESSER.solveBestOrFirstVertical(jumps, 1, "Unknown");

        solutions = TNTRunVerticalGuesser.filterDuplicatesPositionAndResults(solutions);

        Map<String, List<FoundedSolution>> grouped = Arrays.stream(solutions)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(FoundedSolution::jumpName));

        grouped.forEach((s, foundedSolutions) -> {
            for (FoundedSolution solution : foundedSolutions) {
                ReceivedJump received = solution.receiver();
                receivedJumps.remove(received);
            }
        });

        var wolframMathematica = new WolframMathematica()
                .processAndAddArray(v -> new Object[]{
                        v.velX().orElseThrow(),
                        v.velY().orElseThrow(),
                        v.velZ().orElseThrow()
                }, receivedJumps);

        System.out.println("Table for (" + wolframMathematica.getArraySize() + "): " + wolframMathematica);
    }
}
