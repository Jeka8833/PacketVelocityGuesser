package com.Jeka8833.packetVelocityGuesser.parser.filter;

import com.Jeka8833.packetVelocityGuesser.parser.FilePackets;
import com.Jeka8833.packetVelocityGuesser.parser.packet.Packet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The ParameterFilter class is a FileFilterAddon implementation that filters packets based on specific parameters.
 * It allows users to require or block specific names in packet filters.
 */
public class ParameterFilter implements FileFilterAddon {

    private final Map<String, ParameterState> filters;
    private final boolean allowIfAbsent;

    private ParameterFilter(Map<String, ParameterState> filters, boolean allowIfAbsent) {
        this.filters = filters.entrySet().stream()
                .filter(entry -> entry.getValue() != ParameterState.NONE)
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        this.allowIfAbsent = allowIfAbsent;
    }

    @Nullable
    @Override
    public FilePackets filter(@NotNull FilePackets filePackets) {
        Collection<Packet> filtered = new ArrayList<>();

        for (Packet packet : filePackets.packets()) {
            if (isAllowed(packet)) {
                filtered.add(packet);
            }
        }

        return new FilePackets(filePackets.file(), filtered.toArray(Packet[]::new));
    }

    private boolean isAllowed(@NotNull Packet packet) {
        boolean allRequired = false;

        for (Map.Entry<String, ParameterState> entry : filters.entrySet()) {
            ParameterState parameterState = packet.filter(entry.getKey(), entry.getValue());
            if (parameterState == ParameterState.REQUIRE) allRequired = true;

            if (parameterState == ParameterState.BLOCK ||
                    (entry.getValue() == ParameterState.REQUIRE &&
                            parameterState == ParameterState.NONE)) {
                return false;
            }
        }

        if (!allRequired)
            return allowIfAbsent;

        return true;
    }

    /**
     * Creates a new instance of the Builder class for ParameterFilter.
     *
     * @return A new instance of the Builder class.
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    public static Builder create() {
        return new Builder();
    }


    /**
     * Builder class for creating instances of ParameterFilter.
     */
    public static class Builder {
        private final Map<String, ParameterState> filters = new HashMap<>();
        private boolean allowIfAbsent;

        private Builder() {
        }

        public Builder allowIfAbsent() {
            allowIfAbsent = true;
            return this;
        }

        public Builder blockIfAbsent() {
            allowIfAbsent = false;
            return this;
        }

        /**
         * Adds filters to require specific names in {@link Packet}.
         *
         * @param names the names of the filters to be added
         * @return the updated Builder instance
         * @throws IllegalStateException if a filter with the same name already exists with a different state
         */
        @NotNull
        @Contract("_ -> this")
        public Builder require(@NotNull String @NotNull ... names) throws IllegalStateException {
            for (String name : names)
                addFilter(name, ParameterState.REQUIRE);

            return this;
        }

        /**
         * Adds filters to block specific names in {@link Packet}.
         *
         * @param names the names of the filters to be added
         * @return the updated Builder instance
         * @throws IllegalStateException if a filter with the same name already exists with a different state
         */
        @NotNull
        @Contract("_ -> this")
        public Builder block(@NotNull String @NotNull ... names) throws IllegalStateException {
            for (String name : names)
                addFilter(name, ParameterState.BLOCK);

            return this;
        }

        private void addFilter(@NotNull String name, @NotNull ParameterState state)
                throws IllegalStateException {
            filters.compute(name, (s, filterState) -> {
                if (filterState == null || filterState == state) {
                    return state;
                } else {
                    throw new IllegalStateException("Filter " + name + " already exists with state " + filterState);
                }
            });
        }

        /**
         * Builds an instance of ParameterFilter.
         *
         * @return A new instance of ParameterFilter.
         */
        @NotNull
        @Contract(" -> new")
        public ParameterFilter build() {
            return new ParameterFilter(filters, allowIfAbsent);
        }
    }
}
