package com.Jeka8833.packetVelocityGuesser.guesser;

import com.Jeka8833.packetVelocityGuesser.guesser.input.InputConstant;
import com.Jeka8833.packetVelocityGuesser.parser.packet.PlayerCamera;
import com.Jeka8833.packetVelocityGuesser.parser.packet.ReceivedJump;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface VerticalJump extends ResultDeviation {

    long MINIMAL_PING = TimeUnit.MILLISECONDS.toNanos(15);

    @Nullable
    @Override
    default MinErrorValue getMinError(@NotNull PlayerCamera playerCamera, @NotNull ReceivedJump jump) {
        if (jump.time().orElseThrow() - playerCamera.time().orElseThrow() < MINIMAL_PING) return null;

        MinErrorValue minError = null;

        for (Map.Entry<String, InputConstant> entry : getExpectedValues().entrySet()) {
            InputConstant constant = entry.getValue();

            double error = Math.abs(constant.getOffsetTunnel() -
                    playerCamera.pitchSin() * constant.getMultiplierTunnel() - jump.velY().orElseThrow());

            if (minError == null || error < minError.error()) {
                minError = new MinErrorValue(entry.getKey(), error, playerCamera, jump);
            }
        }

        return minError;
    }
}
