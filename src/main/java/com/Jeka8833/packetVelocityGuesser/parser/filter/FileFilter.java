package com.Jeka8833.packetVelocityGuesser.parser.filter;

import com.Jeka8833.packetVelocityGuesser.parser.FilePackets;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * The FileFilter class is used to filter an array of FilePackets using specified file filter addons.
 * FilePackets that pass the filtering criteria will be included in the filtered array.
 */
public class FileFilter {
    private final FileFilterAddon[] addons;

    private FileFilter(FileFilterAddon[] addons) {
        this.addons = addons;
    }

    /**
     * Filters an array of FilePackets using the specified filter addons.
     *
     * @param packets the FilePackets array to filter
     * @return an array of filtered FilePackets
     */
    @NotNull
    public FilePackets @NotNull [] filter(FilePackets @NotNull ... packets) {
        FilePackets[] filtered = packets.clone();

        for (int i = 0; i < filtered.length; i++) {
            for (FileFilterAddon addon : addons) {
                FilePackets filePackets = filtered[i];
                if (filePackets == null) break;

                filtered[i] = addon.filter(filePackets);
            }
        }

        return Arrays.stream(filtered)
                .filter(filePackets -> filePackets != null && filePackets.packets().length > 0)
                .toArray(FilePackets[]::new);
    }

    /**
     * Creates a new instance of {@link Builder} which is used to construct a {@link FileFilter}.
     *
     * @return a new instance of {@link Builder}
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    /**
     * This class represents a builder for creating instances of FileFilter.
     */
    public static class Builder {
        private final Collection<FileFilterAddon> addons = new ArrayList<>();

        private Builder() {
        }

        /**
         * Adds one or more {@link FileFilterAddon}s as filters to the builder.
         *
         * @param addons the file filter addons to be added
         * @return the updated builder instance
         */
        @NotNull
        @Contract("_ -> this")
        public Builder add(FileFilterAddon... addons) {
            this.addons.addAll(Arrays.asList(addons));

            return this;
        }

        /**
         * Creates a new instance of {@link FileFilter} with the specified addons.
         *
         * @return a new {@link FileFilter} object
         */
        @NotNull
        @Contract(" -> new")
        public FileFilter build() {
            return new FileFilter(addons.toArray(new FileFilterAddon[0]));
        }
    }
}
