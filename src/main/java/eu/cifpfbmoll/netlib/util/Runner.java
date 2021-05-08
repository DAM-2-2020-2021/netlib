package eu.cifpfbmoll.netlib.util;

import java.util.function.Consumer;

public class Runner<T> extends Threaded {
    private final T param;
    private final Consumer<T> consumer;

    public Runner(T param, Consumer<T> consumer) {
        this.param = param;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        this.consumer.accept(this.param);
    }
}
