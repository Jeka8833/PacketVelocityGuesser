package com.Jeka8833.packetVelocityGuesser.game.tnttag;

import com.Jeka8833.packetVelocityGuesser.ServerConstants;
import com.Jeka8833.packetVelocityGuesser.TNTClient;
import com.Jeka8833.packetVelocityGuesser.composer.Composer;
import com.Jeka8833.packetVelocityGuesser.composer.RawJump;
import com.Jeka8833.packetVelocityGuesser.output.WolframMathematica;
import com.Jeka8833.packetVelocityGuesser.parser.CsvFileParser;
import com.Jeka8833.packetVelocityGuesser.parser.FilePackets;
import com.Jeka8833.packetVelocityGuesser.parser.ServerStorageFileParser;
import com.Jeka8833.packetVelocityGuesser.parser.filter.FileFilter;
import com.Jeka8833.packetVelocityGuesser.parser.filter.GameInfoFilter;
import com.Jeka8833.packetVelocityGuesser.parser.packet.Packet;
import com.Jeka8833.packetVelocityGuesser.parser.packet.PlayerCamera;
import com.Jeka8833.packetVelocityGuesser.parser.packet.ReceivedJump;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TNTTagTest {
    private static final Path PATH = Path.of("D:\\User\\Download\\jumpBackup\\analytic\\jumpInfoV4");
    private static final ServerStorageFileParser SERVER_PARSER = new ServerStorageFileParser();

    //private static final Path PATH = Path.of("D:\\User\\Download\\New folder\\Fall Web Y.csv");
    //private static final Path PATH = Path.of("C:\\Users\\Jeka8833\\AppData\\Roaming\\.minecraft\\TNTClients-records\\PacketRecorder\\04.07.2024 22.39.csv");
    //private static final Path PATH = Path.of("D:\\User\\Download\\23.06.2024 21.12.csv");
    //private static final CsvFileParser SERVER_PARSER = new CsvFileParser();

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

        packets = PACKET_FILTER.filter(packets);

        return Composer.toReceivedJump(packets);
    }

    @Test
    public void hitDelay() throws IOException, ExecutionException {
        Path[] files = CsvFileParser.getAllFilesInFolder(PATH);
        FilePackets[] packets = SERVER_PARSER.parseAllFiles(files, false);

        //packets = PACKET_FILTER.filter(packets);

        var wolframMathematica = new WolframMathematica();

        for (FilePackets filePackets : packets) {
            long lastPing = 0;

            for (Packet packet : filePackets.packets()) {
                if (packet instanceof ReceivedJump hit) {
                    long time = hit.time().orElseThrow();

                    if (lastPing != 0) {
                        long ping = TimeUnit.NANOSECONDS.toMillis(time - lastPing);
                        if (ping < 1000)
                            wolframMathematica.addVector(ping);
                    }

                    lastPing = time;
                }
            }
        }

        System.out.println("Table (" + wolframMathematica.getArraySize() + "): " + wolframMathematica);
    }

    @Test
    public void yVelocityEffect() throws IOException, ExecutionException {
        Path[] files = CsvFileParser.getAllFilesInFolder(PATH);
        FilePackets[] packets = SERVER_PARSER.parseAllFiles(files, false);

        //packets = PACKET_FILTER.filter(packets);

        RawJump[] rawJumps = Composer.toRawJump(packets);
        Collection<Integer> set = getKnownY();

        for (int shiftTick = 0; shiftTick < 4; shiftTick++) {
            var wolframMathematica = new WolframMathematica();

            for (RawJump rawJump : rawJumps) {
                @NotNull PlayerCamera @NotNull [] positions = rawJump.positions();

                for (ReceivedJump receivedJump : rawJump.jumps()) {
                    if (!set.contains(receivedJump.velY().orElseThrow())) continue;

                    int bestIndex = Integer.MAX_VALUE;
                    long lastTime = Long.MAX_VALUE;

                    for (int i = 0; i < positions.length; i++) {
                        PlayerCamera playerCamera = positions[i];
                        long time = receivedJump.time().orElseThrow() - playerCamera.time().orElseThrow();
                        if (time < 0) continue;

                        if (bestIndex == Integer.MAX_VALUE || time < lastTime) {
                            bestIndex = i;
                            lastTime = time;
                        }
                    }

                    if (bestIndex == Integer.MAX_VALUE) continue;

                    double velocityY = positions[Math.floorMod(bestIndex - shiftTick, positions.length)].y().orElseThrow() -
                            positions[Math.floorMod(bestIndex - 1 - shiftTick, positions.length)].y().orElseThrow();

                    if (Math.abs(velocityY) > 3.9) continue;

                    for (int i = 0; i < 100; i++) {
                        if ((int) (TNTTagCalculation.getYHitVelocity(0.47, i, true) * 8000) ==
                                receivedJump.velY().orElseThrow()) {
                            wolframMathematica.addVector(velocityY, i);
                            break;
                        }
                    }
                }
            }

            wolframMathematica.export("test" + shiftTick + ".txt");
            //System.out.println("Table (" + wolframMathematica.getArraySize() + "): " + wolframMathematica);
        }
    }

    @Test
    public void splitAndPrintJumps() throws IOException, ExecutionException {
        ReceivedJump[] receivedJump = readTestFiles();

        receivedJump = Arrays.stream(receivedJump).distinct().toArray(ReceivedJump[]::new);

        var wolframMathematica = new WolframMathematica()
                .processAndAddArray(v ->
                        new Object[]{
                                v.velX().orElseThrow(),
                                v.velY().orElseThrow(),
                                v.velZ().orElseThrow()
                        }, receivedJump)
                .export("test.txt");

        System.out.println("Table (" + wolframMathematica.getArraySize() + "): " + wolframMathematica);
    }

    @Test
    public void yTimeline() throws IOException, ExecutionException {
        ReceivedJump[] receivedJump = readTestFiles();

        var wolframMathematica = new WolframMathematica()
                .processAndAddArray(v ->
                        new Object[]{
                                TimeUnit.NANOSECONDS.toMillis(v.time().orElseThrow()),
                                v.velY().orElseThrow()
                        }, null, receivedJump)
                .export("test.txt");

        System.out.println("Table (" + wolframMathematica.getArraySize() + "): " + wolframMathematica);
    }

    @Test
    public void printY() throws IOException, ExecutionException {
        ReceivedJump[] receivedJump = readTestFiles();

        Object[] y = Arrays.stream(receivedJump)
                .map(v -> v.velY().orElseThrow())
                //.filter(v -> v <= 3760)
                .distinct()
                .sorted()
                .toArray(Integer[]::new);

        var wolframMathematica = new WolframMathematica()
                .addArray(y)
                .export("test.txt");

        System.out.println("Table (" + wolframMathematica.getArraySize() + "): " + wolframMathematica);
    }

    @Test
    public void printXZLengthProbability() throws IOException, ExecutionException {
        ReceivedJump[] receivedJump = readTestFiles();

        Collection<Integer> set = getKnownY();

        Object[] y = Arrays.stream(receivedJump)
                .filter(receivedJump1 -> receivedJump1.velY().isPresent())
                .filter(receivedJump1 -> set.contains(receivedJump1.velY().orElseThrow()))
                .map(v -> {
                    int x = v.velX().orElseThrow();
                    int z = v.velZ().orElseThrow();

                    return Math.hypot(x, z);
                })
                .toArray(Double[]::new);

        var wolframMathematica = new WolframMathematica()
                .addArray(y)
                .export("test.txt");

        System.out.println("Table (" + wolframMathematica.getArraySize() + "): " + wolframMathematica);
    }

    @Test
    public void groupYCount() throws IOException, ExecutionException {
        ReceivedJump[] receivedJump = readTestFiles();

        Map<Integer, Long> a = Arrays.stream(receivedJump).collect(Collectors.groupingBy(o -> o.velY().orElseThrow(), Collectors.counting()));

        Collection<Integer> set = getKnownY();


        List<Integer> list = new ArrayList<>(a.size());
        for (Map.Entry<Integer, Long> entry : a.entrySet()) {
            if (set.contains(entry.getKey()))
                continue;

            if (entry.getValue() > 0)
                list.add(entry.getKey());
        }
        list.sort(null);

        System.out.println(WolframMathematica.toWolframLanguage(list));
    }

    @Test
    public void searchUnknown() throws IOException, ExecutionException {
        ReceivedJump[] receivedJump = readTestFiles();

        Map<Integer, Long> a = Arrays.stream(receivedJump).collect(Collectors.groupingBy(o -> o.velY().orElseThrow(), Collectors.counting()));

        Collection<Integer> set = getKnownY();


        System.out.println("Total: " + receivedJump.length + " Unknown: " + a.keySet().stream().filter(aLong -> !set.contains(aLong)).mapToLong(a::get).sum());

        System.out.println(a.entrySet().stream().filter(e -> !set.contains(e.getKey())).sorted(Map.Entry.comparingByKey()).collect(Collectors.toList()));

        System.out.println(Arrays.toString(a.keySet().stream().filter(aLong -> !set.contains(aLong)).sorted().toArray()));
    }

    @Test
    public void splitAndPrintJumpsKnown() throws IOException, ExecutionException {
        ReceivedJump[] receivedJump = readTestFiles();

        Collection<Integer> set = getKnownY();

        receivedJump = Arrays.stream(receivedJump)
                .distinct()
                .filter(receivedJump1 -> set.contains(receivedJump1.velY().orElseThrow()))
                .filter(receivedJump1 -> {
                    int x = receivedJump1.velX().orElseThrow();
                    int y = receivedJump1.velY().orElseThrow();
                    int z = receivedJump1.velZ().orElseThrow();

                    double length = Math.sqrt(x * x + y * y + z * z);
                    return !(Math.abs(length - 8000) <= 3);
                })
                .toArray(ReceivedJump[]::new);

        WolframMathematica wolframMathematica = new WolframMathematica().processAndAddArray(v ->
                        new Object[]{
                                v.velX().orElseThrow(),
                                v.velY().orElseThrow(),
                                v.velZ().orElseThrow()
                        }, receivedJump)
                .export("test.txt");

        System.out.println("Table (" + wolframMathematica.getArraySize() + "): " + wolframMathematica);
    }

    private static Collection<Integer> getKnownY() {
        Collection<Integer> set = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            set.add((int) (TNTTagCalculation.getYHitVelocity(0.47, i, true) * 8000));
            set.add((int) (TNTTagCalculation.getYHitVelocity(0.4, i, false) * 8000));
        }
        return set;
    }

    @Test
    public void test() {
        for (int i = 0; i < 20; i++) {
            System.out.println("Tick: " + i + " Sprint: " + (int) (TNTTagCalculation.getYHitVelocity(0.47, i, true) * 8000)
                    + " Walk: " + (int) (TNTTagCalculation.getYHitVelocity(0.4, i, false) * 8000));
        }
    }
}
