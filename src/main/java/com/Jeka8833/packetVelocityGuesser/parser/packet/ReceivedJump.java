package com.Jeka8833.packetVelocityGuesser.parser.packet;

import com.Jeka8833.packetVelocityGuesser.parser.CSVRecordExtender;
import com.Jeka8833.packetVelocityGuesser.parser.filter.ParameterState;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public final class ReceivedJump implements Packet {
    public static final double SCALE = 8000D;

    public static final String FILTER_TIME = "received.jump.time";
    public static final String FILTER_VEL_X = "received.jump.velx";
    public static final String FILTER_VEL_Y = "received.jump.vely";
    public static final String FILTER_VEL_Z = "received.jump.velz";
    public static final String FILTER_POS_X = "received.jump.pos.x";
    public static final String FILTER_POS_Y = "received.jump.pos.y";
    public static final String FILTER_POS_Z = "received.jump.pos.z";
    public static final String FILTER_PITCH = "received.jump.pitch";
    public static final String FILTER_YAW = "received.jump.yaw";

    private final OptionalLong time;
    private final OptionalInt velX;
    private final OptionalInt velY;
    private final OptionalInt velZ;
    private final OptionalDouble posX;
    private final OptionalDouble posY;
    private final OptionalDouble posZ;
    private final Optional<Float> lastReportedPitch;
    private final Optional<Float> lastReportedYaw;

    public ReceivedJump(OptionalLong time, OptionalInt velX, OptionalInt velY, OptionalInt velZ,
                        OptionalDouble posX, OptionalDouble posY, OptionalDouble posZ,
                        Optional<Float> lastReportedPitch, Optional<Float> lastReportedYaw) {
        this.time = time;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.lastReportedPitch = lastReportedPitch;
        this.lastReportedYaw = lastReportedYaw;
    }

    @SuppressWarnings("unused")
    public ReceivedJump(@NotNull CSVRecordExtender record) {
        this(record.getLong("Time"),
                record.getInteger("JumpVelX"),
                record.getInteger("JumpVelY"),
                record.getInteger("JumpVelZ"),
                record.getDouble("PosX"), record.getDouble("PosY"), record.getDouble("PosZ"),
                record.getFloat("Pitch"), record.getFloat("Yaw"));
    }

    @SuppressWarnings("unused")
    public ReceivedJump(DataInputStream stream, int packetID) throws IOException {
        this.time = OptionalLong.of(stream.readLong());
        this.velX = OptionalInt.of(stream.readInt());
        this.velY = OptionalInt.of(stream.readInt());
        this.velZ = OptionalInt.of(stream.readInt());

        if (packetID == 3) {
            this.posX = OptionalDouble.empty();
            this.posY = OptionalDouble.empty();
            this.posZ = OptionalDouble.empty();
        } else {
            this.posX = OptionalDouble.of(stream.readDouble());
            this.posY = OptionalDouble.of(stream.readDouble());
            this.posZ = OptionalDouble.of(stream.readDouble());
        }

        this.lastReportedPitch = Optional.empty();
        this.lastReportedYaw = Optional.empty();
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
            case FILTER_POS_X -> parameterState.getFilteredState(posX.isPresent());
            case FILTER_POS_Y -> parameterState.getFilteredState(posY.isPresent());
            case FILTER_POS_Z -> parameterState.getFilteredState(posZ.isPresent());
            case FILTER_PITCH -> parameterState.getFilteredState(lastReportedPitch.isPresent());
            case FILTER_YAW -> parameterState.getFilteredState(lastReportedYaw.isPresent());
            default -> ParameterState.NONE;
        };
    }

    public OptionalLong time() {
        return time;
    }

    public OptionalInt velX() {
        return velX;
    }

    public OptionalInt velY() {
        return velY;
    }

    public OptionalInt velZ() {
        return velZ;
    }

    public OptionalDouble posX() {
        return posX;
    }

    public OptionalDouble posY() {
        return posY;
    }

    public OptionalDouble posZ() {
        return posZ;
    }

    public Optional<Float> lastReportedPitch() {
        return lastReportedPitch;
    }

    public Optional<Float> lastReportedYaw() {
        return lastReportedYaw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReceivedJump that)) return false;

        return time.equals(that.time) && velX.equals(that.velX) &&
                velY.equals(that.velY) && velZ.equals(that.velZ) &&
                posX.equals(that.posX) && posY.equals(that.posY) &&
                posZ.equals(that.posZ) && lastReportedPitch.equals(that.lastReportedPitch) &&
                lastReportedYaw.equals(that.lastReportedYaw);
    }

    @Override
    public int hashCode() {
        int result = time.hashCode();
        result = 31 * result + velX.hashCode();
        result = 31 * result + velY.hashCode();
        result = 31 * result + velZ.hashCode();
        result = 31 * result + posX.hashCode();
        result = 31 * result + posY.hashCode();
        result = 31 * result + posZ.hashCode();
        result = 31 * result + lastReportedPitch.hashCode();
        result = 31 * result + lastReportedYaw.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ReceivedJump{" +
                "time=" + time +
                ", velX=" + velX +
                ", velY=" + velY +
                ", velZ=" + velZ +
                ", posX=" + posX +
                ", posY=" + posY +
                ", posZ=" + posZ +
                ", lastReportedPitch=" + lastReportedPitch +
                ", lastReportedYaw=" + lastReportedYaw +
                '}';
    }
}
