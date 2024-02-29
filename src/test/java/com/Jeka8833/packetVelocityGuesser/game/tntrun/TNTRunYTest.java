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
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TNTRunYTest {

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

    @Test
    public void printMinMaxApproximation() {
        for (int i = 1; i < 20; i++) {
            double max = TNTRunCalculation.calcJumpHeight(i, -90);
            double min = TNTRunCalculation.calcJumpHeight(i, 90);
            System.out.println("Jump " + i + " max: " + max + " min: " + min);
        }
    }

    @Test
    public void splitAndPrintJumps() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        FoundedSolution[] solutions = GUESSER.solveBestOrFirstVertical(jumps, 2, "Unknown");

        solutions = TNTRunVerticalGuesser.filterDuplicatesPosition(solutions);

        Map<String, List<FoundedSolution>> grouped = Arrays.stream(solutions)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(FoundedSolution::jumpName));

        grouped.forEach((s, foundedSolutions) -> {
            String table = WolframMathematica.toTable(foundedSolutions,
                    v -> v.position().pitch().orElseThrow(),
                    v -> v.receiver().velY().orElseThrow(), Throwable::printStackTrace);
            System.out.println("Table for " + s + "(" + foundedSolutions.size() + "): " + table);
        });
    }

    @Test
    public void allJumpFit() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        FoundedSolution[] solutions = GUESSER.solveBest(jumps);

        solutions = TNTRunVerticalGuesser.filterDuplicatesPosition(solutions);

        List<Object[]> result = new ArrayList<>();
        for (FoundedSolution solution : solutions) {
            if (solution == null || solution.position() == null || solution.receiver() == null) continue;

            result.add(new Object[]{
                    Integer.parseInt(solution.jumpName().substring(5)),
                    solution.position().pitch().orElseThrow(),
                    solution.receiver().velY().orElseThrow()
            });
        }

        String table = WolframMathematica.toTable(result.toArray(Object[][]::new));
        System.out.println("Table (" + result.size() + "): " + table);
    }

    @Test
    public void generatePingList() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        FoundedSolution[] solutions = GUESSER.solveBestOrFirstVertical(jumps, 2, "Unknown");

        Map<String, List<FoundedSolution>> grouped = Arrays.stream(solutions)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(FoundedSolution::jumpName))
                .entrySet().stream()
                .filter(v -> !v.getKey()
                        .equals("Unknown"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Object[] objects_ = new Object[30];
        Arrays.fill(objects_, "{}");
        grouped.forEach((s, foundedSolutions) -> {
            int jump = Integer.parseInt(s.substring(5));

            Collection<Object> objects = new ArrayList<>();
            for (FoundedSolution foundedSolution : foundedSolutions) {
                try {
                    objects.add(foundedSolution.getPingB(TimeUnit.NANOSECONDS).orElseThrow() /
                            TimeUnit.MILLISECONDS.toNanos(2));
                } catch (Exception ignored) {
                }
            }
            String temp = '{' + objects.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(",")) + '}';

            objects_[jump - 1] = temp;
        });

        String pings = WolframMathematica.formatEntry(objects_);
        System.out.println("Pings: " + pings);
    }

    @Test
    public void maxMinHeight() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        FoundedSolution[] solutions = GUESSER.solveBestOrFirstVertical(jumps, 2, "Unknown");

        solutions = Arrays.stream(solutions).filter(foundedSolution ->
                        foundedSolution != null &&
                                foundedSolution.position() != null && (
                                Math.abs(foundedSolution.position().pitch().orElseThrow().doubleValue()) == 90))
                .toArray(FoundedSolution[]::new);

        solutions = TNTRunVerticalGuesser.filterDuplicatesPositionAndResults(solutions);

        Map<String, List<FoundedSolution>> grouped = Arrays.stream(solutions)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(FoundedSolution::jumpName));

        grouped.forEach((s, foundedSolutions) -> {
            String table = WolframMathematica.toTable(foundedSolutions,
                    v -> v.position().pitch().orElseThrow(),
                    v -> v.receiver().velY().orElseThrow(), Throwable::printStackTrace);
            System.out.println("Table for " + s + "(" + foundedSolutions.size() + "): " + table);
        });
    }

    @Test
    public void generateDataset() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        FoundedSolution[] solutions = GUESSER.solveBestOrFirstVertical(jumps, 2, "Unknown");

        solutions = TNTRunVerticalGuesser.filterDuplicatesPosition(solutions);

        Map<String, List<FoundedSolution>> grouped = Arrays.stream(solutions)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(FoundedSolution::jumpName));

        List<FoundedSolution> list = grouped.entrySet().stream()
                .flatMap(v -> v.getValue().stream()
                        .filter(Objects::nonNull)
                        .limit(100))
                .toList();


        String table = WolframMathematica.toTable(list,
                v -> v.position().pitch().orElseThrow(),
                v -> v.receiver().velY().orElseThrow(), Throwable::printStackTrace);
        System.out.println("Table (" + list.size() + "): " + table);
    }
}
