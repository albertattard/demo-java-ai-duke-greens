package demo.functional.browser;

import java.io.Closeable;

import com.microsoft.playwright.Page;

import demo.functional.DukeGreens;

public interface BrowserHarness extends Closeable {

    void openDukeGreens(BrowserConsumer<DukeGreens> consumer) throws Exception;

    void openDukeGreens(BrowserConsumer<Page> pageSetup, BrowserConsumer<DukeGreens> consumer) throws Exception;

    @Override
    void close();
}
