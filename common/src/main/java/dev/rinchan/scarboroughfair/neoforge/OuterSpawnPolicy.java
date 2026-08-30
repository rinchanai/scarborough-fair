package dev.rinchan.scarboroughfair.neoforge;

import java.util.UUID;

/** Pure deterministic policy for outer-island spawn candidates. */
public final class OuterSpawnPolicy {
    public static final int MINIMUM_RADIUS = 1_024;

    private OuterSpawnPolicy() {}

    public static long candidateSeed(long worldSeed, UUID playerId) {
        return worldSeed
            ^ playerId.getMostSignificantBits()
            ^ Long.rotateLeft(playerId.getLeastSignificantBits(), 17);
    }

    public static boolean isOutsideMinimumRadius(int x, int z) {
        return Math.hypot((double) x, (double) z) >= MINIMUM_RADIUS;
    }
}
