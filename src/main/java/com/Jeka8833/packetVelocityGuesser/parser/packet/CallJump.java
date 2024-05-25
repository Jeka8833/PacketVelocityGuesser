package com.Jeka8833.packetVelocityGuesser.parser.packet;

import com.Jeka8833.packetVelocityGuesser.parser.CSVRecordExtender;
import com.Jeka8833.packetVelocityGuesser.parser.filter.ParameterState;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.OptionalLong;

public record CallJump(OptionalLong time, Optional<Boolean> useFeather) implements Packet {
    public static final String FILTER_TIME = "call.jump.time";
    public static final String FILTER_FEATHER = "call.jump.usefeather";

    @SuppressWarnings("unused")
    public CallJump(@NotNull CSVRecordExtender record) {
        this(record.getLong("Time"),
                record.tryGet("Type").map(s -> s.equalsIgnoreCase("Call jump feather")));
    }

    @SuppressWarnings("unused")
    public CallJump(DataInputStream stream) throws IOException {
        this(OptionalLong.of(stream.readLong()),
                Optional.of(stream.readBoolean()));
    }

    @NotNull
    @Override
    public ParameterState filter(@NotNull String name, @NotNull ParameterState parameterState) {
        return switch (name) {
            case FILTER_TIME -> parameterState.getFilteredState(time.isPresent());
            case FILTER_FEATHER -> parameterState.getFilteredState(useFeather.isPresent());
            default -> ParameterState.NONE;
        };
    }
}
