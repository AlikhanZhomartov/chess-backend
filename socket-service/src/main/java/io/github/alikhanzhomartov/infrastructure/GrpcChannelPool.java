package io.github.alikhanzhomartov.infrastructure;

import io.github.alikhanzhomartov.grpc.GameBrainServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GrpcChannelPool {

    private final List<ManagedChannel> channels;
    private final List<GameBrainServiceGrpc.GameBrainServiceBlockingStub> stubs;
    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);
    private final int poolSize;

    public GrpcChannelPool(String host, int port, int poolSize) {
        this.poolSize = poolSize;
        this.channels = new ArrayList<>(poolSize);
        this.stubs = new ArrayList<>(poolSize);

        for (int i = 0; i < poolSize; i++) {
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(host, port)
                    .usePlaintext()
                    .disableRetry()
                    .build();

            channels.add(channel);
            stubs.add(GameBrainServiceGrpc.newBlockingStub(channel));
        }
    }

    public GameBrainServiceGrpc.GameBrainServiceBlockingStub nextStub() {
        int index = roundRobinCounter.getAndUpdate(i -> {
            int next = i + 1;
            return next >= poolSize ? 0 : next;
        });

        return stubs.get(index);
    }

    public void shutdown() {
        for (ManagedChannel channel : channels) {
            if (!channel.isShutdown()) {
                channel.shutdown();
            }
        }
    }
}
