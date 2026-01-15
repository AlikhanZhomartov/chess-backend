package io.github.alikhanzhomartov;

import io.github.alikhanzhomartov.service.GameBrainServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.Executors;

public class ChessBrainServer {
    private static final int CORES = Runtime.getRuntime().availableProcessors();

    static void main(String[] args) throws IOException, InterruptedException {
        int port = 9090;

        Server server = ServerBuilder.forPort(port)
                .executor(Executors.newFixedThreadPool(CORES))
                .addService(new GameBrainServiceImpl())
                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

        server.awaitTermination();
    }
}