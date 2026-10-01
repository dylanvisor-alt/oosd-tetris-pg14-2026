package tetris;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tetris.controller.ConfigController;
import tetris.controller.HighScoreController;
import tetris.model.ScoreEntry;
import tetris.persistence.ScoreRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AdvancedTestingTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    void acceptsValidGameLevels(int level) {
        ConfigController controller = new ConfigController();

        controller.setLevel(level);

        assertEquals(level, controller.getLevel());
    }

    @Test
    void fakeRepositoryStoresAndReturnsScores() {
        FakeScoreRepository repository = new FakeScoreRepository();
        HighScoreController controller =
                new HighScoreController(repository);

        controller.saveScore("Han", 700);
        controller.saveScore("Dylan", 900);

        assertEquals(
                List.of(
                        new ScoreEntry("Dylan", 900),
                        new ScoreEntry("Han", 700)
                ),
                controller.getTopScores()
        );
    }

    @Test
    void mockRepositoryReceivesTrimmedPlayerName() {
        ScoreRepository repository =
                mock(ScoreRepository.class);
        HighScoreController controller =
                new HighScoreController(repository);

        controller.saveScore("  Gia Han  ", 800);

        verify(repository)
                .save(new ScoreEntry("Gia Han", 800));
    }

    private static class FakeScoreRepository
            implements ScoreRepository {

        private final List<ScoreEntry> scores =
                new ArrayList<>();

        @Override
        public void save(ScoreEntry scoreEntry) {
            scores.add(scoreEntry);
        }

        @Override
        public List<ScoreEntry> findTopScores(int limit) {
            return scores.stream()
                    .sorted(
                            Comparator.comparingInt(
                                    ScoreEntry::score
                            ).reversed()
                    )
                    .limit(limit)
                    .toList();
        }
    }
}