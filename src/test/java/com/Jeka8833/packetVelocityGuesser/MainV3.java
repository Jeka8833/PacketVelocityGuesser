package com.Jeka8833.packetVelocityGuesser;

import com.Jeka8833.packetVelocityGuesser.composer.Composer;
import com.Jeka8833.packetVelocityGuesser.composer.RawJump;
import com.Jeka8833.packetVelocityGuesser.composer.RawJumpFilter;
import com.Jeka8833.packetVelocityGuesser.game.tntrun.TNTRunVerticalGuesser;
import com.Jeka8833.packetVelocityGuesser.guesser.FoundedSolution;
import com.Jeka8833.packetVelocityGuesser.guesser.Guesser;
import com.Jeka8833.packetVelocityGuesser.output.WolframMathematica;
import com.Jeka8833.packetVelocityGuesser.parser.FilePackets;
import com.Jeka8833.packetVelocityGuesser.parser.CsvFileParser;
import com.Jeka8833.packetVelocityGuesser.parser.ServerStorageFileParser;
import com.Jeka8833.packetVelocityGuesser.parser.filter.FileFilter;
import com.Jeka8833.packetVelocityGuesser.parser.filter.GameInfoFilter;
import com.Jeka8833.packetVelocityGuesser.parser.filter.ParameterFilter;
import com.Jeka8833.packetVelocityGuesser.parser.filter.VersionFilter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class MainV3 {

    //private static final Path PATH = TNTClient.getRecorderPath();
    private static final Path PATH = Path.of("D:\\User\\Download\\jumpdata\\");
    private static final CsvFileParser CSV_PARSER = new CsvFileParser();
    private static final ServerStorageFileParser SERVER_PARSER = new ServerStorageFileParser();
    private static final FileFilter PACKET_FILTER = FileFilter.create()
            .add(ParameterFilter.create()
                    .allowIfAbsent()
                    .build())
            .add(VersionFilter.create()
                    .allowIfAbsent()
                    .build())
            .add(GameInfoFilter.create()

                    .mode()
                    .blockIfAbsent()
                    .require(ServerConstants.Hypixel.Mode.TNTRun)
                    .build()

                    .serverBrand()
                    .blockIfAbsent()
                    .require(ServerConstants.HYPIXEL_SERVER)
                    .build()

                    .build())
            .build();

    public static void main(String[] args) throws IOException, ExecutionException {
        Path[] files = CsvFileParser.getAllFilesInFolder(PATH);
        FilePackets[] packets = SERVER_PARSER.parseAllFiles(files, false);

        FilePackets[] filteredPackets = PACKET_FILTER.filter(packets);

        RawJump[] jumps = Composer.toRawJump(filteredPackets);

        RawJump[] filteredJumps = RawJumpFilter.filterDuplicates(
                RawJumpFilter.filterUncompletedJumps(jumps));

        Guesser guesser = new Guesser(null, new TNTRunVerticalGuesser());

        FoundedSolution[] solutions = guesser.solveBestOrFirstVertical(filteredJumps, 0.001, "Unknown");

        Map<String, List<FoundedSolution>> grouped = Arrays.stream(solutions)
                .collect(Collectors.groupingBy(FoundedSolution::jumpName));

        String all = WolframMathematica.toTable(solutions,
                v -> v.position().pitch().orElseThrow(),
                v -> v.receiver().velY().orElseThrow(), Throwable::printStackTrace);
        System.out.println("All table: " + all);

        grouped.forEach((s, foundedSolutions) -> {
            String table = WolframMathematica.toTable(foundedSolutions,
                    v -> v.position().pitch().orElseThrow(),
                    v -> v.receiver().velY().orElseThrow(), e -> {
                        e.printStackTrace();
                    });
            System.out.println("Table for " + s + ": " + table);
        });
    }


}
