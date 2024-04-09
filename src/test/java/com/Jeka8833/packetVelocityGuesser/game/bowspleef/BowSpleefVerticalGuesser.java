package com.Jeka8833.packetVelocityGuesser.game.bowspleef;

import com.Jeka8833.packetVelocityGuesser.guesser.VerticalJump;
import com.Jeka8833.packetVelocityGuesser.guesser.input.InputConstant;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class BowSpleefVerticalGuesser implements VerticalJump {
    @Override
    public @NotNull Map<@NotNull String, @NotNull InputConstant> getExpectedValues() {
        return Collections.emptyMap();
    }
}
