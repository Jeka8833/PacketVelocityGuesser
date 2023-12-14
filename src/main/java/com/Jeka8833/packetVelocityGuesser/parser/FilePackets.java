package com.Jeka8833.packetVelocityGuesser.parser;

import com.Jeka8833.packetVelocityGuesser.parser.packet.Packet;

import java.nio.file.Path;

public record FilePackets(Path file, Packet[] packets) {
}
