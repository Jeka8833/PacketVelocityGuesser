package com.Jeka8833.packetVelocityGuesser.guesser;

import com.Jeka8833.packetVelocityGuesser.guesser.input.InputConstant;
import com.Jeka8833.packetVelocityGuesser.parser.packet.PlayerCamera;
import com.Jeka8833.packetVelocityGuesser.parser.packet.ReceivedJump;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface VerticalJump extends ResultDeviation {

    @Nullable
    @Override
    default MinErrorValue getMinError(@NotNull PlayerCamera playerCamera, @NotNull ReceivedJump jump) {
        MinErrorValue minError = null;

        for (Map.Entry<String, InputConstant> entry : getExpectedValues().entrySet()) {
            InputConstant constant = entry.getValue();

            double error;
            if (constant.isTunnel()) {
                error = Math.abs(constant.getOffsetTunnel() -
                        playerCamera.pitchSin() * constant.getMultiplierTunnel() - jump.velY().orElseThrow());
            } else {
                error = Math.abs(constant.getOffsetEngine() -
                        playerCamera.pitchSin() * constant.getMultiplierEngine() - jump.engineVelY());
            }

            if (minError == null || error < minError.error()) {
                minError = new MinErrorValue(entry.getKey(), error, playerCamera, jump);
            }
        }

        return minError;
    }
}
