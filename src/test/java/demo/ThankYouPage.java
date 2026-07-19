package demo;

import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class ThankYouPage extends PageObject {

    public ThankYouPage(final Page page) {
        super(page);
    }

    public ThankYouPage shouldShowThankYou() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Thank you")).isVisible();
        assertThat(elementByTextAndExactName("Your simulated order is complete. No payment has been taken.")).isVisible();
        assertThat(page.locator(".feedback-star")).hasCount(5);
        return this;
    }

    public WelcomePage returnToWelcomePage() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Return to welcome page").click();
        return new WelcomePage(page);
    }

    public ThankYouPage startFeedbackDictation() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Start dictation").click();
        return this;
    }

    public ThankYouPage shouldPlaceThankYouActionsOnOneRow() {
        assertThat(page.locator(".thank-you-actions")).hasCSS("display", "flex");
        final var returnToWelcome = elementByRoleAndExactName(AriaRole.BUTTON, "Return to welcome page");
        final var startDictation = elementByRoleAndExactName(AriaRole.BUTTON, "Start dictation");
        final var sendFeedback = elementByRoleAndExactName(AriaRole.BUTTON, "Send feedback");
        final Number returnTop = (Number) returnToWelcome.evaluate("element => element.getBoundingClientRect().top");
        final Number dictationTop = (Number) startDictation.evaluate("element => element.getBoundingClientRect().top");
        final Number submitTop = (Number) sendFeedback.evaluate("element => element.getBoundingClientRect().top");
        final Number returnHeight = (Number) returnToWelcome.evaluate("element => element.getBoundingClientRect().height");
        final Number dictationHeight = (Number) startDictation.evaluate("element => element.getBoundingClientRect().height");
        final Number submitHeight = (Number) sendFeedback.evaluate("element => element.getBoundingClientRect().height");
        final Number returnLeft = (Number) returnToWelcome.evaluate("element => element.getBoundingClientRect().left");
        final Number dictationLeft = (Number) startDictation.evaluate("element => element.getBoundingClientRect().left");

        org.assertj.core.api.Assertions.assertThat(returnTop.doubleValue() + returnHeight.doubleValue() / 2)
                .isEqualTo(dictationTop.doubleValue() + dictationHeight.doubleValue() / 2);
        org.assertj.core.api.Assertions.assertThat(dictationTop.doubleValue() + dictationHeight.doubleValue() / 2)
                .isEqualTo(submitTop.doubleValue() + submitHeight.doubleValue() / 2);
        org.assertj.core.api.Assertions.assertThat(returnLeft.doubleValue()).isLessThan(dictationLeft.doubleValue());
        return this;
    }

    public ThankYouPage receiveFeedbackDictation(final String transcript) {
        page.evaluate("""
                transcript => window.testSpeechRecognition.onresult({
                    results: [Object.assign([{ transcript }], { isFinal: true })]
                })
                """, transcript);
        return this;
    }

    public ThankYouPage stopFeedbackDictation() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Stop dictation").click();
        return this;
    }

    public ThankYouPage shouldKeepDictatedFeedbackEditable(final String feedback) {
        assertThat(elementByRoleAndExactName(AriaRole.TEXTBOX, "Comment (optional)")).hasValue(feedback);
        assertThat(page.locator("#feedback-dictation-status")).hasText("Dictation complete. Review or amend the text before submitting.");
        org.assertj.core.api.Assertions.assertThat(page.evaluate("() => String(window.mealRequestSubmissionCount)")).isEqualTo("0");
        return this;
    }

    public FeedbackConfirmationPage submitFeedback(final int rating) {
        page.locator("label[for='feedback-rating-" + rating + "']").click();
        for (int star = 1; star <= 5; star++) {
            final String colour = (String) page.locator("label[for='feedback-rating-" + star + "'] .feedback-star")
                    .evaluate("element => getComputedStyle(element).color");
            org.assertj.core.api.Assertions.assertThat(colour)
                    .isEqualTo(star <= rating ? "rgb(217, 138, 0)" : "rgb(138, 155, 142)");
        }
        elementByRoleAndExactName(AriaRole.BUTTON, "Send feedback").click();
        return new FeedbackConfirmationPage(page);
    }
}
