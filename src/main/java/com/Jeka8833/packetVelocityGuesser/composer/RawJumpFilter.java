package com.Jeka8833.packetVelocityGuesser.composer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class RawJumpFilter {

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull RawJump @NotNull [] filterUncompletedJumps(@Nullable RawJump @NotNull [] database) {
        return Arrays.stream(database)
                .filter(Objects::nonNull)
                .filter(rawJump -> rawJump.calls().length != 0 &&
                        rawJump.jumps().length != 0 && rawJump.positions().length != 0)
                .toArray(RawJump[]::new);
    }


    @Contract(value = "_ -> new", pure = true)
    public static @NotNull RawJump @NotNull [] filterDuplicates(@Nullable RawJump @NotNull [] database) {
        var set = new HashSet<>(Arrays.asList(database));

        return set.stream().filter(Objects::nonNull).toArray(RawJump[]::new);
    }
}
