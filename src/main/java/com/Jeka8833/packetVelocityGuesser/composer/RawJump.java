package com.Jeka8833.packetVelocityGuesser.composer;

import com.Jeka8833.packetVelocityGuesser.parser.packet.CallJump;
import com.Jeka8833.packetVelocityGuesser.parser.packet.PlayerCamera;
import com.Jeka8833.packetVelocityGuesser.parser.packet.ReceivedJump;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public record RawJump(@NotNull Path file,
                      @NotNull CallJump @NotNull [] calls,
                      @NotNull ReceivedJump @NotNull [] jumps,
                      @NotNull PlayerCamera @NotNull [] positions) {

    @Nullable
    public Pair<CallJump, Double> getCall(@NotNull PlayerCamera playerCamera) {
        Pair<CallJump, Double> minDelay = null;

        for (CallJump callJump : calls) {
            double delay = callJump.getTime(TimeUnit.NANOSECONDS) - playerCamera.getTime(TimeUnit.NANOSECONDS);
            if (delay <= 0) continue;

            if (minDelay == null || delay < minDelay.second()) {
                minDelay = new Pair<>(callJump, delay);
            }
        }

        return minDelay;
    }

    @Nullable
    public Pair<CallJump, Double> getCallOrFirstCall(@NotNull PlayerCamera playerCamera) {
        Pair<CallJump, Double> minDelay = getCall(playerCamera);

        if (minDelay == null && calls.length != 0) {
            double delay = calls[0].getTime(TimeUnit.NANOSECONDS) - playerCamera.getTime(TimeUnit.NANOSECONDS);

            return new Pair<>(calls[0], delay);
        }

        return minDelay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RawJump rawJump = (RawJump) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(calls, rawJump.calls)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(jumps, rawJump.jumps)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(positions, rawJump.positions);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(calls);
        result = 31 * result + Arrays.hashCode(jumps);
        result = 31 * result + Arrays.hashCode(positions);
        return result;
    }
}
