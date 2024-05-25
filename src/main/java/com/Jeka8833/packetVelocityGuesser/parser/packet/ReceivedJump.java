package com.Jeka8833.packetVelocityGuesser.parser.packet;

import com.Jeka8833.packetVelocityGuesser.parser.CSVRecordExtender;
import com.Jeka8833.packetVelocityGuesser.parser.filter.ParameterState;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.OptionalInt;
import java.util.OptionalLong;

public record ReceivedJump(OptionalLong time, OptionalInt velX, OptionalInt velY,
                           OptionalInt velZ) implements Packet {
    public static final double SCALE = 8000D;

    public static final String FILTER_TIME = "received.jump.time";
    public static final String FILTER_VEL_X = "received.jump.velx";
    public static final String FILTER_VEL_Y = "received.jump.vely";
    public static final String FILTER_VEL_Z = "received.jump.velz";

    @SuppressWarnings("unused")
    public ReceivedJump(@NotNull CSVRecordExtender record) {
        this(record.getLong("Time"),
                record.getInteger("JumpVelX"),
                record.getInteger("JumpVelY"),
                record.getInteger("JumpVelZ"));
    }

    @SuppressWarnings("unused")
    public ReceivedJump(DataInputStream stream) throws IOException {
        this(OptionalLong.of(stream.readLong()),
                OptionalInt.of(stream.readInt()),
                OptionalInt.of(stream.readInt()),
                OptionalInt.of(stream.readInt()));
    }

    public double engineVelX() {
        return velX.orElseThrow() / SCALE;
    }

    public double engineVelY() {
        return velY.orElseThrow() / SCALE;
    }

    public double engineVelZ() {
        return velZ.orElseThrow() / SCALE;
    }

    @Override
    public @NotNull ParameterState filter(@NotNull String name, @NotNull ParameterState parameterState) {
        return switch (name) {
            case FILTER_TIME -> parameterState.getFilteredState(time.isPresent());
            case FILTER_VEL_X -> parameterState.getFilteredState(velX.isPresent());
            case FILTER_VEL_Y -> parameterState.getFilteredState(velY.isPresent());
            case FILTER_VEL_Z -> parameterState.getFilteredState(velZ.isPresent());
            default -> ParameterState.NONE;
        };
    }
}
