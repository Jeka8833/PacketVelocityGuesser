package com.Jeka8833.packetVelocityGuesser.parser.packet;

import com.Jeka8833.packetVelocityGuesser.parser.CSVRecordExtender;
import com.Jeka8833.packetVelocityGuesser.parser.filter.ParameterState;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;

public final class CallJump implements Packet {
    public static final String FILTER_TIME = "call.jump.time";
    public static final String FILTER_FEATHER = "call.jump.usefeather";
    public static final String FILTER_POS_X = "call.jump.pos.x";
    public static final String FILTER_POS_Y = "call.jump.pos.y";
    public static final String FILTER_POS_Z = "call.jump.pos.z";
    public static final String FILTER_PITCH = "call.jump.pitch";
    public static final String FILTER_YAW = "call.jump.yaw";

    private final OptionalLong time;
    private final Optional<Boolean> useFeather;
    private final OptionalDouble posX;
    private final OptionalDouble posY;
    private final OptionalDouble posZ;
    private final Optional<Float> lastReportedPitch;
    private final Optional<Float> lastReportedYaw;

    public CallJump(OptionalLong time, Optional<Boolean> useFeather,
                    OptionalDouble posX, OptionalDouble posY, OptionalDouble posZ,
                    Optional<Float> lastReportedPitch, Optional<Float> lastReportedYaw) {
        this.time = time;
        this.useFeather = useFeather;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.lastReportedPitch = lastReportedPitch;
        this.lastReportedYaw = lastReportedYaw;
    }

    @SuppressWarnings("unused")
    public CallJump(@NotNull CSVRecordExtender record) {
        this(record.getLong("Time"),
                record.tryGet("Type").map(s -> s.equalsIgnoreCase("Call jump feather")),
                record.getDouble("PosX"), record.getDouble("PosY"), record.getDouble("PosZ"),
                record.getFloat("Pitch"), record.getFloat("Yaw"));
    }

    @SuppressWarnings("unused")
    public CallJump(DataInputStream stream, int packetID) throws IOException {
        this.time = OptionalLong.of(stream.readLong());
        this.useFeather = Optional.of(stream.readBoolean());

        if (packetID == 0) {
            this.posX = OptionalDouble.empty();
            this.posY = OptionalDouble.empty();
            this.posZ = OptionalDouble.empty();
            this.lastReportedPitch = Optional.empty();
            this.lastReportedYaw = Optional.empty();
        } else {
            this.posX = OptionalDouble.of(stream.readDouble());
            this.posY = OptionalDouble.of(stream.readDouble());
            this.posZ = OptionalDouble.of(stream.readDouble());
            this.lastReportedPitch = Optional.of(stream.readFloat());
            this.lastReportedYaw = Optional.of(stream.readFloat());
        }
    }

    @NotNull
    @Override
    public ParameterState filter(@NotNull String name, @NotNull ParameterState parameterState) {
        return switch (name) {
            case FILTER_TIME -> parameterState.getFilteredState(time.isPresent());
            case FILTER_FEATHER -> parameterState.getFilteredState(useFeather.isPresent());
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

    public Optional<Boolean> useFeather() {
        return useFeather;
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
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CallJump) obj;
        return Objects.equals(this.time, that.time) &&
                Objects.equals(this.useFeather, that.useFeather) &&
                Objects.equals(this.posX, that.posX) &&
                Objects.equals(this.posY, that.posY) &&
                Objects.equals(this.posZ, that.posZ) &&
                Objects.equals(this.lastReportedPitch, that.lastReportedPitch) &&
                Objects.equals(this.lastReportedYaw, that.lastReportedYaw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, useFeather, posX, posY, posZ, lastReportedPitch, lastReportedYaw);
    }

    @Override
    public String toString() {
        return "CallJump[" +
                "time=" + time + ", " +
                "useFeather=" + useFeather + ", " +
                "posX=" + posX + ", " +
                "posY=" + posY + ", " +
                "posZ=" + posZ + ", " +
                "lastReportedPitch=" + lastReportedPitch + ", " +
                "lastReportedYaw=" + lastReportedYaw + ']';
    }

}
