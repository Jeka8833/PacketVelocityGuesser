package com.Jeka8833.packetVelocityGuesser.parser;

import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Optional;

public record CSVRecordExtender(@NotNull CSVRecord record) {

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public Optional<Long> getLong(@NotNull String name) {
        Optional<String> value = tryGet(name);
        if (value.isEmpty()) return Optional.empty();

        try {
            return Optional.of(Long.parseLong(value.get()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public Optional<Integer> getInteger(@NotNull String name) {
        Optional<String> value = tryGet(name);
        if (value.isEmpty()) return Optional.empty();

        try {
            return Optional.of(Integer.parseInt(value.get()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public Optional<Boolean> getBoolean(@NotNull String name) {
        return tryGet(name)
                .map(Boolean::parseBoolean);

    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public Optional<BigDecimal> getBigDecimal(@NotNull String name) {
        Optional<String> value = tryGet(name);
        if (value.isEmpty()) return Optional.empty();

        try {
            return Optional.of(new BigDecimal(value.get()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public Optional<String> tryGet(@NotNull String name) {
        if (!record.isSet(name)) return Optional.empty();

        String value = record.get(name);
        if (value == null) return Optional.empty();

        value = value.strip();
        if (value.isEmpty()) return Optional.empty();
        return Optional.of(value);
    }
}
