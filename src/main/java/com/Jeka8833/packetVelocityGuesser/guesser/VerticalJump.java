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
        double preCalcPitchSin = -playerCamera.pitchSin(); // Too expensive to calculate it every time
        double preCalcY = jump.velY().orElseThrow();

        double minError = Double.MAX_VALUE;
        String minErrorName = null;

        for (Map.Entry<String, InputConstant> entry : getExpectedValues().entrySet()) {
            InputConstant constant = entry.getValue();

            double error = Math.abs(Math.fma(preCalcPitchSin, constant.getMultiplierTunnel(),
                    constant.getOffsetTunnel() - preCalcY));

            if (error < minError) {
                minError = error;
                minErrorName = entry.getKey();
            }
        }

        if (minErrorName == null) return null;

        return new MinErrorValue(minErrorName, minError, playerCamera, jump);
    }
}
