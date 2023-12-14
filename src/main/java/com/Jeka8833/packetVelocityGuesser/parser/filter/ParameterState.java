package com.Jeka8833.packetVelocityGuesser.parser.filter;

import org.jetbrains.annotations.NotNull;

/**
 * This enum represents the state of a parameter in a packet.
 * The states can be BLOCK, NONE, or REQUIRE.
 */
public enum ParameterState {

    /**
     * Represents a state where the parameter is blocked.
     */
    BLOCK,

    /**
     * Represents a state where the parameter is not required.
     */
    NONE,

    /**
     * Represents a state where the parameter is required.
     */
    REQUIRE;

    /**
     * Retrieves the filtered {@link ParameterState} based on the given condition.
     * If the current state is BLOCK, it returns NONE if the condition is true, otherwise it returns BLOCK.
     * If the current state is NONE, it always returns NONE.
     * If the current state is REQUIRE, it returns REQUIRE if the condition is true, otherwise it returns NONE.
     *
     * @param isPresent the condition to apply
     * @return the filtered {@link ParameterState}
     */
    @NotNull
    public ParameterState getFilteredState(boolean isPresent) {
        return switch (this) {
            case BLOCK -> isPresent ? NONE : BLOCK;
            case NONE -> NONE;
            case REQUIRE -> isPresent ? REQUIRE : NONE;
        };
    }
}
