package tetris;

import org.junit.jupiter.api.Test;
import tetris.network.ConnectivityChecker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ConnectivityCheckerTest {
    @Test
    void blockingCheckNeverThrowsWithoutNetwork() {
        // not asserting true/false
        assertDoesNotThrow(ConnectivityChecker::isOnlineBlocking);
    }
}
