package dev.rinchan.scarboroughfair.neoforge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

final class OuterSpawnPolicyTest {
    @Test
    void radialBoundaryMatchesSquaredDistanceAcrossTheCandidateArea() {
        long minimumSquared = (long) OuterSpawnPolicy.MINIMUM_RADIUS * OuterSpawnPolicy.MINIMUM_RADIUS;
        for (int x = -1_100; x <= 1_100; x += 11) {
            for (int z = -1_100; z <= 1_100; z += 11) {
                boolean expected = (long) x * x + (long) z * z >= minimumSquared;
                assertEquals(expected, OuterSpawnPolicy.isOutsideMinimumRadius(x, z));
            }
        }
    }

    @Test
    void rejectsAxisOnlyAndOffByOneRadiusChecks() {
        assertTrue(OuterSpawnPolicy.isOutsideMinimumRadius(1_024, 0));
        assertFalse(OuterSpawnPolicy.isOutsideMinimumRadius(1_023, 0));
        assertTrue(OuterSpawnPolicy.isOutsideMinimumRadius(725, 725));
    }

    @Test
    void candidateSeedIsStableAndOwnedByWorldAndPlayer() {
        UUID firstPlayer = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID secondPlayer = UUID.fromString("00000000-0000-0000-0000-000000000002");
        long seed = OuterSpawnPolicy.candidateSeed(42L, firstPlayer);
        assertEquals(seed, OuterSpawnPolicy.candidateSeed(42L, firstPlayer));
        assertNotEquals(seed, OuterSpawnPolicy.candidateSeed(43L, firstPlayer));
        assertNotEquals(seed, OuterSpawnPolicy.candidateSeed(42L, secondPlayer));
    }
}
