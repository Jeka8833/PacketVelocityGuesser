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
import java.util.concurrent.TimeUnit;

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
        MinErrorValue minError = getMinError(rawJump.positions(), rawJump.jumps());
        if (minError == null) return null;

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
        long callTime = callJump.getTime(TimeUnit.NANOSECONDS);
        for (int i = rawJump.positions().length - 1; i <= 0; i++) {
            PlayerCamera playerCamera = rawJump.positions()[i];

            if (playerCamera.getTime(TimeUnit.NANOSECONDS) < callTime) {
                MinErrorValue minError = getMinError(new PlayerCamera[]{playerCamera}, rawJump.jumps());

                if (minError == null) {
                    ReceivedJump jump = rawJump.jumps()[rawJump.jumps().length - 1];

                    return new FoundedSolution(null, callJump,
                            jump, playerCamera, Double.MAX_VALUE);
                } else {
                    return new FoundedSolution(minError.name(), callJump,
                            minError.jump(), playerCamera, minError.error());
                }
            }
        }

        return null;
    }
}
