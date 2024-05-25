package com.Jeka8833.packetVelocityGuesser.guesser;

import com.Jeka8833.packetVelocityGuesser.composer.RawJump;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Function;

public record Guesser(@Nullable HorizontalJump horizontalGuesser, @Nullable VerticalJump verticalGuesser) {

    @Contract("null, null -> fail")
    public Guesser {
        if (horizontalGuesser == null && verticalGuesser == null)
            throw new IllegalArgumentException("Both guessers can't be null");
    }

    @Nullable
    @Contract("_ -> new")
    public FoundedSolution @NotNull [] solveBest(@NotNull RawJump @NotNull [] data) {
        var solutions = new FoundedSolution[data.length];
        for (int i = 0; i < data.length; i++) {
            RawJump rawJump = data[i];

            FoundedSolution vertical = verticalGuesser != null ?
                    verticalGuesser.findBestApproximation(rawJump) : null;
            FoundedSolution horizontal = horizontalGuesser != null ?
                    horizontalGuesser.findBestApproximation(rawJump) : null;

            if (vertical != null && horizontal != null) {
                solutions[i] = (vertical.error() < horizontal.error()) ? vertical : horizontal;
            } else {
                solutions[i] = (vertical != null) ? vertical : horizontal;
            }
        }
        return solutions;
    }

    @Nullable
    @Contract("_, _ -> new")
    public FoundedSolution @NotNull [] solveBest(@NotNull RawJump @NotNull [] data, double maxError) {
        var solutions = new FoundedSolution[data.length];
        for (int i = 0; i < data.length; i++) {
            RawJump rawJump = data[i];

            FoundedSolution vertical = verticalGuesser != null ?
                    verticalGuesser.findBestApproximation(rawJump, maxError) : null;
            FoundedSolution horizontal = horizontalGuesser != null ?
                    horizontalGuesser.findBestApproximation(rawJump, maxError) : null;

            if (vertical != null && horizontal != null) {
                solutions[i] = (vertical.error() < horizontal.error()) ? vertical : horizontal;
            } else {
                solutions[i] = (vertical != null) ? vertical : horizontal;
            }
        }
        return solutions;
    }

    @Nullable
    @Contract("_, _, _ -> new")
    public FoundedSolution @NotNull [] solveBestOrFirst(@NotNull RawJump @NotNull [] data,
                                                        double maxError, @NotNull String unknownName) {
        var solutions = new FoundedSolution[data.length];
        for (int i = 0; i < data.length; i++) {
            RawJump rawJump = data[i];

            FoundedSolution vertical = verticalGuesser != null ?
                    verticalGuesser.findBestApproximation(rawJump) : null;
            FoundedSolution horizontal = horizontalGuesser != null ?
                    horizontalGuesser.findBestApproximation(rawJump) : null;

            boolean isVertical = vertical != null && vertical.error() <= maxError;
            boolean isHorizontal = horizontal != null && horizontal.error() <= maxError;

            if (isVertical && isHorizontal) {
                solutions[i] = (vertical.error() < horizontal.error()) ? vertical : horizontal;
            } else if (isVertical) {
                solutions[i] = vertical;
            } else if (isHorizontal) {
                solutions[i] = horizontal;
            } else {
                vertical = verticalGuesser != null ? verticalGuesser.findFirst(rawJump) : null;
                horizontal = horizontalGuesser != null ? horizontalGuesser.findFirst(rawJump) : null;

                if (vertical != null && horizontal != null) {
                    solutions[i] = (vertical.error() < horizontal.error()) ? vertical : horizontal;
                } else if (vertical != null) {
                    solutions[i] = vertical;
                } else if (horizontal != null) {
                    solutions[i] = horizontal;
                } else {
                    continue;
                }

                solutions[i] = new FoundedSolution(unknownName, solutions[i].caller(),
                        solutions[i].receiver(), solutions[i].position(), solutions[i].error());
            }
        }
        return solutions;
    }

    @Nullable
    @Contract("_ -> new")
    public FoundedSolution @NotNull [] solveBestVertical(@NotNull RawJump @NotNull [] data) {
        if (verticalGuesser == null) throw new IllegalArgumentException("Vertical guesser is null");

        return find(data, verticalGuesser::findBestApproximation);
    }

    @Nullable
    @Contract("_ -> new")
    public FoundedSolution @NotNull [] solveBestHorizontal(@NotNull RawJump @NotNull [] data) {
        if (horizontalGuesser == null) throw new IllegalArgumentException("Horizontal guesser is null");

        return find(data, horizontalGuesser::findBestApproximation);
    }

    @Nullable
    @Contract("_, _ -> new")
    public FoundedSolution @NotNull [] solveBestVertical(@NotNull RawJump @NotNull [] data, double maxError) {
        if (verticalGuesser == null) throw new IllegalArgumentException("Vertical guesser is null");

        return find(data, rawJump -> verticalGuesser.findBestApproximation(rawJump, maxError));
    }

    @Nullable
    @Contract("_, _ -> new")
    public FoundedSolution @NotNull [] solveBestHorizontal(@NotNull RawJump @NotNull [] data, double maxError) {
        if (horizontalGuesser == null) throw new IllegalArgumentException("Horizontal guesser is null");

        return find(data, rawJump -> horizontalGuesser.findBestApproximation(rawJump, maxError));
    }

    @Nullable
    @Contract("_ -> new")
    public FoundedSolution @NotNull [] solveFirstVertical(@NotNull RawJump @NotNull [] data) {
        if (verticalGuesser == null) throw new IllegalArgumentException("Vertical guesser is null");

        return find(data, verticalGuesser::findFirst);
    }

    @Nullable
    @Contract("_ -> new")
    public FoundedSolution @NotNull [] solveFirstHorizontal(@NotNull RawJump @NotNull [] data) {
        if (horizontalGuesser == null) throw new IllegalArgumentException("Horizontal guesser is null");

        return find(data, horizontalGuesser::findFirst);
    }

    @Nullable
    @Contract("_, _, _ -> new")
    public FoundedSolution @NotNull [] solveBestOrFirstVertical(@NotNull RawJump @NotNull [] data,
                                                                double maxError, @NotNull String unknownName) {
        if (verticalGuesser == null) throw new IllegalArgumentException("Vertical guesser is null");

        return find(data, rawJump -> verticalGuesser.findBestOrFirst(rawJump, maxError, unknownName));
    }

    @Nullable
    @Contract("_, _, _ -> new")
    public FoundedSolution @NotNull [] solveBestOrFirstHorizontal(@NotNull RawJump @NotNull [] data,
                                                                  double maxError, @NotNull String unknownName) {
        if (horizontalGuesser == null) throw new IllegalArgumentException("Horizontal guesser is null");

        return find(data, rawJump -> horizontalGuesser.findBestOrFirst(rawJump, maxError, unknownName));
    }

    private static FoundedSolution[] find(RawJump[] data, Function<RawJump, FoundedSolution> function) {
        return Arrays.stream(data)
                .parallel()
                .map(function)
                .toArray(FoundedSolution[]::new);
    }
}
