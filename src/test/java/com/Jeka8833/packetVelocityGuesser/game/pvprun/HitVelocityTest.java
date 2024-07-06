package com.Jeka8833.packetVelocityGuesser.game.pvprun;

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

public class HitVelocityTest {

    private static final Path PATH = Path.of("D:\\User\\Download\\jumpBackup\\analytic\\jumpInfoV4");
    private static final ServerStorageFileParser SERVER_PARSER = new ServerStorageFileParser();

/*    private static final Path PATH = Path.of("C:\\Users\\Jeka8833\\AppData\\Roaming\\.minecraft\\TNTClients-records\\PacketRecorder\\01.06.2024 11.58.csv");
    private static final CsvFileParser SERVER_PARSER = new CsvFileParser();*/

    private static final Guesser GUESSER = new Guesser(null, new PVPRunVerticalGuesser());


    private static final FileFilter PACKET_FILTER = FileFilter.create()
            .add(GameInfoFilter.create()
                    .mode()
                    .blockIfAbsent()
                    .require(ServerConstants.Hypixel.Mode.PVPRun)
                    .build()

                    .serverBrand()
                    .blockIfAbsent()
                    .require(ServerConstants.HYPIXEL_SERVER)
                    .build()

                    .build())
            .build();

    private static RawJump[] readTestFiles() throws IOException, ExecutionException {
        Path[] files = CsvFileParser.getAllFilesInFolder(PATH);
        FilePackets[] packets = SERVER_PARSER.parseAllFiles(files, false);

        packets = PACKET_FILTER.filter(packets);

        RawJump[] jumps = Composer.toRawJump(packets);

        //jumps = RawJumpFilter.filterUncompletedJumps(jumps);
        jumps = RawJumpFilter.filterZeroRotation(jumps);
        //jumps = RawJumpFilter.filterFutureCamera(jumps);
        //jumps = RawJumpFilter.filterDuplicates(jumps);

        return jumps;
    }

    @Test
    public void splitAndPrintJumps() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        FoundedSolution[] solutions = GUESSER.solveBestOrFirstVertical(jumps, 1, "Unknown");

        solutions = TNTRunVerticalGuesser.filterDuplicatesPositionAndResults(solutions);

        Map<String, List<FoundedSolution>> grouped = Arrays.stream(solutions)
                .filter(Objects::nonNull)
                //.filter(v -> v.position().onGround().orElseThrow())
                .collect(Collectors.groupingBy(FoundedSolution::jumpName));

        grouped.forEach((s, foundedSolutions) -> {
            var table = new WolframMathematica()
                    .processAndAddArray(v -> new Object[]{
                            v.position().pitch().orElseThrow(),
                            v.receiver().velY().orElseThrow()
                    }, foundedSolutions);

            System.out.println("Table for " + s + "(" + table.getArraySize() + "): " + table);
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

        var table = new WolframMathematica()
                .processAndAddArray(v -> new Object[]{
                        v.velX().orElseThrow(),
                        v.velY().orElseThrow(),
                        v.velZ().orElseThrow()
                }, receivedJumps);

        System.out.println("Table for (" + table.getArraySize() + "): " + table);
    }
}
