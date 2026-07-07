package demo.functional.browser;

@FunctionalInterface
public interface BrowserConsumer<T> {

    void accept(T consumable) throws Exception;
}
