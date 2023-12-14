package com.Jeka8833.packetVelocityGuesser.guesser;

import com.Jeka8833.packetVelocityGuesser.parser.packet.PlayerCamera;
import com.Jeka8833.packetVelocityGuesser.parser.packet.ReceivedJump;
import org.jetbrains.annotations.NotNull;

public record MinErrorValue(@NotNull String name, double error,
                            @NotNull PlayerCamera playerCamera, @NotNull ReceivedJump jump) {
}
