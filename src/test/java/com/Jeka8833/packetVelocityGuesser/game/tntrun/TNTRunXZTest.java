package com.Jeka8833.packetVelocityGuesser.game.tntrun;

import com.Jeka8833.packetVelocityGuesser.ServerConstants;
import com.Jeka8833.packetVelocityGuesser.TNTClient;
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
import com.Jeka8833.packetVelocityGuesser.parser.packet.PlayerCamera;
import com.Jeka8833.packetVelocityGuesser.parser.packet.ReceivedJump;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TNTRunXZTest {

    //private static final Path PATH = Path.of("D:\\User\\Download\\newJumps\\");
    private static final Path PATH = TNTClient.getRecorderPath().resolve("08.04.2024 19.02.csv");
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
        //FilePackets[] packets = SERVER_PARSER.parseAllFiles(files, false);
        FilePackets[] packets = new CsvFileParser().parseAllFiles(new Path[]{PATH}, false);

        //packets = PACKET_FILTER.filter(packets);

        RawJump[] jumps = Composer.toRawJump(packets);

        //jumps = RawJumpFilter.filterDuplicates(jumps);
        //jumps = RawJumpFilter.filterZeroRotation(jumps);
        //jumps = RawJumpFilter.filterFutureCamera(jumps);
        jumps = RawJumpFilter.filterUncompletedJumps(jumps);

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
    public void predictXY() {
        System.out.println(TNTRunEndCalculation.getJump(250, 0, 0, new TNTRunEndCalculation.Velocity(0,0, 0), false).z() * 8000);
        System.out.println(TNTRunEndCalculation.getJump(250, 0, 0, new TNTRunEndCalculation.Velocity(0,0, 0), true).z() * 8000);
    }

    @Test
    public void allJumpFit() throws IOException, ExecutionException {
        RawJump[] jumps = readTestFiles();

        FoundedSolution[] solutions = GUESSER.solveBestOrFirstVertical(jumps, 2, "Unknown");

        solutions = TNTRunVerticalGuesser.filterDuplicatesPosition(solutions);

        Map<String, List<FoundedSolution>> grouped = Arrays.stream(solutions)
                .filter(Objects::nonNull)
                .filter(foundedSolution -> !foundedSolution.jumpName().equals("Unknown"))
                //.filter(foundedSolution -> foundedSolution.position() != null && foundedSolution.position().onGround().orElse(false))
                .collect(Collectors.groupingBy(FoundedSolution::jumpName));

        double scale = 0;

        grouped.forEach((s, foundedSolutions) -> {
            if (!s.equals("Jump 4")) return;


            FoundedSolution[] foundedSolutions1 = new FoundedSolution[foundedSolutions.size()];
            for (int i = 0, foundedSolutionsSize = foundedSolutions.size(); i < foundedSolutionsSize; i++) {
                FoundedSolution foundedSolution = foundedSolutions.get(i);
                boolean found = false;
                for (RawJump rawJump : jumps) {
                    @NotNull PlayerCamera @NotNull [] positions = rawJump.positions();
                    for (int j = 1; j < positions.length; j++) {
                        PlayerCamera playerCamera = positions[j];
                        if (foundedSolution.position() == playerCamera) {
                            int velocityX = (int) (foundedSolution.receiver().velX().orElseThrow() +
                                    (playerCamera.x().orElseThrow() - positions[j - 1].x().orElseThrow()) * scale);
                            int velocityZ = (int) (foundedSolution.receiver().velZ().orElseThrow() +
                                    (playerCamera.z().orElseThrow() - positions[j - 1].z().orElseThrow()) * scale);

                            foundedSolutions1[i] = new FoundedSolution(foundedSolution.jumpName(),
                                    foundedSolution.caller(), new ReceivedJump(foundedSolution.receiver().time(),
                                    Optional.of(velocityX), foundedSolution.receiver().velY(), Optional.of(velocityZ)),
                                    foundedSolution.position(), foundedSolution.error());
                            found = true;
                            break;
                        }
                    }
                    if (found) break;

                    foundedSolutions1[i] = foundedSolution;
                }
            }


            String table = printXZTable(List.of(foundedSolutions1));
            //String table = printXZTable(foundedSolutions);

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
