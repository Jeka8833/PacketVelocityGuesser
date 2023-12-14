package com.Jeka8833.packetVelocityGuesser.guesser.input;

public record InputTunnelConstants(double offset, double multiplier) implements InputConstant {
    private static final double TUNNEL_MULTIPLIER = 8000;

    @Override
    public boolean isTunnel() {
        return true;
    }

    @Override
    public double getOffsetTunnel() {
        return offset;
    }

    @Override
    public double getMultiplierTunnel() {
        return multiplier;
    }

    @Override
    public double getOffsetEngine() {
        return offset / TUNNEL_MULTIPLIER;
    }

    @Override
    public double getMultiplierEngine() {
        return multiplier / TUNNEL_MULTIPLIER;
    }
}
