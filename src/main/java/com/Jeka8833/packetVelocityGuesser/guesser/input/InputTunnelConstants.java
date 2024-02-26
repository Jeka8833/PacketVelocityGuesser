package com.Jeka8833.packetVelocityGuesser.guesser.input;

public record InputTunnelConstants(double offset, double multiplier) implements InputConstant {
    @Override
    public double getOffsetTunnel() {
        return offset;
    }

    @Override
    public double getMultiplierTunnel() {
        return multiplier;
    }
}
