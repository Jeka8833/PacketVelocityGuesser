package com.Jeka8833.packetVelocityGuesser.guesser;

import com.Jeka8833.packetVelocityGuesser.parser.packet.CallJump;
import com.Jeka8833.packetVelocityGuesser.parser.packet.PlayerCamera;
import com.Jeka8833.packetVelocityGuesser.parser.packet.ReceivedJump;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

public record FoundedSolution(@Nullable String jumpName, @Nullable CallJump caller,
                              @Nullable ReceivedJump receiver, @Nullable PlayerCamera position, double error) {

    @Contract(pure = true)
    public OptionalLong getPingA(@NotNull TimeUnit timeUnit) {
        if (caller == null || receiver == null) return OptionalLong.empty();

        long time = receiver.time().orElseThrow() - caller.time().orElseThrow();
        if (time < 0) return OptionalLong.empty();

        return OptionalLong.of(timeUnit.convert(time, TimeUnit.NANOSECONDS));
    }

    public OptionalLong getPingB(@NotNull TimeUnit timeUnit) {
        if (position == null || receiver == null) return OptionalLong.empty();

        long time = receiver.time().orElseThrow() - position.time().orElseThrow();
        if (time < 0) return OptionalLong.empty();

        return OptionalLong.of(timeUnit.convert(time, TimeUnit.NANOSECONDS));
    }
}
