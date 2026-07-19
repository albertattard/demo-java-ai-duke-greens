package demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ContextConfiguration;

@JdbcTest
@ContextConfiguration(initializers = TestDemoAccess.class)
@Import(JdbcFeedbackRepository.class)
class JdbcFeedbackRepositoryTest {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void storesARatingAndOptionalComment() {
        feedbackRepository.save(new FeedbackSubmission(4, "Clear and useful."));
        feedbackRepository.save(new FeedbackSubmission(5, null));

        assertThat(jdbcClient.sql("SELECT rating, comment FROM feedback ORDER BY id")
                .query((resultSet, rowNumber) -> resultSet.getInt("rating") + ":" + resultSet.getString("comment"))
                .list())
                .containsExactly("4:Clear and useful.", "5:null");
    }

    @Test
    void protectsTheFeedbackRatingRangeInTheSchema() {
        assertThatThrownBy(() -> jdbcClient.sql("INSERT INTO feedback (rating) VALUES (0)").update())
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
