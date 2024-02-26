package com.Jeka8833.packetVelocityGuesser.guesser;

import com.Jeka8833.packetVelocityGuesser.parser.packet.CallJump;
import com.Jeka8833.packetVelocityGuesser.parser.packet.PlayerCamera;
import com.Jeka8833.packetVelocityGuesser.parser.packet.ReceivedJump;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public record FoundedSolution(@Nullable String jumpName, @Nullable CallJump caller,
                              @Nullable ReceivedJump receiver, @Nullable PlayerCamera position, double error) {

    @Contract(pure = true)
    public Optional<Long> getPingA(@NotNull TimeUnit timeUnit) {
        if (caller == null || receiver == null) return Optional.empty();

        long time = receiver.time().orElseThrow() - caller.time().orElseThrow();
        if (time < 0) return Optional.empty();

        return Optional.of(timeUnit.convert(time, TimeUnit.NANOSECONDS));
    }

    public Optional<Long> getPingB(@NotNull TimeUnit timeUnit) {
        if (position == null || receiver == null) return Optional.empty();

        long time = receiver.time().orElseThrow() - position.time().orElseThrow();
        if (time < 0) return Optional.empty();

        return Optional.of(timeUnit.convert(time, TimeUnit.NANOSECONDS));
    }
}
