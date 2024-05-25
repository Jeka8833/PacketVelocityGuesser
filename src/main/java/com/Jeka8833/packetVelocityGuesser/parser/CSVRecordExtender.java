package com.Jeka8833.packetVelocityGuesser.parser;

import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public record CSVRecordExtender(@NotNull CSVRecord record) {

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public OptionalLong getLong(@NotNull String name) {
        Optional<String> value = tryGet(name);
        if (value.isEmpty()) return OptionalLong.empty();

        try {
            return OptionalLong.of(Long.parseLong(value.get()));
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public OptionalInt getInteger(@NotNull String name) {
        Optional<String> value = tryGet(name);
        if (value.isEmpty()) return OptionalInt.empty();

        try {
            return OptionalInt.of(Integer.parseInt(value.get()));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
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
    public OptionalDouble getDouble(@NotNull String name) {
        Optional<String> value = tryGet(name);
        if (value.isEmpty()) return OptionalDouble.empty();

        try {
            return OptionalDouble.of(new BigDecimal(value.get()).doubleValue());
        } catch (NumberFormatException e) {
            return OptionalDouble.empty();
        }
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public Optional<Float> getFloat(@NotNull String name) {
        Optional<String> value = tryGet(name);
        if (value.isEmpty()) return Optional.empty();

        try {
            return Optional.of(new BigDecimal(value.get()).floatValue());
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
