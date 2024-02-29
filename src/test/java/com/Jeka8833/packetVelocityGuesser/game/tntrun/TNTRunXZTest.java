package com.Jeka8833.packetVelocityGuesser.game.tntrun;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TNTRunXZTest {

    private static final Path PATH = Path.of("D:\\User\\Download\\jumps\\");
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
        FilePackets[] packets = SERVER_PARSER.parseAllFiles(files, false);

        FilePackets[] filteredPackets = PACKET_FILTER.filter(packets);

        RawJump[] jumps = Composer.toRawJump(filteredPackets);

        jumps = RawJumpFilter.filterUncompletedJumps(jumps);
        jumps = RawJumpFilter.filterDuplicates(jumps);
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

    private static boolean isMaximum(double rotation) {
        if (rotation == 0) return true;

        if (rotation > 0) {
            for (int i = 0; i < 360 * 10_000; i += 90) {
                if (Math.abs(rotation - i) < 0.001) return true;
                if (rotation < i) return false;
            }
        } else {
            for (int i = 0; i > -360 * 10_000; i -= 90) {
                if (Math.abs(rotation - i) < 0.001) return true;
                if (rotation > i) return false;
            }
        }

        return false;
    }

    @Test
    public void allJumpFit() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        FoundedSolution[] solutions = GUESSER.solveBest(jumps);

        solutions = TNTRunVerticalGuesser.filterDuplicatesPosition(solutions);

        Map<String, List<FoundedSolution>> grouped = Arrays.stream(solutions)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(FoundedSolution::jumpName));

        grouped.forEach((s, foundedSolutions) -> {
            if (!s.equals("Jump 1")) return;

            String table = printXZTable(foundedSolutions);

            try {
                Files.write(Path.of("D:\\User\\Download\\calculations\\result.txt"), table.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            long count = table.chars().filter(ch -> ch == '}').count() - 1;

            System.out.println("Table for " + s + "(" + count + "): " + table);
        });
    }

    @Test
    public void findMaxMin() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        FoundedSolution[] solutions = GUESSER.solveBestOrFirstVertical(jumps, 2, "Unknown");

        solutions = Arrays.stream(solutions).filter(foundedSolution ->
                        foundedSolution != null &&
                                foundedSolution.position() != null &&
                                isMaximum(foundedSolution.position().yaw().orElseThrow()))
                .toArray(FoundedSolution[]::new);

        solutions = TNTRunVerticalGuesser.filterDuplicatesPositionAndResults(solutions);

        Map<String, List<FoundedSolution>> grouped = Arrays.stream(solutions)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(FoundedSolution::jumpName));

        grouped.forEach((s, foundedSolutions) -> {
            String table = WolframMathematica.toTable(foundedSolutions,
                    v -> v.position().yaw().orElseThrow(),
                    v -> v.receiver().velZ().orElseThrow(), Throwable::printStackTrace);
            System.out.println("Table for " + s + "(" + foundedSolutions.size() + "): " + table);
        });
    }
}
