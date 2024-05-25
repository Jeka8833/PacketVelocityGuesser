package com.Jeka8833.packetVelocityGuesser.game.bowspleef;

import com.Jeka8833.packetVelocityGuesser.guesser.VerticalJump;
import com.Jeka8833.packetVelocityGuesser.guesser.input.InputConstant;
import com.Jeka8833.packetVelocityGuesser.guesser.input.InputTunnelConstants;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BowSpleefVerticalGuesser implements VerticalJump {
    @Override
    public @NotNull Map<@NotNull String, @NotNull InputConstant> getExpectedValues() {
        // Y Max: 10400
        // Y Min: 800
        // Y Multiplier: (10400 - 800) / 2 = 4800
        // Y Offset: (10400 + 800) / 2 = 5600

        return Map.of("Main", new InputTunnelConstants(5600, 4800));
    }
}
