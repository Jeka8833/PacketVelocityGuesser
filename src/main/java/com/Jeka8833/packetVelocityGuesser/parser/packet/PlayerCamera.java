package com.Jeka8833.packetVelocityGuesser.parser.packet;

import com.Jeka8833.packetVelocityGuesser.parser.CSVRecordExtender;
import com.Jeka8833.packetVelocityGuesser.parser.filter.ParameterState;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Optional;

public record PlayerCamera(Optional<Long> time, Optional<Double> x, Optional<Double> y,
                           Optional<Double> z, Optional<Float> pitch, Optional<Float> yaw,
                           Optional<Boolean> onGround)
        implements Packet {

    public static final String FILTER_TIME = "playercamera.time";
    public static final String FILTER_X = "playercamera.x";
    public static final String FILTER_Y = "playercamera.y";
    public static final String FILTER_Z = "playercamera.z";
    public static final String FILTER_PITCH = "playercamera.pitch";
    public static final String FILTER_YAW = "playercamera.yaw";
    public static final String FILTER_GROUND = "playercamera.onground";

    @SuppressWarnings("unused")
    public PlayerCamera(@NotNull CSVRecordExtender record) {
        this(record.getLong("Time"),
                record.getDouble("PosX"),
                record.getDouble("PosY"),
                record.getDouble("PosZ"),
                record.getFloat("Pitch"),
                record.getFloat("Yaw"),
                record.getBoolean("OnGround"));
    }

    @SuppressWarnings("unused")
    public PlayerCamera(DataInputStream stream) throws IOException {
        this(Optional.of(stream.readLong()),
                Optional.of(stream.readDouble()),
                Optional.of(stream.readDouble()),
                Optional.of(stream.readDouble()),
                Optional.of(stream.readFloat()),
                Optional.of(stream.readFloat()),
                Optional.of(stream.readBoolean()));
    }

    public double yawCos() {
        return Math.cos(Math.toRadians(yaw.orElseThrow()));
    }

    public double yawSin() {
        return Math.sin(Math.toRadians(yaw.orElseThrow()));
    }

    public double pitchCos() {
        return Math.cos(Math.toRadians(pitch.orElseThrow()));
    }

    public double pitchSin() {
        return Math.sin(Math.toRadians(pitch.orElseThrow()));
    }

    @NotNull
    @Override
    public ParameterState filter(@NotNull String name, @NotNull ParameterState parameterState) {
        return switch (name) {
            case FILTER_TIME -> parameterState.getFilteredState(time.isPresent());
            case FILTER_X -> parameterState.getFilteredState(x.isPresent());
            case FILTER_Y -> parameterState.getFilteredState(y.isPresent());
            case FILTER_Z -> parameterState.getFilteredState(z.isPresent());
            case FILTER_PITCH -> parameterState.getFilteredState(pitch.isPresent());
            case FILTER_YAW -> parameterState.getFilteredState(yaw.isPresent());
            case FILTER_GROUND -> parameterState.getFilteredState(onGround.isPresent());
            default -> ParameterState.NONE;
        };
    }
}
