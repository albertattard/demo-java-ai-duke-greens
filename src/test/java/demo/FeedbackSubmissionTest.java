package demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import org.junit.jupiter.api.Test;

class FeedbackSubmissionTest {

    @Test
    void acceptsARequiredRatingAndNormalisesAnOptionalComment() {
        assertThat(FeedbackSubmission.from("4", "  Clear and useful.  "))
                .isEqualTo(new FeedbackSubmission(4, "Clear and useful."));
        assertThat(FeedbackSubmission.from("5", "  "))
                .isEqualTo(new FeedbackSubmission(5, null));
    }

    @Test
    void rejectsMissingOrOutOfRangeRatingsAndOversizedComments() {
        assertThatIllegalArgumentException().isThrownBy(() -> FeedbackSubmission.from(null, null));
        assertThatIllegalArgumentException().isThrownBy(() -> FeedbackSubmission.from("6", null));
        assertThatIllegalArgumentException().isThrownBy(() -> FeedbackSubmission.from("3", "x".repeat(2_001)));
    }
}
