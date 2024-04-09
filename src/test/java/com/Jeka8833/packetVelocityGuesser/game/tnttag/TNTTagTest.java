package com.Jeka8833.packetVelocityGuesser.game.tnttag;

import com.Jeka8833.packetVelocityGuesser.ServerConstants;
import com.Jeka8833.packetVelocityGuesser.TNTClient;
import com.Jeka8833.packetVelocityGuesser.composer.Composer;
import com.Jeka8833.packetVelocityGuesser.composer.RawJump;
import com.Jeka8833.packetVelocityGuesser.composer.RawJumpFilter;
import com.Jeka8833.packetVelocityGuesser.game.tntrun.TNTRunVerticalGuesser;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TNTTagTest {

    //private static final Path PATH = Path.of("D:\\User\\Download\\newJumps\\");
    private static final Path PATH = TNTClient.getRecorderPath().resolve("01.03.2024 21.58.csv");
    private static final ServerStorageFileParser SERVER_PARSER = new ServerStorageFileParser();
    private static final Guesser GUESSER = new Guesser(null, new TNTRunVerticalGuesser());
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

    private static RawJump[] readTestFiles() throws IOException, ExecutionException {
        Path[] files = CsvFileParser.getAllFilesInFolder(PATH);
        FilePackets[] packets = SERVER_PARSER.parseAllFiles(files, false);
        //FilePackets[] packets = new CsvFileParser().parseAllFiles(new Path[]{PATH}, false);

        FilePackets[] filteredPackets = PACKET_FILTER.filter(packets);

        RawJump[] jumps = Composer.toRawJump(filteredPackets);

        //jumps = RawJumpFilter.filterUncompletedJumps(jumps);
        jumps = RawJumpFilter.filterDuplicates(jumps);
        return jumps;
    }

    @Test
    public void splitAndPrintJumps() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        ReceivedJump[] receivedJump = Arrays.stream(jumps).flatMap(j -> Arrays.stream(j.jumps())).toArray(ReceivedJump[]::new);

        String table = WolframMathematica.toTable(Arrays.stream(receivedJump).toList(),
                v -> v.velX().orElseThrow(),
                v -> v.velY().orElseThrow(),
                v -> v.velZ().orElseThrow(), Throwable::printStackTrace);
        System.out.println("Table for " + "(" + receivedJump.length + "): " + table);
    }
}
