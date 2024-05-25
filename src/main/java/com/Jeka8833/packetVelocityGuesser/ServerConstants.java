package com.Jeka8833.packetVelocityGuesser;

public final class ServerConstants {

    public static final String HYPIXEL_SERVER = "Hypixel";
    public static final String TNTRUN_SERVER = "Odyssey";

    private ServerConstants() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    public static final class Hypixel {
        private Hypixel() {
            throw new UnsupportedOperationException("This class cannot be instantiated");
        }

        public static final class Mode {
            private Mode() {
                throw new UnsupportedOperationException("This class cannot be instantiated");
            }

            public static final String WIZARDS = "Wizards";
            public static final String TNTRun = "TNT Run";
            public static final String PVPRun = "PVP Run";
            public static final String BowSpleef = "Bow Spleef";
            public static final String BowSpleefDuel = "Bow Spleef Duel";
            public static final String TNTTag = "TNT Tag";
        }
    }
}
