package com.Jeka8833.packetVelocityGuesser.guesser;

import com.Jeka8833.packetVelocityGuesser.guesser.input.InputConstant;
import com.Jeka8833.packetVelocityGuesser.parser.packet.PlayerCamera;
import com.Jeka8833.packetVelocityGuesser.parser.packet.ReceivedJump;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface HorizontalJump extends ResultDeviation {

    @Nullable
    @Override
    default MinErrorValue getMinError(@NotNull PlayerCamera playerCamera, @NotNull ReceivedJump jump) {
        MinErrorValue minError = null;

        for (Map.Entry<String, InputConstant> entry : getExpectedValues().entrySet()) {
            InputConstant constant = entry.getValue();

            double errorX = constant.getOffsetTunnel() -
                    playerCamera.yawSin() * constant.getMultiplierTunnel() - jump.velX().orElseThrow();
            double errorY = constant.getOffsetTunnel() -
                    playerCamera.yawCos() * constant.getMultiplierTunnel() - jump.velY().orElseThrow();

            double error = errorX * errorX + errorY * errorY;

            if (minError == null || error < minError.error()) {
                minError = new MinErrorValue(entry.getKey(), error, playerCamera, jump);
            }
        }

        if (minError == null) return null;

        return new MinErrorValue(minError.name(), Math.sqrt(minError.error()), minError.playerCamera(), minError.jump());
    }
}
