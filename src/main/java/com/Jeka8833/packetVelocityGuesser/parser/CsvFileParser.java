package com.Jeka8833.packetVelocityGuesser.parser;

import com.Jeka8833.packetVelocityGuesser.parser.packet.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.DuplicateHeaderMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class CsvFileParser {

    public static final CSVFormat DEFAULT_TNTCLIENT_FORMAT = CSVFormat.EXCEL.builder()
            .setNullString(null)
            .setHeader()
            .setSkipHeaderRecord(true)
            .setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW)
            .build();

    public static final Map<String, Class<? extends Packet>> DEFAULT_TNTCLIENT_PACKET_LIST = Map.ofEntries(
            Map.entry("Call jump feather", CallJump.class),
            Map.entry("Call jump space", CallJump.class),
            Map.entry("Game Info", GameInfo.class),
            Map.entry("History", PlayerCamera.class),
            Map.entry("Player Info", PlayerInfo.class),
            Map.entry("Jump", ReceivedJump.class)
    );

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newVirtualThreadPerTaskExecutor();


    private static final Logger LOGGER = LogManager.getLogger(CsvFileParser.class);

    private final Map<String, Class<? extends Packet>> packetNames;
    private final CSVFormat fileformat;

    public CsvFileParser() {
        this(DEFAULT_TNTCLIENT_PACKET_LIST, DEFAULT_TNTCLIENT_FORMAT);
    }

    public CsvFileParser(@NotNull Map<@NotNull String, @NotNull Class<? extends Packet>> packetNames) {
        this(packetNames, DEFAULT_TNTCLIENT_FORMAT);
    }

    public CsvFileParser(@NotNull CSVFormat fileformat) {
        this(DEFAULT_TNTCLIENT_PACKET_LIST, fileformat);
    }

    public CsvFileParser(@NotNull Map<@NotNull String, @NotNull Class<? extends Packet>> packetNames,
                         @NotNull CSVFormat fileformat) {
        this.packetNames = packetNames;
        this.fileformat = fileformat;
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
        Collection<Callable<FilePackets>> tasks = new ArrayList<>();
        for (Path file : files) {
            tasks.add(() -> readFile(file));
        }
        List<Future<FilePackets>> futures = executorService.invokeAll(tasks);

        Collection<FilePackets> database = new ArrayList<>();
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
        if (!Files.isRegularFile(file)) throw new IOException("File is not regular file");
        if (!Files.isReadable(file)) throw new IOException("File is not readable");

        Collection<Packet> packets = new ArrayList<>();

        try (BufferedReader bufferedReader = Files.newBufferedReader(file);
             var csvParser = new CSVParser(bufferedReader, fileformat)) {
            for (CSVRecord record : csvParser) {
                var csvRecordExtender = new CSVRecordExtender(record);

                Optional<String> packetName = csvRecordExtender.tryGet("Type");
                if (packetName.isEmpty()) continue;

                Class<? extends Packet> aClass = packetNames.get(packetName.get());
                if (aClass == null) continue;

                Packet packet = aClass.getDeclaredConstructor(CSVRecordExtender.class)
                        .newInstance(csvRecordExtender);
                packets.add(packet);
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new NoSuchMethodException(e.getMessage());
        }

        return new FilePackets(file, packets.toArray(Packet[]::new));
    }

    @NotNull
    @Contract("_ -> new")
    public static Path @NotNull [] getAllFilesInFolder(@NotNull Path folder) throws IOException {
        try (Stream<Path> paths = Files.walk(folder)) {
            return paths.filter(Files::isRegularFile).toArray(Path[]::new);
        }
    }
}
