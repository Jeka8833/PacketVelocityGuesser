package com.Jeka8833.packetVelocityGuesser.composer;

import com.Jeka8833.packetVelocityGuesser.parser.packet.PlayerCamera;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RawJumpFilter {

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull RawJump @NotNull [] filterUncompletedJumps(@Nullable RawJump @NotNull [] database) {
        int databaseSize = database.length;

        int index = 0;
        RawJump[] list = new RawJump[databaseSize];

        for (int i = 0; i < databaseSize; i++) {
            RawJump rawJump = database[i];
            if (rawJump != null && rawJump.calls().length != 0 &&
                    rawJump.jumps().length != 0 && rawJump.positions().length != 0) {
                list[index++] = rawJump;
            }
        }

        return Arrays.copyOf(list, index);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull RawJump @NotNull [] filterDuplicates(@Nullable RawJump @NotNull [] database) {
        Collection<RawJump> set = new HashSet<>(database.length);
        for (RawJump rawJump : database) {
            if (rawJump != null) {
                set.add(rawJump);
            }
        }

        return set.toArray(new RawJump[0]);
    }

    public static @NotNull RawJump @NotNull [] filterZeroRotation(@Nullable RawJump @NotNull [] database) {
        return Arrays.stream(database)
                .parallel()
                .filter(rawJump -> rawJump != null && rawJump.positions().length > 0)
                .map(rawJump -> {
                    Collection<PlayerCamera> positions = new ArrayList<>(rawJump.positions().length);

                    float lastPitch = 0f;
                    float lastYaw = 0f;
                    for (PlayerCamera camera : rawJump.positions()) {
                        float pitch = camera.pitch().orElse(0f);
                        float yaw = camera.yaw().orElse(0f);

                        if (pitch != 0f || yaw != 0f) {
                            lastPitch = pitch;
                            lastYaw = yaw;
                        }

                        if (lastPitch != 0f || lastYaw != 0f) {
                            positions.add(new PlayerCamera(camera.time(), camera.x(), camera.y(), camera.z(),
                                    Optional.of(lastPitch), Optional.of(lastYaw), camera.onGround()));
                        }
                    }

                    return new RawJump(rawJump.file(), rawJump.calls(), rawJump.jumps(),
                            positions.toArray(new PlayerCamera[0]));
                })
                .toArray(RawJump[]::new);
    }

    public static @NotNull RawJump @NotNull [] filterFutureCamera(@Nullable RawJump @NotNull [] database) {
        return Arrays.stream(database)
                .parallel()
                .filter(rawJump -> rawJump != null && rawJump.positions().length > 0 && rawJump.calls().length > 0)
                .map(rawJump -> {
                    long lastCall = rawJump.calls()[0].time().orElseThrow();

                    Collection<PlayerCamera> positions = new ArrayList<>(rawJump.positions().length);

                    for (PlayerCamera camera : rawJump.positions()) {
                        if (camera.time().orElseThrow() > lastCall) continue;

                        positions.add(camera);
                    }

                    return new RawJump(rawJump.file(), rawJump.calls(), rawJump.jumps(),
                            positions.toArray(new PlayerCamera[0]));
                })
                .toArray(RawJump[]::new);
    }
}
