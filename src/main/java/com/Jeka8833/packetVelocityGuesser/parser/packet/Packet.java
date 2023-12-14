package com.Jeka8833.packetVelocityGuesser.parser.packet;

import com.Jeka8833.packetVelocityGuesser.parser.filter.ParameterState;
import org.jetbrains.annotations.NotNull;

public interface Packet {
    @NotNull
    ParameterState filter(@NotNull String name, @NotNull ParameterState parameterState);
}
