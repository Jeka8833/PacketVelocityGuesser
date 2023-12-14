package com.Jeka8833.packetVelocityGuesser.parser.packet;

import com.Jeka8833.packetVelocityGuesser.parser.CSVRecordExtender;
import com.Jeka8833.packetVelocityGuesser.parser.filter.ParameterState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public record PlayerCamera(Optional<Long> time, Optional<BigDecimal> x, Optional<BigDecimal> y,
                           Optional<BigDecimal> z, Optional<BigDecimal> pitch, Optional<BigDecimal> yaw,
                           Optional<Boolean> onGround)
        implements Packet {

    public static final String FILTER_TIME = "playercamera.time";
    public static final String FILTER_X = "playercamera.x";
    public static final String FILTER_Y = "playercamera.y";
    public static final String FILTER_Z = "playercamera.z";
    public static final String FILTER_PITCH = "playercamera.pitch";
    public static final String FILTER_YAW = "playercamera.yaw";
    public static final String FILTER_GROUND = "playercamera.onground";

    private static final BigDecimal DEGREES_TO_RADIANS = new BigDecimal("0.0174532925199432957692369076848861271" +
            "3442871888541725456097191440171009114603449443682241569634509482"); // Pi / 180

    @SuppressWarnings("unused")
    public PlayerCamera(@NotNull CSVRecordExtender record) {
        this(record.getLong("Time"),
                record.getBigDecimal("PosX"),
                record.getBigDecimal("PosY"),
                record.getBigDecimal("PosZ"),
                record.getBigDecimal("Pitch"),
                record.getBigDecimal("Yaw"),
                record.getBoolean("OnGround"));
    }

    @Contract(pure = true)
    public long getTime(@NotNull TimeUnit timeUnit) {
        return timeUnit.convert(time.orElseThrow(), TimeUnit.NANOSECONDS);
    }

    public double yawCos() {
        return Math.cos(yaw.orElseThrow().multiply(DEGREES_TO_RADIANS).doubleValue());
    }

    public double yawSin() {
        return Math.sin(yaw.orElseThrow().multiply(DEGREES_TO_RADIANS).doubleValue());
    }

    public double pitchSin() {
        return Math.sin(pitch.orElseThrow().multiply(DEGREES_TO_RADIANS).doubleValue());
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
