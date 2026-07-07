package demo.functional.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import demo.functional.DukeGreens;

public final class PlaywrightBrowserHarness implements BrowserHarness {

    private final String baseUrl;
    private final Playwright playwright;
    private final Browser browser;

    public PlaywrightBrowserHarness(final String baseUrl) {
        this.baseUrl = baseUrl;
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @Override
    public void openDukeGreens(final BrowserConsumer<DukeGreens> consumer) throws Exception {
        try (final Page page = browser.newPage()) {
            consumer.accept(new DukeGreens(baseUrl, page));
        }
    }

    @Override
    public void close() {
        closeAll(browser, playwright);
    }

    private static void closeAll(final AutoCloseable... closeables) {
        RuntimeException failure = null;

        for (final AutoCloseable closeable : closeables) {
            if (closeable == null) {
                continue;
            }

            try {
                closeable.close();
            } catch (final Exception e) {
                if (failure == null) {
                    failure = e instanceof RuntimeException re ? re : new RuntimeException("Failed to close resource", e);
                } else {
                    failure.addSuppressed(e);
                }
            }
        }

        if (failure != null) {
            throw failure;
        }
    }
}
