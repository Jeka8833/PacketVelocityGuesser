package com.Jeka8833.packetVelocityGuesser.game.pvprun;

import com.Jeka8833.packetVelocityGuesser.guesser.FoundedSolution;
import com.Jeka8833.packetVelocityGuesser.guesser.VerticalJump;
import com.Jeka8833.packetVelocityGuesser.guesser.input.InputConstant;
import com.Jeka8833.packetVelocityGuesser.guesser.input.InputTunnelConstants;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class PVPRunVerticalGuesser implements VerticalJump {
    @Override
    public @NotNull Map<@NotNull String, @NotNull InputConstant> getExpectedValues() {
        // XZ OnGround: 3744
        // XZ InAir: 3744

        // Y Max: 10296
        // Y Min: 2808
        // Y Multiplier: (10296 - 2808) / 2 = 3744
        // Y Offset: 2808 + 3744 = 6552

        return Map.of("Main", new InputTunnelConstants(6552D, 3744D), "Hypixel Bug", new InputTunnelConstants(3359D, 0D));
    }

    public static FoundedSolution[] filterDuplicatesPositionAndResults(FoundedSolution[] solutions) {
        HashSet<Filter<FoundedSolution>> filters = Arrays.stream(solutions)
                .filter(foundedSolution -> foundedSolution != null && foundedSolution.position() != null && foundedSolution.receiver() != null)
                .map(foundedSolution ->
                        new Filter<>(foundedSolution, foundedSolution.position().pitch().orElseThrow().doubleValue(),
                                foundedSolution.receiver().velY().orElseThrow(),
                                foundedSolution.jumpName()))
                .collect(Collectors.toCollection(HashSet::new));

        return filters.stream().map(Filter::master).toArray(FoundedSolution[]::new);
    }


    private record Filter<Master>(Master master, Object... objects) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Filter<?> filter = (Filter<?>) o;

            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(objects, filter.objects);
        }

        public int hashCode() {
            return Arrays.hashCode(objects);
        }
    }
}
