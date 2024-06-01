package com.Jeka8833.packetVelocityGuesser.parser;

import com.Jeka8833.packetVelocityGuesser.parser.packet.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class ServerStorageFileParser {
    private static final byte[] HEADER_SIGNATURE = {'T', 'C', '_', 'J', 'I'};

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newVirtualThreadPerTaskExecutor();

    public static final Map<Integer, Class<? extends Packet>> DEFAULT_TNTCLIENT_PACKET_LIST = Map.ofEntries(
            Map.entry(0, CallJump.class),
            Map.entry(4, CallJump.class),
            Map.entry(1, GameInfo.class),
            Map.entry(2, PlayerCamera.class),
            Map.entry(3, ReceivedJump.class),
            Map.entry(5, ReceivedJump.class)
    );

    private static final Logger LOGGER = LogManager.getLogger(ServerStorageFileParser.class);
    private final Map<Integer, Constructor<? extends Packet>> packetIndexes;

    public ServerStorageFileParser() {
        this(DEFAULT_TNTCLIENT_PACKET_LIST);
    }

    public ServerStorageFileParser(Map<Integer, Class<? extends Packet>> packetIndexes) {
        try {
            Map<Integer, Constructor<? extends Packet>> constructors = new HashMap<>();
            for (Map.Entry<Integer, Class<? extends Packet>> entry : packetIndexes.entrySet()) {
                constructors.put(entry.getKey(),
                        entry.getValue().getDeclaredConstructor(DataInputStream.class, int.class));
            }

            this.packetIndexes = constructors;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while creating ServerStorageFileParser", e);
        }
    }


    @NotNull
    @Blocking
    @Contract("_, _ -> new")
    public FilePackets[] parseAllFiles(@NotNull Path @NotNull [] files, boolean skipErrors)
            throws ExecutionException {
        try {
            return parseAllFiles(files, skipErrors, EXECUTOR_SERVICE);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while parsing files", e);
        }
    }

    @NotNull
    @Blocking
    @Contract("_, _, _ -> new")
    public FilePackets[] parseAllFiles(@NotNull Path @NotNull [] files, boolean skipErrors,
                                       @NotNull ExecutorService executorService)
            throws InterruptedException, ExecutionException {
        Collection<Callable<FilePackets>> tasks = new ArrayList<>(files.length);
        for (Path file : files) {
            tasks.add(() -> readFile(file));
        }

        Collection<Future<FilePackets>> futures = executorService.invokeAll(tasks);

        Collection<FilePackets> database = new ArrayList<>(futures.size());
        for (Future<FilePackets> future : futures) {
            try {
                database.add(future.get());
            } catch (ExecutionException e) {
                if (skipErrors) {
                    LOGGER.debug("Error while parsing file", e.getCause());
                } else {
                    throw e;
                }
            }
        }

        return database.toArray(FilePackets[]::new);
    }

    @NotNull
    @Blocking
    @Contract(value = "_ -> new", pure = true)
    public FilePackets readFile(@NotNull Path file) throws IOException, NoSuchMethodException {
        Collection<Packet> packets = new ArrayList<>();

        try (InputStream inputStream = Files.newInputStream(file);
             DataInputStream dataStream = new DataInputStream(new BufferedInputStream(inputStream))) {

            if (!hasHeader(dataStream)) throw new IOException("File has no header: " + file);

            dataStream.skipNBytes(8); // skip timeCreateFile

            int packetID;
            while ((packetID = dataStream.read()) != -1) {
                Constructor<? extends Packet> aClass = packetIndexes.get(packetID);
                if (aClass == null) continue;

                Packet packet = aClass.newInstance(dataStream, packetID);
                packets.add(packet);
            }
        } catch (OutOfMemoryError e) {
            LOGGER.fatal("Out of memory");

            System.exit(0);
        } catch (EOFException e) {
            LOGGER.warn("Unexpected end of file", e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new NoSuchMethodException(e.getMessage());
        }

        return new FilePackets(file, packets.toArray(Packet[]::new));
    }

    private static boolean hasHeader(DataInputStream inputStream) throws IOException {
        return Arrays.equals(inputStream.readNBytes(HEADER_SIGNATURE.length), HEADER_SIGNATURE);
    }
}
