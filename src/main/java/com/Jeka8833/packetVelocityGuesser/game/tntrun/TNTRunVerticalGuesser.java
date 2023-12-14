package com.Jeka8833.packetVelocityGuesser.game.tntrun;

import com.Jeka8833.packetVelocityGuesser.guesser.VerticalJump;
import com.Jeka8833.packetVelocityGuesser.guesser.input.InputConstant;
import com.Jeka8833.packetVelocityGuesser.guesser.input.InputGameConstants;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class TNTRunVerticalGuesser implements VerticalJump {
    private static final Map<String, InputConstant> JUMP_CONSTANT_MAP = Map.of(
            "Jump 1", new InputGameConstants(
                    0.8215273728793346847902513720066839544950617831445956371227937403165221839055929748453689609430088013,
                    0.4651420061975349405911669188074268028226939383343920831134453826538030257987501282504614572306789106
            ),
            "Jump 2", new InputGameConstants(
                    0.7241264389237547442706191632721554541398124379077324020336042615464092056212764836688938083036336101,
                    0.4586623701636597952285527981637714797530828377905689283705737424513359978441649367255180359945405024
            ),
            "Jump 3", new InputGameConstants(
                    0.6312625327123864544085288195830709117760520323845223102204421913461842993839553585343476703771084850,
                    0.4494867778173909662938779494870700371181782083793382814183132906723938865591915726855758320102079373
            ),
            "Jump 4", new InputGameConstants(
                    0.5403980359731432421357581581179476117988336220159667597155459342606996654272046263572863534178238711,
                    0.4404322519385349960099312640977171666618569206279300772692788323181994800713155235951827511618072483
            )
    );

    @Override
    public @NotNull Map<@NotNull String, @NotNull InputConstant> getExpectedValues() {
        return JUMP_CONSTANT_MAP;
    }
}
