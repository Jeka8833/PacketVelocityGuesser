package com.Jeka8833.packetVelocityGuesser.filter;

import com.Jeka8833.packetVelocityGuesser.composer.RawJump;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class DatabaseFilter {

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Collection<RawJump> filterUncompletedJumps(@NotNull Collection<@Nullable RawJump> database) {
        return database.stream()
                .filter(Objects::nonNull)
                .filter(rawJump -> rawJump.calls().length != 0 &&
                        rawJump.jumps().length != 0 && rawJump.positions().length != 0)
                .toList();
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Collection<RawJump> filterDuplicates(@NotNull Collection<@Nullable RawJump> database) {
        return new HashSet<>(database);
    }
}
