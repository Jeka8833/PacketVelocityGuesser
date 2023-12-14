package com.Jeka8833.packetVelocityGuesser.parser.filter;

import com.Jeka8833.packetVelocityGuesser.parser.FilePackets;
import com.Jeka8833.packetVelocityGuesser.parser.packet.GameInfo;
import com.Jeka8833.packetVelocityGuesser.parser.packet.Packet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public class GameInfoFilter implements FileFilterAddon {
    private final Parameter serverBrand;
    private final Parameter gameType;
    private final Parameter map;
    private final Parameter mode;

    public GameInfoFilter(Parameter serverBrand, Parameter gameType, Parameter map, Parameter mode) {
        this.serverBrand = serverBrand;
        this.gameType = gameType;
        this.map = map;
        this.mode = mode;
    }

    @Override
    @Nullable
    public FilePackets filter(@NotNull FilePackets filePackets) {
        Collection<Packet> packets = new ArrayList<>();
        boolean allow = false;

        for (Packet packet : filePackets.packets()) {
            if (packet instanceof GameInfo gameInfo) {
                allow = isValid(gameInfo);
            } else if (allow) {
                packets.add(packet);
            }
        }

        return new FilePackets(filePackets.file(), packets.toArray(new Packet[0]));
    }

    private boolean isValid(@NotNull GameInfo gameInfo) {
        if (!serverBrand.filter(gameInfo.serverBrand())) return false;
        if (!gameType.filter(gameInfo.gametype())) return false;
        if (!map.filter(gameInfo.map())) return false;
        return mode.filter(gameInfo.mode());
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private final Parameter serverBrand = new Parameter(this);
        private final Parameter gameType = new Parameter(this);
        private final Parameter map = new Parameter(this);
        private final Parameter mode = new Parameter(this);

        private Builder() {
        }

        public Parameter serverBrand() {
            return serverBrand;
        }

        public Parameter gameType() {
            return gameType;
        }

        public Parameter map() {
            return map;
        }

        public Parameter mode() {
            return mode;
        }

        public GameInfoFilter build() {
            return new GameInfoFilter(serverBrand.clone(), gameType.clone(), map.clone(), mode.clone());
        }
    }

    public static class Parameter implements Cloneable {
        private final Builder builder;
        private final Collection<String> allowed = new HashSet<>();
        private final Collection<String> blocked = new HashSet<>();
        private boolean allowIfAbsent = false;

        private Parameter(Builder builder) {
            this.builder = builder;
        }

        @NotNull
        @Contract(" -> this")
        public Parameter allowIfAbsent() {
            allowIfAbsent = true;

            return this;
        }

        @NotNull
        @Contract(" -> this")
        public Parameter blockIfAbsent() {
            allowIfAbsent = false;

            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Parameter require(@NotNull String @NotNull ... names) {
            for (String name : names) {
                allowed.add(name.toLowerCase());
            }

            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Parameter block(@NotNull String @NotNull ... names) {
            for (String name : names) {
                blocked.add(name.toLowerCase());
            }

            return this;
        }

        public Builder build() {
            return builder;
        }

        private boolean filter(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") @NotNull Optional<String> name) {
            if (allowed.isEmpty() && blocked.isEmpty()) return true;

            if (name.isEmpty()) return allowIfAbsent;

            String lower = name.get().toLowerCase();

            if (allowed.contains(lower))
                return true;
            if (blocked.contains(lower))
                return false;

            return allowIfAbsent;
        }

        @Override
        @SuppressWarnings("MethodDoesntCallSuperMethod")
        public Parameter clone() {
            Parameter clone = new Parameter(builder);
            clone.allowed.addAll(allowed);
            clone.blocked.addAll(blocked);
            clone.allowIfAbsent = allowIfAbsent;

            return clone;
        }
    }
}
