package com.Jeka8833.packetVelocityGuesser.parser.filter;

import com.Jeka8833.packetVelocityGuesser.TNTClient;
import com.Jeka8833.packetVelocityGuesser.parser.FilePackets;
import com.Jeka8833.packetVelocityGuesser.parser.packet.Packet;
import com.Jeka8833.packetVelocityGuesser.parser.packet.PlayerInfo;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * The VersionFilter class is used to filter FilePackets objects based on version information contained in {@link FilePackets}
 */
public class VersionFilter implements FileFilterAddon {

    private final Predicate<@Nullable String> versionPredicate;
    private final boolean allowIfNoVersion;

    private VersionFilter(Predicate<String> versionPredicate, boolean allowIfNoVersion) {
        this.versionPredicate = versionPredicate;
        this.allowIfNoVersion = allowIfNoVersion;
    }

    /**
     * Filters the given {@link FilePackets} based on the version information contained in the packets.
     *
     * @param filePackets The {@link FilePackets} object to filter.
     * @return The filtered {@link FilePackets} object, or null if the filter conditions are not met.
     * @throws NullPointerException if {@code filePackets} is null.
     */
    @Override
    @Nullable
    public FilePackets filter(@NotNull FilePackets filePackets) {
        for (Packet packet : filePackets.packets()) {
            if (packet instanceof PlayerInfo playerInfo) {
                Optional<String> version = playerInfo.version();
                if (version.isEmpty()) {
                    if (allowIfNoVersion) return filePackets;

                    return null;
                }

                if (versionPredicate.test(version.get())) return filePackets;

                return null;
            }
        }

        return null;
    }

    /**
     * Creates a new instance of the Builder class, used to construct a VersionFilter object with specific configuration.
     *
     * @return a new instance of the Builder class
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    /**
     * The Builder class is used to construct a VersionFilter object with specific configuration.
     */
    public static class Builder {
        private Predicate<String> versionPredicate;
        private boolean allowIfNoVersion = false;

        /**
         * Allow FilePackets if no version is found.
         *
         * @return the Builder object with the allowIfNoVersion property set to true
         */
        @NotNull
        @Contract(" -> this")
        public Builder allowIfAbsent() {
            allowIfNoVersion = true;

            return this;
        }

        /**
         * Block FilePackets if no version is found.
         *
         * @return the Builder object
         */
        @NotNull
        @Contract(" -> this")
        public Builder blockIfAbsent() {
            allowIfNoVersion = false;

            return this;
        }

        /**
         * Adds a condition to filter versions that are bigger than the given version.
         * Expression: packetVersion > version
         *
         * @param version the version to compare against
         * @return the Builder object
         */
        @NotNull
        @Contract("_ -> this")
        public Builder biggerThan(@NotNull String version) {
            andVersion((packetVersion) -> TNTClient.compareVersions(packetVersion, version) > 0);

            return this;
        }

        /**
         * Adds a condition to filter versions that are the same as or bigger than the given version.
         * Expression: packetVersion >= version
         *
         * @param version the version to compare against
         * @return the Builder object
         */
        @NotNull
        @Contract("_ -> this")
        public Builder sameOrBiggerThan(@NotNull String version) {
            andVersion((packetVersion) -> TNTClient.compareVersions(packetVersion, version) >= 0);

            return this;
        }

        /**
         * Adds a condition to filter versions that are the same as or smaller than the given version.
         * Expression: packetVersion <= version
         *
         * @param version the version to compare against
         * @return the Builder object
         */
        @NotNull
        @Contract("_ -> this")
        public Builder sameOrSmallerThan(@NotNull String version) {
            andVersion((packetVersion) -> TNTClient.compareVersions(packetVersion, version) <= 0);

            return this;
        }

        /**
         * Adds a condition to filter versions smaller than the given version.
         * Expression: packetVersion < version
         *
         * @param version the version to compare against
         * @return the Builder object
         */
        @NotNull
        @Contract("_ -> this")
        public Builder smallerThan(@NotNull String version) {
            andVersion((packetVersion) -> TNTClient.compareVersions(packetVersion, version) < 0);

            return this;
        }

        private void andVersion(Predicate<String> predicate) {
            this.versionPredicate = versionPredicate == null ? predicate : versionPredicate.and(predicate);
        }

        /**
         * Builds a VersionFilter object based on the provided configuration.
         *
         * @return a new VersionFilter object
         */
        @NotNull
        @Contract(" -> new")
        public VersionFilter build() {
            return new VersionFilter(versionPredicate == null ? s -> true : versionPredicate, allowIfNoVersion);
        }
    }
}
