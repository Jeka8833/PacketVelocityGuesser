package com.Jeka8833.packetVelocityGuesser.parser.packet;

import com.Jeka8833.packetVelocityGuesser.parser.CSVRecordExtender;
import com.Jeka8833.packetVelocityGuesser.parser.filter.ParameterState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record PlayerInfo(Optional<String> version) implements Packet {

    public static final String FILTER_VERSION = "playerinfo.version";

    @SuppressWarnings("unused")
    public PlayerInfo(@NotNull CSVRecordExtender record) {
        this(record.tryGet("Version"));
    }

    @Override
    public @NotNull ParameterState filter(@NotNull String name, @NotNull ParameterState parameterState) {
        return switch (name) {
            case FILTER_VERSION -> parameterState.getFilteredState(version.isPresent());
            default -> ParameterState.NONE;
        };
    }
}
