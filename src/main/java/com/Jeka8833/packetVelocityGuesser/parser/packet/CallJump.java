package com.Jeka8833.packetVelocityGuesser.parser.packet;

import com.Jeka8833.packetVelocityGuesser.parser.CSVRecordExtender;
import com.Jeka8833.packetVelocityGuesser.parser.filter.ParameterState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public record CallJump(Optional<Long> time, Optional<Boolean> useFeather)
        implements Packet {

    public static final String FILTER_TIME = "call.jump.time";
    public static final String FILTER_FEATHER = "call.jump.usefeather";

    @SuppressWarnings("unused")
    public CallJump(@NotNull CSVRecordExtender record) {
        this(record.getLong("Time"),
                record.tryGet("Type").map(s -> s.equalsIgnoreCase("Call jump feather")));
    }

    @Contract(pure = true)
    public long getTime(@NotNull TimeUnit timeUnit) {
        return timeUnit.convert(time.orElseThrow(), TimeUnit.NANOSECONDS);
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
