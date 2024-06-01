package com.Jeka8833.packetVelocityGuesser.game.tnttag;

import com.Jeka8833.packetVelocityGuesser.ServerConstants;
import com.Jeka8833.packetVelocityGuesser.composer.Composer;
import com.Jeka8833.packetVelocityGuesser.output.WolframMathematica;
import com.Jeka8833.packetVelocityGuesser.parser.CsvFileParser;
import com.Jeka8833.packetVelocityGuesser.parser.FilePackets;
import com.Jeka8833.packetVelocityGuesser.parser.ServerStorageFileParser;
import com.Jeka8833.packetVelocityGuesser.parser.filter.FileFilter;
import com.Jeka8833.packetVelocityGuesser.parser.filter.GameInfoFilter;
import com.Jeka8833.packetVelocityGuesser.parser.packet.ReceivedJump;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TNTTagTest {

    private static final Path PATH = Path.of("D:\\TNTClientAnalytics\\jumpInfoV4");
    private static final ServerStorageFileParser SERVER_PARSER = new ServerStorageFileParser();
    private static final FileFilter PACKET_FILTER = FileFilter.create()
            .add(GameInfoFilter.create()
                    .mode()
                    .blockIfAbsent()
                    .require(ServerConstants.Hypixel.Mode.TNTTag)
                    .build()

                    .serverBrand()
                    .blockIfAbsent()
                    .require(ServerConstants.HYPIXEL_SERVER)
                    .build()

                    .build())
            .build();

    private static ReceivedJump[] readTestFiles() throws IOException, ExecutionException {
        Path[] files = CsvFileParser.getAllFilesInFolder(PATH);
        FilePackets[] packets = SERVER_PARSER.parseAllFiles(files, false);

        FilePackets[] filteredPackets = PACKET_FILTER.filter(packets);

        return Composer.toReceivedJump(filteredPackets);
    }

    @Test
    public void splitAndPrintJumps() throws IOException, ExecutionException {
        ReceivedJump[] receivedJump = readTestFiles();

        receivedJump = Arrays.stream(receivedJump).distinct().toArray(ReceivedJump[]::new);

        String table = WolframMathematica.toTable(Arrays.stream(receivedJump).toList(),
                v -> v.velX().orElseThrow(),
                v -> v.velY().orElseThrow(),
                v -> v.velZ().orElseThrow(), Throwable::printStackTrace);
        System.out.println("Table for " + "(" + receivedJump.length + "): " + table);

        Files.writeString(Paths.get("test.txt"), "Table for " + "(" + receivedJump.length + "): " + table);
    }

    @Test
    public void printY() throws IOException, ExecutionException {
        ReceivedJump[] receivedJump = readTestFiles();

        Object[] y = Arrays.stream(receivedJump).map(v -> v.velY().orElseThrow()).toArray(Integer[]::new);
        System.out.println(WolframMathematica.formatEntry(y));
    }

    @Test
    public void groupYCount() throws IOException, ExecutionException {
        ReceivedJump[] receivedJump = readTestFiles();

        Map<Integer, Long> a = Arrays.stream(receivedJump).collect(Collectors.groupingBy(o -> o.velY().orElseThrow(), Collectors.counting()));
        List<Integer> list = new ArrayList<>(a.size());
        for (Map.Entry<Integer, Long> entry : a.entrySet()) {
            if (entry.getValue() > 200)
                list.add(entry.getKey());
        }
        list.sort(null);

        System.out.println(Arrays.toString(list.toArray()));
    }
}
