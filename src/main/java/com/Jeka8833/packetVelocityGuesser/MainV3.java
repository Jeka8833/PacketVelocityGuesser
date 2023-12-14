package com.Jeka8833.packetVelocityGuesser;

import com.Jeka8833.packetVelocityGuesser.composer.Composer;
import com.Jeka8833.packetVelocityGuesser.composer.RawJump;
import com.Jeka8833.packetVelocityGuesser.filter.DatabaseFilter;
import com.Jeka8833.packetVelocityGuesser.game.wizards.WizardsVerticalGuesser;
import com.Jeka8833.packetVelocityGuesser.guesser.FoundedSolution;
import com.Jeka8833.packetVelocityGuesser.guesser.Guesser;
import com.Jeka8833.packetVelocityGuesser.output.WolframMathematica;
import com.Jeka8833.packetVelocityGuesser.parser.FilePackets;
import com.Jeka8833.packetVelocityGuesser.parser.FileParser;
import com.Jeka8833.packetVelocityGuesser.parser.filter.FileFilter;
import com.Jeka8833.packetVelocityGuesser.parser.filter.GameInfoFilter;
import com.Jeka8833.packetVelocityGuesser.parser.filter.ParameterFilter;
import com.Jeka8833.packetVelocityGuesser.parser.filter.VersionFilter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainV3 {

    private static final Path PATH = TNTClient.getRecorderPath();
    private static final FileParser FILE_PARSER = new FileParser();
    private static final FileFilter PACKET_FILTER = FileFilter.create()
            .add(ParameterFilter.create()
                    .allowIfAbsent()
                    .build())
            .add(VersionFilter.create()
                    .allowIfAbsent()
                    .build())
            .add(GameInfoFilter.create()

                    .mode()
                    .blockIfAbsent()
                    .require("Wizards")
                    .build()

                    .build())
            .build();

    public static void main(String[] args) throws IOException, ExecutionException {
        Path[] files = FileParser.getAllFilesInFolder(PATH);
        FilePackets[] packets = FILE_PARSER.parseAllFiles(files, false);

        FilePackets[] filteredPackets = PACKET_FILTER.filter(packets);

        RawJump[] jumps = Composer.toRawJump(filteredPackets);
        Collection<RawJump> filteredJumps = DatabaseFilter.filterDuplicates(
                DatabaseFilter.filterUncompletedJumps(List.of(jumps)));


        Guesser guesser = new Guesser(null, new WizardsVerticalGuesser());
        Collection<@Nullable FoundedSolution> solutions = guesser.solveVertical(filteredJumps);

        String table = WolframMathematica.toTable(solutions,
                v -> v.position().pitch().orElseThrow(),
                v -> v.receiver().velY().orElseThrow(), Throwable::printStackTrace);

        System.out.println("Result: " + table);
    }


}
