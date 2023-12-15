package com.Jeka8833.packetVelocityGuesser.composer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class RawJumpFilter {

    @Contract(value = "_ -> new", pure = true)
    public static @Nullable RawJump @NotNull [] filterUncompletedJumps(@Nullable RawJump @NotNull [] database) {
        return Arrays.stream(database)
                .filter(Objects::nonNull)
                .filter(rawJump -> rawJump.calls().length != 0 &&
                        rawJump.jumps().length != 0 && rawJump.positions().length != 0)
                .toArray(RawJump[]::new);
    }


    @Contract(value = "_ -> new", pure = true)
    public static @Nullable RawJump @NotNull [] filterDuplicates(@Nullable RawJump @NotNull [] database) {
        var set = new HashSet<>(List.of(database));

        return set.toArray(RawJump[]::new);
    }
}
