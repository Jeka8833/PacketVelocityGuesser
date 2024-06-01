package com.Jeka8833.packetVelocityGuesser.parser.packet;

import com.Jeka8833.packetVelocityGuesser.parser.CSVRecordExtender;
import com.Jeka8833.packetVelocityGuesser.parser.filter.ParameterState;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.OptionalLong;

public record GameInfo(OptionalLong time, Optional<String> serverBrand, Optional<String> gametype,
                       Optional<String> map, Optional<String> mode) implements Packet {

    public static final String FILTER_TIME = "gameinfo.serverbrand";
    public static final String FILTER_GAME_TYPE = "gameinfo.gametype";
    public static final String FILTER_MAP = "gameinfo.map";
    public static final String FILTER_MODE = "gameinfo.mode";

    @SuppressWarnings("unused")
    public GameInfo(@NotNull CSVRecordExtender record) {
        this(record.getLong("Time"),
                record.tryGet("ServerBrand"),
                record.tryGet("GameType"),
                record.tryGet("Map"),
                record.tryGet("Mode"));
    }

    @SuppressWarnings("unused")
    public GameInfo(DataInputStream stream, int packetID) throws IOException {
        this(OptionalLong.of(stream.readLong()),
                Optional.of(stream.readUTF()),
                Optional.of(stream.readUTF()),
                Optional.of(stream.readUTF()),
                Optional.of(stream.readUTF()));
    }

    @NotNull
    @Override
    public ParameterState filter(@NotNull String name, @NotNull ParameterState parameterState) {
        return switch (name) {
            case FILTER_TIME -> parameterState.getFilteredState(serverBrand.isPresent());
            case FILTER_GAME_TYPE -> parameterState.getFilteredState(gametype.isPresent());
            case FILTER_MAP -> parameterState.getFilteredState(map.isPresent());
            case FILTER_MODE -> parameterState.getFilteredState(mode.isPresent());
            default -> ParameterState.NONE;
        };
    }
}
