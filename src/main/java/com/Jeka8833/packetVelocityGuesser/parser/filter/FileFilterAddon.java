package com.Jeka8833.packetVelocityGuesser.parser.filter;

import com.Jeka8833.packetVelocityGuesser.parser.FilePackets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This interface represents a file filter addon. Implementations of this interface can be used to filter file with packets.
 */
public interface FileFilterAddon {

    @Nullable
    FilePackets filter(@NotNull FilePackets filePackets);
}
