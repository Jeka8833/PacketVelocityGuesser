package com.Jeka8833.packetVelocityGuesser.guesser;

import com.Jeka8833.packetVelocityGuesser.composer.RawJump;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public record Guesser(HorizontalJump horizontalGuesser, VerticalJump verticalGuesser) {

    public Guesser {
        if (horizontalGuesser == null && verticalGuesser == null)
            throw new IllegalArgumentException("Both guessers can't be null");
    }

    public Collection<@Nullable FoundedSolution> solveVertical(@NotNull Collection<RawJump> data) {
        Collection<FoundedSolution> solutions = new ArrayList<>();
        for (RawJump rawJump : data) {
            solutions.add(verticalGuesser.findBestApproximation(rawJump));
        }
        return solutions;
    }
}
