package demo;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class JdbcFeedbackRepository implements FeedbackRepository {

    private final JdbcClient jdbcClient;

    JdbcFeedbackRepository(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void save(final FeedbackSubmission feedback) {
        jdbcClient.sql("""
                INSERT INTO feedback (rating, comment)
                VALUES (:rating, :comment)
                """)
                .param("rating", feedback.rating())
                .param("comment", feedback.comment())
                .update();
    }
}
