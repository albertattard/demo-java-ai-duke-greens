package demo;

import java.net.URI;
import java.util.Locale;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import com.microsoft.playwright.options.AriaRole;

public final class WelcomePage extends PageObject {

    public WelcomePage(final Page page) {
        super(page);
    }

    public WelcomePage shouldShowWelcome() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Duke Greens")).isVisible();
        return this;
    }

    public AboutPage openAboutPage() {
        open("/about");
        return new AboutPage(page);
    }

    public WelcomePage shouldShowNoActiveMealRequest() {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Meal request unavailable")).isVisible();
        final Locator informationMessage = page.locator(".information-message");
        assertThat(informationMessage).isVisible();
        assertThat(informationMessage).hasAttribute("role", "status");
        assertThat(informationMessage.locator("p")).hasText("There is no active meal request to display.");
        return this;
    }

    public WelcomePage shouldProvideMealRequestInput() {
        final Locator mealRequestInput = elementByRoleAndExactName(AriaRole.TEXTBOX, "Tell us what you would like to cook, and we will help plan your meals and groceries.");
        assertThat(mealRequestInput).isVisible();
        assertThat(mealRequestInput).hasAttribute("maxlength", "1000");
        assertThat(mealRequestInput).hasAttribute("aria-describedby", "meal-request-character-count meal-request-character-count-error");
        shouldShowMealRequestCharacterCount(0);
        return this;
    }

    public WelcomePage startDictation() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Start dictation").click();
        return this;
    }

    public WelcomePage stopDictation() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Stop dictation").click();
        return this;
    }

    public WelcomePage cancelDictation() {
        elementByRoleAndExactName(AriaRole.BUTTON, "Cancel dictation").click();
        return this;
    }

    public WelcomePage receiveDictation(final String transcript) {
        page.evaluate("""
                transcript => window.testSpeechRecognition.onresult({
                    results: [Object.assign([{ transcript }], { isFinal: true })]
                })
                """, transcript);
        return this;
    }

    public WelcomePage enterMealRequest(final String request) {
        fillMealRequest(request);
        return this;
    }

    public WelcomePage shouldShowMealRequestCharacterCount(final int count) {
        final Locator counter = page.locator("[data-character-count]");
        assertThat(counter).hasText(String.format(Locale.US, "%,d of 1,000 characters", count));
        org.assertj.core.api.Assertions.assertThat(counter.evaluate("element => getComputedStyle(element).textAlign")).isEqualTo("right");
        final Number counterFontSize = (Number) counter.evaluate("element => Number.parseFloat(getComputedStyle(element).fontSize)");
        final Number textareaFontSize = (Number) elementByRoleAndExactName(AriaRole.TEXTBOX, "Tell us what you would like to cook, and we will help plan your meals and groceries.")
                .evaluate("element => Number.parseFloat(getComputedStyle(element).fontSize)");
        org.assertj.core.api.Assertions.assertThat(counterFontSize.doubleValue()).isLessThan(textareaFontSize.doubleValue());
        return this;
    }

    public WelcomePage failDictation(final String error) {
        page.evaluate("error => window.testSpeechRecognition.onerror({ error })", error);
        return this;
    }

    public WelcomePage shouldShowDictationListening() {
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Stop dictation")).isVisible();
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Cancel dictation")).isVisible();
        assertThat(page.locator("[data-dictation-status]")).hasText("Listening. Speak your request, then stop dictation when you are finished.");
        return this;
    }

    public WelcomePage shouldShowCompletedDictation(final String transcript) {
        assertThat(elementByRoleAndExactName(AriaRole.TEXTBOX, "Tell us what you would like to cook, and we will help plan your meals and groceries.")).hasValue(transcript);
        shouldShowMealRequestCharacterCount(transcript.length());
        final Locator status = page.locator("[data-dictation-status]");
        assertThat(status).hasText("Dictation complete. Review or amend the text before submitting.");
        final Number inputBottom = (Number) elementByRoleAndExactName(AriaRole.TEXTBOX, "Tell us what you would like to cook, and we will help plan your meals and groceries.")
                .evaluate("element => element.getBoundingClientRect().bottom");
        final Number statusTop = (Number) status.evaluate("element => element.getBoundingClientRect().top");
        org.assertj.core.api.Assertions.assertThat(statusTop.doubleValue()).isGreaterThan(inputBottom.doubleValue());
        org.assertj.core.api.Assertions.assertThat(page.evaluate("() => String(window.mealRequestSubmissionCount)")).isEqualTo("0");
        return this;
    }

    public WelcomePage shouldKeepTextWhenDictationIsCancelled(final String text) {
        assertThat(elementByRoleAndExactName(AriaRole.TEXTBOX, "Tell us what you would like to cook, and we will help plan your meals and groceries.")).hasValue(text);
        assertThat(page.locator("[data-dictation-status]")).hasText("Dictation cancelled. Your typed request is unchanged.");
        org.assertj.core.api.Assertions.assertThat(page.evaluate("() => String(window.mealRequestSubmissionCount)")).isEqualTo("0");
        return this;
    }

    public WelcomePage shouldKeepTextWhenDictationFails(final String text) {
        final Locator input = elementByRoleAndExactName(AriaRole.TEXTBOX, "Tell us what you would like to cook, and we will help plan your meals and groceries.");
        final Locator status = page.locator("[data-dictation-status]");
        assertThat(input).hasValue(text);
        assertThat(status).hasText("Microphone access was not allowed. Check your browser permission and try again.");
        final Number inputWidth = (Number) input.evaluate("element => element.getBoundingClientRect().width");
        final Number statusWidth = (Number) status.evaluate("element => element.getBoundingClientRect().width");
        org.assertj.core.api.Assertions.assertThat(statusWidth.doubleValue()).isEqualTo(inputWidth.doubleValue());
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Start dictation")).isVisible();
        org.assertj.core.api.Assertions.assertThat(page.evaluate("() => String(window.mealRequestSubmissionCount)")).isEqualTo("0");
        return this;
    }

    public WelcomePage shouldShowUnavailableDictation() {
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Start dictation")).isHidden();
        assertThat(page.locator("[data-dictation-status]")).hasText("Dictation is unavailable in this browser. You can still type your request.");
        return this;
    }

    public RecommendationsPage submitMealRequest(final String request) {
        fillMealRequest(request);
        elementByRoleAndExactName(AriaRole.BUTTON, "Get meal ideas").click();
        return new RecommendationsPage(page);
    }

    public WelcomePage submitInvalidMealRequest(final String request) {
        fillMealRequest(request);
        elementByRoleAndExactName(AriaRole.BUTTON, "Get meal ideas").click();
        return this;
    }

    public WelcomePage reload() {
        page.reload();
        return this;
    }

    WelcomePage shouldShowBlankRequestValidation() {
        final Locator validationMessage = page.locator(".validation-message");
        assertThat(validationMessage).isVisible();
        assertThat(validationMessage).hasText("Describe at least one meal you want.");
        assertThat(validationMessage).hasAttribute("aria-live", "polite");
        return this;
    }

    WelcomePage shouldShowInitialRequestState() {
        final Locator mealRequestInput = elementByRoleAndExactName(AriaRole.TEXTBOX, "Tell us what you would like to cook, and we will help plan your meals and groceries.");
        assertThat(mealRequestInput).isVisible();
        assertThat(mealRequestInput).hasValue("");
        assertThat(elementByRoleAndExactName(AriaRole.BUTTON, "Get meal ideas")).isVisible();
        return this;
    }

    WelcomePage shouldPlaceDictationBesideMealIdeas() {
        final Locator dictation = elementByRoleAndExactName(AriaRole.BUTTON, "Start dictation");
        final Locator submit = elementByRoleAndExactName(AriaRole.BUTTON, "Get meal ideas");
        final Number dictationTop = (Number) dictation.evaluate("element => element.getBoundingClientRect().top");
        final Number submitTop = (Number) submit.evaluate("element => element.getBoundingClientRect().top");
        final Number dictationLeft = (Number) dictation.evaluate("element => element.getBoundingClientRect().left");
        final Number submitLeft = (Number) submit.evaluate("element => element.getBoundingClientRect().left");

        org.assertj.core.api.Assertions.assertThat(dictationTop.doubleValue()).isEqualTo(submitTop.doubleValue());
        org.assertj.core.api.Assertions.assertThat(dictationLeft.doubleValue()).isLessThan(submitLeft.doubleValue());
        return this;
    }

    WelcomePage shouldPlaceMealRequestActionsOnOneRow() {
        final Locator actions = page.locator(".meal-request-actions");
        assertThat(actions).isVisible();
        assertThat(elementByRoleAndExactName(actions, AriaRole.BUTTON, "Log out")).isVisible();
        assertThat(elementByRoleAndExactName(actions, AriaRole.BUTTON, "Get meal ideas")).isVisible();
        org.assertj.core.api.Assertions.assertThat(actions.evaluate("element => getComputedStyle(element).justifyContent")).isEqualTo("space-between");
        return this;
    }

    public WelcomePage shouldShowProducts(final int count) {
        assertThat(elementByRoleAndExactName(AriaRole.HEADING, "Products")).isVisible();
        assertThat(page.locator("[data-testid='product-card']")).hasCount(count);
        return this;
    }

    public WelcomePage shouldShowProduct(final String name, final String packageDetail, final String price) {
        final Locator card = elementByRoleAndExactName(AriaRole.HEADING, name)
                .locator("xpath=ancestor::article[@data-testid='product-card']");
        assertThat(card).hasCount(1);
        assertThat(elementByRoleAndExactName(card, AriaRole.HEADING, name)).isVisible();
        assertThat(elementByTextAndExactName(card, packageDetail)).isVisible();
        assertThat(elementByTextAndExactName(card, price + "\u00a0€")).isVisible();
        return this;
    }

    private void fillMealRequest(final String request) {
        elementByRoleAndExactName(AriaRole.TEXTBOX, "Tell us what you would like to cook, and we will help plan your meals and groceries.").fill(request);
    }

    private void open(final String path) {
        page.navigate(URI.create(page.url()).resolve(path).toString());
    }
}
