package com.Jeka8833.packetVelocityGuesser.output;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class WolframMathematica {

    @NotNull
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <Value> String toTable(@NotNull Collection<@NotNull Value> values,
                                         @NotNull Function<@NotNull Value, @NotNull Object> x,
                                         @NotNull Function<@NotNull Value, @NotNull Object> y) {
        return toTable(values, x, y, e -> {
            throw new RuntimeException(e);
        });
    }

    @NotNull
    @Contract(value = "_, _, _ -> new", pure = true)
    public static <Value> String toTable(@NotNull Value @NotNull [] values,
                                         @NotNull Function<@NotNull Value, @NotNull Object> x,
                                         @NotNull Function<@NotNull Value, @NotNull Object> y) {
        return toTable(List.of(values), x, y, e -> {
            throw new RuntimeException(e);
        });
    }

    @NotNull
    @Contract(value = "_, _, _, _ -> new", pure = true)
    public static <Value> String toTable(@NotNull Collection<@Nullable Value> values,
                                         @NotNull Function<@Nullable Value, @Nullable Object> x,
                                         @NotNull Function<@Nullable Value, @Nullable Object> y,
                                         @Nullable Consumer<@NotNull Exception> exceptionConsumer) {
        Collection<Map.Entry<Object, Object>> entries = new ArrayList<>();
        for (Value value : values) {
            try {
                Object xValue = x.apply(value);
                if (xValue == null) throw new NullPointerException("xValue is null");

                Object yValue = y.apply(value);
                if (yValue == null) throw new NullPointerException("yValue is null");

                entries.add(Map.entry(xValue, yValue));
            } catch (Exception e) {
                if (exceptionConsumer != null)
                    exceptionConsumer.accept(e);
            }
        }

        return toTable(entries);
    }

    @NotNull
    @Contract(value = "_, _, _, _ -> new", pure = true)
    public static <Value> String toTable(@Nullable Value @NotNull [] values,
                                         @NotNull Function<@Nullable Value, @Nullable Object> x,
                                         @NotNull Function<@Nullable Value, @Nullable Object> y,
                                         @Nullable Consumer<@NotNull Exception> exceptionConsumer) {
        return toTable(List.of(values), x, y, exceptionConsumer);
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static String toTable(@NotNull Collection<Map.Entry<@NotNull Object, @NotNull Object>> entries) {
        //noinspection unchecked
        return toTable(entries.toArray(new Map.Entry[0]));
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static String toTable(@NotNull Map.Entry<@NotNull Object, @NotNull Object> @NotNull [] entries) {
        Object[][] result = new Object[entries.length][2];
        for (int i = 0; i < entries.length; i++) {
            result[i][0] = entries[i].getKey();
            result[i][1] = entries[i].getValue();
        }

        return toTable(result);
    }

    // ================================================================================================================

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static String toTable(@NotNull Object @NotNull [][] entries) {
        var stringJoiner = new StringJoiner(",", "{", "}");
        for (Object[] objects : entries) {
            stringJoiner.add(formatEntry(objects));
        }

        return stringJoiner.toString();
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    private static String formatEntry(@NotNull Object @NotNull [] items) {
        var stringJoiner = new StringJoiner(",", "{", "}");
        for (Object item : items) {
            stringJoiner.add(maxPrecision(item));
        }

        return stringJoiner.toString();
    }

    @Nullable
    @Contract(value = "!null -> new; null -> null", pure = true)
    private static String maxPrecision(@Nullable Object object) {
        switch (object) {
            case BigDecimal bigDecimal -> {
                return bigDecimal.toPlainString() + '`';
            }
            case Double d -> {
                return new BigDecimal(d).toPlainString() + '`';
            }
            case Float f -> {
                return new BigDecimal(f).toPlainString() + '`';
            }
            case null -> {
                return null;
            }
            default -> {
                return object.toString();
            }
        }
    }
}
