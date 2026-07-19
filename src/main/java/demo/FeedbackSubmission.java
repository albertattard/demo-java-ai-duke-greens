package demo;

import module java.base;

import static demo.Numbers.requireBetween;
import static demo.Strings.isBlank;
import static demo.Strings.requireLengthBetween;

record FeedbackSubmission(int rating, String comment) {

    private static final int MAXIMUM_COMMENT_LENGTH = 2_000;

    FeedbackSubmission {
        requireBetween(rating, 1, 5, "Choose a rating from one to five stars.");
        comment = requireValidComment(comment);
    }

    static FeedbackSubmission from(final String rating, final String comment) {
        final int parsedRating;
        try {
            parsedRating = Integer.parseInt(rating);
        } catch (final NumberFormatException _) {
            throw new IllegalArgumentException("Choose a rating from one to five stars.");
        }

        return new FeedbackSubmission(parsedRating, Strings.isBlank(comment) ? null : comment.strip());
    }

    private static String requireValidComment(final String comment) {
        return isBlank(comment)
                ? null
                : requireLengthBetween(comment, 0, MAXIMUM_COMMENT_LENGTH, "Your comment must be 2,000 characters or fewer.");
    }
}
