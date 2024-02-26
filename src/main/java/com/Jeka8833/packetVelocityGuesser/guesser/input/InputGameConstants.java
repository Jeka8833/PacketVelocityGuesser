package com.Jeka8833.packetVelocityGuesser.guesser.input;

public record InputGameConstants(double offset, double multiplier) implements InputConstant {
    private static final double TUNNEL_MULTIPLIER = 8000;

    @Override
    public double getOffsetTunnel() {
        return offset / TUNNEL_MULTIPLIER;
    }

    @Override
    public double getMultiplierTunnel() {
        return multiplier / TUNNEL_MULTIPLIER;
    }
}
