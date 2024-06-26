package com.Jeka8833.packetVelocityGuesser.guesser;

import com.Jeka8833.packetVelocityGuesser.composer.Pair;
import com.Jeka8833.packetVelocityGuesser.composer.RawJump;
import com.Jeka8833.packetVelocityGuesser.guesser.input.InputConstant;
import com.Jeka8833.packetVelocityGuesser.parser.packet.CallJump;
import com.Jeka8833.packetVelocityGuesser.parser.packet.PlayerCamera;
import com.Jeka8833.packetVelocityGuesser.parser.packet.ReceivedJump;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ResultDeviation {

    @NotNull
    Map<@NotNull String, @NotNull InputConstant> getExpectedValues();

    @Nullable
    MinErrorValue getMinError(@NotNull PlayerCamera playerCamera, @NotNull ReceivedJump jump);

    @Nullable
    default MinErrorValue getMinError(@NotNull PlayerCamera @NotNull [] playerCameras,
                                      @NotNull ReceivedJump @NotNull [] receivedJumps) {
        MinErrorValue minError = null;

        for (PlayerCamera playerCamera : playerCameras) {
            for (ReceivedJump receivedJump : receivedJumps) {
                MinErrorValue error = getMinError(playerCamera, receivedJump);
                if (error == null) continue;

                if (minError == null || error.error() < minError.error())
                    minError = error;
            }
        }

        return minError;
    }

    @Nullable
    default FoundedSolution findBestApproximation(@NotNull RawJump rawJump) {
        return findBestApproximation(rawJump, Double.MAX_VALUE);
    }

    @Nullable
    default FoundedSolution findBestApproximation(@NotNull RawJump rawJump, double maxError) {
        MinErrorValue minError = getMinError(rawJump.positions(), rawJump.jumps());
        if (minError == null || minError.error() >= maxError) return null;

        Pair<CallJump, Double> callJump = rawJump.getCall(minError.playerCamera());
        if (callJump == null) {
            return new FoundedSolution(minError.name(), null,
                    minError.jump(), minError.playerCamera(), minError.error());
        } else {
            return new FoundedSolution(minError.name(), callJump.first(),
                    minError.jump(), minError.playerCamera(), minError.error());
        }
    }

    @Nullable
    default FoundedSolution findFirst(@NotNull RawJump rawJump) {
        if (rawJump.calls().length == 0 || rawJump.jumps().length == 0) return null;

        CallJump callJump = rawJump.calls()[0];
        long callTime = callJump.time().orElseThrow();

        long errorTime = Long.MAX_VALUE;
        PlayerCamera firstPosition = null;
        for (PlayerCamera playerCamera : rawJump.positions()) {
            long time = callTime - playerCamera.time().orElseThrow();
            if (time >= 0 && time < errorTime) {
                errorTime = time;
                firstPosition = playerCamera;
                break;
            }
        }

        if (firstPosition == null) return null;
        MinErrorValue minError = getMinError(new PlayerCamera[]{firstPosition}, rawJump.jumps());

        if (minError == null) {
            ReceivedJump jump = rawJump.jumps()[rawJump.jumps().length - 1];

            return new FoundedSolution(null, callJump, jump, firstPosition, Double.MAX_VALUE);
        } else {
            return new FoundedSolution(minError.name(), callJump, minError.jump(), firstPosition, minError.error());
        }
    }

    @Nullable
    default FoundedSolution findBestOrFirst(@NotNull RawJump rawJump, double minError, @NotNull String unknownName) {
        FoundedSolution solution = findBestApproximation(rawJump);
        if (solution != null && solution.error() < minError) return solution;

        solution = findFirst(rawJump);

        if (solution == null) return null;

        return new FoundedSolution(unknownName, solution.caller(),
                solution.receiver(), solution.position(), solution.error());
    }
}
