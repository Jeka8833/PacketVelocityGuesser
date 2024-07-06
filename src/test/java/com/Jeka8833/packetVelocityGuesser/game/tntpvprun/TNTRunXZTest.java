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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TNTRunXZTest {
    private static final Path PATH = Path.of("D:\\User\\Download\\jumpBackup\\analytic\\jumpInfoV4\\");
    private static final ServerStorageFileParser SERVER_PARSER = new ServerStorageFileParser();

    //private static final Path PATH = Path.of("C:\\Users\\Jeka8833\\AppData\\Roaming\\.minecraft\\TNTClients-records\\PacketRecorder\\31.05.2024 21.31.csv");
    //private static final CsvFileParser SERVER_PARSER = new CsvFileParser();

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
        FilePackets[] packets = SERVER_PARSER.parseAllFiles(files, true);

        packets = PACKET_FILTER.filter(packets);

        RawJump[] jumps = Composer.toRawJump(packets);

        //jumps = RawJumpFilter.filterUncompletedJumps(jumps);
        jumps = RawJumpFilter.filterZeroRotation(jumps);
        //jumps = RawJumpFilter.filterFutureCamera(jumps);
        //jumps = RawJumpFilter.filterDuplicates(jumps);

        return jumps;
    }

    private static WolframMathematica printXZTable(Iterable<FoundedSolution> foundedSolutions) {
        return new WolframMathematica()
                .processAndAddArray(v -> {
                    double val = v.receiver().velZ().orElseThrow() / v.position().pitchCos();
                    if (Double.isFinite(val) && Math.abs(val) < 10_000) {
                        return new Object[]{v.position().yaw().orElseThrow(), val};
                    }

                    throw new RuntimeException("Not finite value");
                }, null, foundedSolutions)
                .processAndAddArray(v -> {
                    double val = v.receiver().velX().orElseThrow() / v.position().pitchCos();
                    if (Double.isFinite(val) && Math.abs(val) < 10_000) {
                        return new Object[]{v.position().yaw().orElseThrow() + 90D, val};
                    }

                    throw new RuntimeException("Not finite value");
                }, null, foundedSolutions);
    }

    @Test
    public void allJumpFit() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        FoundedSolution[] solutions = GUESSER.solveBestOrFirstVertical(jumps, 1, "Unknown");

        solutions = TNTRunVerticalGuesser.filterDuplicatesPositionAndResults(solutions);

        Map<String, List<FoundedSolution>> grouped = Arrays.stream(solutions)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(FoundedSolution::jumpName));

        StringBuffer stringBuffer = new StringBuffer();

        grouped.forEach((s, foundedSolutions) -> {
            var table = printXZTable(foundedSolutions);

            stringBuffer.append("Table for ").append(s).append("(").append(table.getArraySize()).append("): ").append(table).append('\n');
        });

        Files.writeString(Paths.get("test.txt"), stringBuffer.toString());
    }


    @Test
    public void printXZPingMove() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        List<Double> xzMove = new ArrayList<>();
        for (RawJump jump : jumps) {
            if (jump.calls().length == 0 || jump.jumps().length == 0)
                continue;

            CallJump firstCall = jump.calls()[0];
            if (firstCall.posY().isEmpty()) continue;

            ReceivedJump firstReceived = jump.jumps()[0];
            if (firstReceived.posY().isEmpty()) continue;

            xzMove.add(Math.hypot(firstReceived.posX().orElseThrow() - firstCall.posX().orElseThrow(),
                    firstReceived.posZ().orElseThrow() - firstCall.posZ().orElseThrow()));
        }

        var table = new WolframMathematica()
                .addArray(xzMove.toArray());
        table.export("test.txt");
        System.out.println("Table(" + table.getArraySize() + "): " + table);
    }
}
