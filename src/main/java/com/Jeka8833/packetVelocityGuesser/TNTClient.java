package com.Jeka8833.packetVelocityGuesser;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The TNTClient class provides utility methods for retrieving information related to the TNTClient application.
 * It includes methods for getting the Minecraft directory, the operating system platform, and the path to the PacketRecorder directory.
 */
public class TNTClient {

    /**
     * Compares two version strings and returns an integer indicating their order.
     * The version strings are expected to consist of numbers separated by dots, and may contain other non-numeric characters that are ignored.
     * For example, compareVersions(“1.2.3”, “1.2.4”) returns -1, compareVersions(“2.0”, “2.0.0”) returns 0, and compareVersions(“3.1.1”, “3.0.9”) returns 1.
     *
     * @param x the first version string to compare
     * @param y the second version string to compare
     * @return -1 if x is less than y, 0 if x is equal to y, or 1 if x is greater than y
     * @throws NumberFormatException if the string does not contain a parsable integer
     */
    @Contract(pure = true)
    @Range(from = -1, to = 1)
    public static int compareVersions(@NotNull String x, @NotNull String y) {
        String[] parts1 = x.replaceAll("[^0-9.]+", "").split("\\.");
        String[] parts2 = y.replaceAll("[^0-9.]+", "").split("\\.");
        int minLength = Math.min(parts1.length, parts2.length);

        for (int i = 0; i < minLength; i++) {
            int num1 = Integer.parseInt(parts1[i]);
            int num2 = Integer.parseInt(parts2[i]);

            if (num1 != num2) return num1 > num2 ? 1 : -1;
        }

        for (int i = minLength; i < parts1.length; i++) {
            if (Integer.parseInt(parts1[i]) != 0) return 1;
        }
        for (int i = minLength; i < parts2.length; i++) {
            if (Integer.parseInt(parts2[i]) != 0) return -1;
        }

        return 0;
    }

    /**
     * Retrieves the path for the PacketRecorder directory.
     * The path is based on the current Minecraft directory.
     *
     * @return the path to the PacketRecorder directory as a Path object
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    public static Path getRecorderPath() {
        return getMinecraftDirectory().resolve("TNTClients-records").resolve("PacketRecorder");
    }

    /**
     * Returns the Minecraft directory based on the current platform.
     *
     * @return the Minecraft directory as a Path object
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    public static Path getMinecraftDirectory() {
        return switch (getPlatform()) {
            case LINUX, SOLARIS -> Paths.get(System.getProperty("user.home"), ".minecraft");
            case WINDOWS -> {
                String appdata = System.getenv("APPDATA");
                yield appdata == null ?
                        Paths.get(System.getProperty("user.home"), ".minecraft") :
                        Paths.get(appdata, ".minecraft");
            }
            case MACOS -> Paths.get(System.getProperty("user.home"),
                    "Library", "Application Support", "minecraft");
            default -> Paths.get(System.getProperty("user.home"), "minecraft");
        };
    }

    /**
     * Retrieves the current operating system platform.
     *
     * @return the {@link OS} enum representing the current platform.
     */
    @NotNull
    public static OS getPlatform() {
        final String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) return OS.WINDOWS;
        if (osName.contains("mac")) return OS.MACOS;
        if (osName.contains("solaris") || osName.contains("sunos")) return OS.SOLARIS;
        if (osName.contains("linux") || osName.contains("unix")) return OS.LINUX;

        return OS.UNKNOWN;
    }

    /**
     * The {@code OS} enum represents different operating systems.
     * It provides constants for the following operating systems: LINUX, SOLARIS, WINDOWS, MACOS, UNKNOWN.
     */
    public enum OS {
        LINUX,
        SOLARIS,
        WINDOWS,
        MACOS,
        UNKNOWN
    }
}
