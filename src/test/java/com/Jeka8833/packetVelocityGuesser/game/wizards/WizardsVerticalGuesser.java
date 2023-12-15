package com.Jeka8833.packetVelocityGuesser.game.wizards;

import com.Jeka8833.packetVelocityGuesser.guesser.VerticalJump;
import com.Jeka8833.packetVelocityGuesser.guesser.input.InputConstant;
import com.Jeka8833.packetVelocityGuesser.guesser.input.InputGameConstants;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class WizardsVerticalGuesser implements VerticalJump {
    private static final Map<String, InputConstant> JUMP_CONSTANT_MAP = Map.of(
            "Default Jump", new InputGameConstants(
                    0.8961457366495107289147034575622101126840706761486442780247396720603011845913708106996752196104970594,
                    0.5732853960573859138444039920500764430400213521009186450168147390434495270341274890430060966369362871
            ),
            "Something 1", new InputGameConstants(
                    0.3113954165364490193053856492199018720942662251044442278611878972540791650115295973190678379649939143,
                    0.5732479834342725112690618898616265490682372402007269717755912503383985793045282648579984185214717232
            )
    );

    @NotNull
    @Override
    public Map<@NotNull String, @NotNull InputConstant> getExpectedValues() {
        return JUMP_CONSTANT_MAP;
    }
}
