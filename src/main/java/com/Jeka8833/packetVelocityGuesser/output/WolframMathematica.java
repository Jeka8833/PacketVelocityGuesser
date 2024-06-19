package com.Jeka8833.packetVelocityGuesser.output;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public final class WolframMathematica {
    private final Collection<Object> objects = Collections.synchronizedCollection(new ArrayList<>());

    public WolframMathematica addVector(Collection<Object> objects) {
        this.objects.add(objects);

        return this;
    }

    public WolframMathematica addVector(Object... objects) {
        this.objects.add(objects);

        return this;
    }


    @SafeVarargs
    public final <Type> WolframMathematica processAndAddArray(@NotNull Function<Type, Object[]> process,
                                                              Type... objects) {
        for (Type object : objects) {
            addVector(process.apply(object));
        }

        return this;
    }

    @SafeVarargs
    public final <Type> WolframMathematica processAndAddArray(@NotNull Function<Type, Object[]> process,
                                                              @Nullable Consumer<@NotNull Exception> exceptionConsumer,
                                                              Type... objects) {
        for (Type object : objects) {
            try {
                addVector(process.apply(object));
            } catch (Exception e) {
                if (exceptionConsumer != null) {
                    exceptionConsumer.accept(e);
                }
            }
        }

        return this;
    }

    public <Type> WolframMathematica processAndAddArray(@NotNull Function<Type, Object[]> process,
                                                        Iterable<Type> objects) {
        for (Type object : objects) {
            addVector(process.apply(object));
        }

        return this;
    }

    public <Type> WolframMathematica processAndAddArray(@NotNull Function<Type, Object[]> process,
                                                        @Nullable Consumer<@NotNull Exception> exceptionConsumer,
                                                        Iterable<Type> objects) {
        for (Type object : objects) {
            try {
                addVector(process.apply(object));
            } catch (Exception e) {
                if (exceptionConsumer != null) {
                    exceptionConsumer.accept(e);
                }
            }
        }

        return this;
    }

    public WolframMathematica addArray(Object... objects) {
        for (Object object : objects) {
            addVector(object);
        }

        return this;
    }

    public WolframMathematica addArray(Iterable<Object> objects) {
        for (Object object : objects) {
            addVector(object);
        }

        return this;
    }

    public int getArraySize() {
        return objects.size();
    }

    @Override
    public String toString() {
        return toWolframLanguage(objects);
    }

    public WolframMathematica export(String path) throws IOException {
        return export(Path.of(path));
    }

    public WolframMathematica export(Path path) throws IOException {
        Files.writeString(path, toString());

        return this;
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static String toWolframLanguage(@Nullable Object obj) {
        if (obj == null) return maxPrecision(null);

        var stringJoiner = new StringJoiner(",", "{", "}");

        if (obj instanceof Iterable<?> iterable) {
            Iterator<?> iterator = iterable.iterator();
            if (!iterator.hasNext()) return maxPrecision(null);

            Object first = iterator.next();
            if (iterator.hasNext()) {
                stringJoiner.add(toWolframLanguage(first));

                while (iterator.hasNext()) stringJoiner.add(toWolframLanguage(iterator.next()));

                return stringJoiner.toString();
            }

            return toWolframLanguage(first);
        } else if (obj.getClass().isArray()) {
            int length = Array.getLength(obj);
            if (length == 0) return maxPrecision(null);
            if (length == 1) return toWolframLanguage(Array.get(obj, 0));

            for (int i = 0; i < length; i++) {
                stringJoiner.add(toWolframLanguage(Array.get(obj, i)));
            }

            return stringJoiner.toString();
        }

        return maxPrecision(obj);
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    private static String maxPrecision(@Nullable Object object) {
        switch (object) {
            case BigDecimal bigDecimal -> {
                String textValue = bigDecimal.toPlainString();
                if (textValue.contains(".")) return textValue + '`';

                return textValue;
            }
            case Double d -> {
                String textValue = new BigDecimal(d).toPlainString();
                if (textValue.contains(".")) return textValue + '`';

                return textValue;
            }
            case Float f -> {
                String textValue = new BigDecimal(f).toPlainString();
                if (textValue.contains(".")) return textValue + '`';

                return textValue;
            }
            case Boolean b -> {
                return b ? "True" : "False";
            }
            case null -> {
                return "Null";
            }
            default -> {
                return object.toString();
            }
        }
    }
}
