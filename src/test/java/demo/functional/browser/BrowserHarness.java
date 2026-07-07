package demo.functional.browser;

import java.io.Closeable;

import demo.functional.DukeGreens;

public interface BrowserHarness extends Closeable {

    void openDukeGreens(BrowserConsumer<DukeGreens> consumer) throws Exception;

    @Override
    void close();
}
