package io.github.alikhanzhomartov.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrencyConfig {
    public static final ExecutorService V_THREAD_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    public static final ExecutorService BROADCAST_EXECUTOR;

    static {
        int cores = Runtime.getRuntime().availableProcessors();

        ThreadFactory threadFactory = new NamedThreadFactory("broadcast-worker-");

        BROADCAST_EXECUTOR = Executors.newFixedThreadPool(cores, threadFactory);
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger counter = new AtomicInteger(1);

        public NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);

            t.setName(prefix + counter.getAndIncrement());
            t.setDaemon(true);

            return t;
        }
    }
}
