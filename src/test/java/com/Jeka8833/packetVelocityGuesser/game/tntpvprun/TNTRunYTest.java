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
import com.Jeka8833.packetVelocityGuesser.parser.packet.CallJump;
import com.Jeka8833.packetVelocityGuesser.parser.packet.ReceivedJump;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TNTRunYTest {

    private static final Path PATH = Path.of("D:\\User\\Download\\jumpBackup\\analytic\\jumpInfoV4\\");
    private static final ServerStorageFileParser SERVER_PARSER = new ServerStorageFileParser();

/*    private static final Path PATH = Path.of("C:\\Users\\Jeka8833\\AppData\\Roaming\\.minecraft\\TNTClients-records\\PacketRecorder\\01.06.2024 11.58.csv");
    private static final CsvFileParser SERVER_PARSER = new CsvFileParser();*/

    private static final Guesser GUESSER = new Guesser(null, new TNTRunVerticalGuesser());


    private static final FileFilter PACKET_FILTER = FileFilter.create()
            .add(GameInfoFilter.create()
                    .mode()
                    .blockIfAbsent()
                    .require(ServerConstants.Hypixel.Mode.TNTRun/*, ServerConstants.Hypixel.Mode.PVPRun*/)
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
    public void firstJump() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        FoundedSolution[] solutions = GUESSER.solveFirstVertical(jumps);

        solutions = TNTRunVerticalGuesser.filterDuplicatesPositionAndResults(solutions);

        var table = new WolframMathematica()
                .processAndAddArray(v -> new Object[]{
                        v.position().pitch().orElseThrow(),
                        v.receiver().velY().orElseThrow()
                }, solutions);

        System.out.println("Table(" + table.getArraySize() + "): " + table);
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
    public void printYPingMove() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        List<Double> yMove = new ArrayList<>();
        for (RawJump jump : jumps) {
            if (jump.calls().length == 0 || jump.jumps().length == 0)
                continue;

            CallJump firstCall = jump.calls()[0];
            if (firstCall.posY().isEmpty()) continue;

            ReceivedJump firstReceived = jump.jumps()[0];
            if (firstReceived.posY().isEmpty()) continue;

            yMove.add(firstReceived.posY().orElseThrow() - firstCall.posY().orElseThrow());
        }

        var table = new WolframMathematica()
                .addArray(yMove.toArray());
        System.out.println("Table(" + table.getArraySize() + "): " + table);
    }
}
