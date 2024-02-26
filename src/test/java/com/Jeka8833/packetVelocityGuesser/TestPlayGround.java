package com.Jeka8833.packetVelocityGuesser;

import com.Jeka8833.packetVelocityGuesser.composer.Composer;
import com.Jeka8833.packetVelocityGuesser.composer.RawJump;
import com.Jeka8833.packetVelocityGuesser.composer.RawJumpFilter;
import com.Jeka8833.packetVelocityGuesser.game.tntrun.TNTRunVerticalGuesser;
import com.Jeka8833.packetVelocityGuesser.guesser.FoundedSolution;
import com.Jeka8833.packetVelocityGuesser.guesser.Guesser;
import com.Jeka8833.packetVelocityGuesser.output.WolframMathematica;
import com.Jeka8833.packetVelocityGuesser.parser.CsvFileParser;
import com.Jeka8833.packetVelocityGuesser.parser.FilePackets;
import com.Jeka8833.packetVelocityGuesser.parser.ServerStorageFileParser;
import com.Jeka8833.packetVelocityGuesser.parser.filter.FileFilter;
import com.Jeka8833.packetVelocityGuesser.parser.filter.GameInfoFilter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TestPlayGround {

    private static final Path PATH = Path.of("D:\\User\\Download\\jumps\\");
    private static final ServerStorageFileParser SERVER_PARSER = new ServerStorageFileParser();
    private static final FileFilter PACKET_FILTER = FileFilter.create()
            .add(GameInfoFilter.create()
                    .mode()
                    .blockIfAbsent()
                    .require(ServerConstants.Hypixel.Mode.TNTRun)
                    .build()

                    .serverBrand()
                    .blockIfAbsent()
                    .require(ServerConstants.HYPIXEL_SERVER)
                    .build()

                    .build())
            .build();


    public static void main(String[] args) throws IOException, ExecutionException {
        Path[] files = CsvFileParser.getAllFilesInFolder(PATH);

        FilePackets[] packets = SERVER_PARSER.parseAllFiles(files, false);
        FilePackets[] filteredPackets = PACKET_FILTER.filter(packets);

        RawJump[] jumps = Composer.toRawJump(filteredPackets);
        RawJump[] filteredJumps = RawJumpFilter.filterUncompletedJumps(jumps);

        Guesser guesser = new Guesser(null, new TNTRunVerticalGuesser());
        FoundedSolution[] solutions = guesser.solveBest(filteredJumps);

        ArrayList<RawJump> need = new ArrayList<>();
        for (int i = 0; i < solutions.length; i++) {
            FoundedSolution solution = solutions[i];
            //if (solution == null || !("Jump 2".equals(solution.jumpName()) || "Jump 1".equals(solution.jumpName())|| "Jump 3".equals(solution.jumpName()))) {
/*                try {
                    if (solution.receiver().velY().orElse(0) == 8646) {
                        System.out.println("Jump 2: ");
                    }
                } catch (Exception e){
                }*/

                need.add(filteredJumps[i]);
            //}
        }

        FoundedSolution[] firstPredict = guesser.solveFirstVertical(need.toArray(new RawJump[0]));

        Map<String, List<FoundedSolution>> grouped = Arrays.stream(firstPredict)
                .collect(Collectors.groupingBy(foundedSolution ->
                        foundedSolution == null || foundedSolution.jumpName() == null ?
                                "Unknown" : foundedSolution.jumpName()));

        String all = WolframMathematica.toTable(firstPredict,
                v -> v.position().pitch().orElseThrow(),
                v -> v.receiver().velY().orElseThrow(), Throwable::printStackTrace);
        System.out.println("All table(" + firstPredict.length + "): " + all);

        grouped.forEach((s, foundedSolutions) -> {
            String table = WolframMathematica.toTable(foundedSolutions,
                    v -> v.position().pitch().orElseThrow(),
                    v -> v.receiver().velY().orElseThrow(), Throwable::printStackTrace);
            System.out.println("Table for " + s + "(" + foundedSolutions.size() + "): " + table);
        });
    }
}
