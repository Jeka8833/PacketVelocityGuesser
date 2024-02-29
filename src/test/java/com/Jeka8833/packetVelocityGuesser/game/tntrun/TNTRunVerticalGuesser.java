package com.Jeka8833.packetVelocityGuesser.game.tntrun;

import com.Jeka8833.packetVelocityGuesser.guesser.FoundedSolution;
import com.Jeka8833.packetVelocityGuesser.guesser.VerticalJump;
import com.Jeka8833.packetVelocityGuesser.guesser.input.InputConstant;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class TNTRunVerticalGuesser implements VerticalJump {
    private static final Map<String, InputConstant> JUMP_CONSTANT_MAP = getJumpConstantMap();

    private static Map<String, InputConstant> getJumpConstantMap() {
        var map = new HashMap<String, InputConstant>();
        for (int i = 1; i < 20; i++) {
            map.put("Jump " + i, TNTRunCalculation.getYConstant(i));
        }

        return Map.copyOf(map);
    }


    @Override
    public @NotNull Map<@NotNull String, @NotNull InputConstant> getExpectedValues() {
        return JUMP_CONSTANT_MAP;
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

    public static FoundedSolution[] filterDuplicatesPosition(FoundedSolution[] solutions) {
        HashSet<Filter<FoundedSolution>> filters = Arrays.stream(solutions)
                .filter(foundedSolution -> foundedSolution != null && foundedSolution.position() != null)
                .map(foundedSolution ->
                        new Filter<>(foundedSolution, foundedSolution.position().pitch().orElseThrow().doubleValue(),
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
