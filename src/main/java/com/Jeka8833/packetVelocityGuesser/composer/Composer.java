package com.Jeka8833.packetVelocityGuesser.composer;

import com.Jeka8833.packetVelocityGuesser.parser.FilePackets;
import com.Jeka8833.packetVelocityGuesser.parser.packet.CallJump;
import com.Jeka8833.packetVelocityGuesser.parser.packet.Packet;
import com.Jeka8833.packetVelocityGuesser.parser.packet.PlayerCamera;
import com.Jeka8833.packetVelocityGuesser.parser.packet.ReceivedJump;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

public class Composer {

    @NotNull
    @Contract("_ -> new")
    public static ReceivedJump @NotNull [] toReceivedJump(@NotNull FilePackets... files) {
        Collection<ReceivedJump> jumps = new ArrayList<>();

        for (FilePackets file : files) {
            for (Packet packet : file.packets()) {
                if (packet instanceof ReceivedJump receivedJump)
                    jumps.add(receivedJump);
            }
        }
        return jumps.toArray(ReceivedJump[]::new);
    }

    @NotNull
    @Contract("_ -> new")
    public static RawJump @NotNull [] toRawJump(@NotNull FilePackets... files) {
        Collection<RawJump> rawJumps = new ArrayList<>();

        for (FilePackets file : files) {
            Collection<CallJump> calls = new ArrayList<>();
            Collection<PlayerCamera> positions = new ArrayList<>();
            Collection<ReceivedJump> jumps = new ArrayList<>();

            boolean isWaitingNextJump = false;
            for (Packet packet : file.packets()) {
                switch (packet) {
                    case CallJump jump -> {
                        if (isWaitingNextJump)
                            rawJumps.add(prepareAndClearCollections(file.file(), calls, jumps, positions));

                        isWaitingNextJump = false;

                        calls.add(jump);
                    }

                    case PlayerCamera playerCamera -> positions.add(playerCamera);

                    case ReceivedJump receivedJump -> {
                        isWaitingNextJump = true;

                        jumps.add(receivedJump);
                    }

                    default -> {
                        // Ignore
                    }
                }
            }
            if (isWaitingNextJump)
                rawJumps.add(prepareAndClearCollections(file.file(), calls, jumps, positions));
        }
        return rawJumps.toArray(RawJump[]::new);
    }

    @NotNull
    @Contract("_, _, _, _ -> new")
    private static RawJump prepareAndClearCollections(@NotNull Path path,
                                                      @NotNull Collection<@NotNull CallJump> calls,
                                                      @NotNull Collection<@NotNull ReceivedJump> jumps,
                                                      @NotNull Collection<@NotNull PlayerCamera> positions) {
        var rawJump = new RawJump(path, calls.toArray(CallJump[]::new), jumps.toArray(ReceivedJump[]::new),
                positions.toArray(PlayerCamera[]::new));

        calls.clear();
        jumps.clear();
        positions.clear();

        return rawJump;
    }
}
