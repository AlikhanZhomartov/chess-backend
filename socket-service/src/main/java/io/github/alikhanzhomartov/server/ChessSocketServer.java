package io.github.alikhanzhomartov.server;

import io.github.alikhanzhomartov.auth.JwtService;
import io.github.alikhanzhomartov.config.ServerConfig;
import io.github.alikhanzhomartov.exception.GlobalExceptionHandler;
import io.github.alikhanzhomartov.infrastructure.GrpcChannelPool;
import io.github.alikhanzhomartov.server.handler.auth.JwtAuthHandler;
import io.github.alikhanzhomartov.server.handler.game.GamePlayHandler;
import io.github.alikhanzhomartov.server.handler.game.HeartbeatHandler;
import io.github.alikhanzhomartov.server.handler.session.SessionLifecycleHandler;
import io.github.alikhanzhomartov.service.GameEventListener;
import io.github.alikhanzhomartov.service.GameEventPublisher;
import io.github.alikhanzhomartov.service.GameService;
import io.github.alikhanzhomartov.service.impl.GameEventListenerImpl;
import io.github.alikhanzhomartov.service.impl.GameEventPublisherImpl;
import io.github.alikhanzhomartov.service.impl.GrpcGameServiceImpl;
import io.github.alikhanzhomartov.session.SessionManager;
import io.nats.client.Connection;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static io.github.alikhanzhomartov.config.NatsConfig.createNatsConnection;

public class ChessSocketServer {
    private static final Logger log = LoggerFactory.getLogger(ChessSocketServer.class);

    public static void handle() {
        ServerConfig config = ServerConfig.loadFromEnv();

        SessionManager sessionManager = new SessionManager();

        Connection natsConnection = createNatsConnection(config.natsUrl());

        GameEventPublisher publisher = new GameEventPublisherImpl(natsConnection);
        GameEventListener listener = new GameEventListenerImpl(natsConnection, sessionManager);

        listener.start();

        GrpcChannelPool grpcPool = new GrpcChannelPool(
                config.grpcHost(),
                config.grpcPort(),
                config.grpcPoolSize()
        );
        JwtService jwtService = new JwtService();
        GameService gameService = new GrpcGameServiceImpl(grpcPool, publisher);

        startServer(config, sessionManager, gameService, jwtService, grpcPool);
    }

    private static void startServer(ServerConfig config,
                                    SessionManager sessionManager,
                                    GameService gameService,
                                    JwtService jwtService,
                                    GrpcChannelPool grpcPool) {
        EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(config.bossThreads(), NioIoHandler.newFactory());
        EventLoopGroup workGroup = new MultiThreadIoEventLoopGroup(config.workerThreads(), NioIoHandler.newFactory());

        shutDownHook(bossGroup, workGroup, grpcPool);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            setupPipeline(ch, gameService, sessionManager, jwtService);
                        }
                    });

            log.info("Chess Socket Server started on port {}", config.port());
            bootstrap.bind(config.port()).sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Server interrupted", e);
        } catch (Exception e) {
            log.error("Fatal server error", e);
            System.exit(1);
        }
    }

    private static void setupPipeline(SocketChannel ch, GameService gameService, SessionManager sessionManager, JwtService jwtService) {
        ch.pipeline()
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new JwtAuthHandler(jwtService))

                .addLast(new IdleStateHandler(60, 30, 0, TimeUnit.SECONDS))
                .addLast(new WebSocketServerProtocolHandler("/game", null, true))
                .addLast(new WebSocketFrameAggregator(65536))

                .addLast(new HeartbeatHandler())
                .addLast(new SessionLifecycleHandler(gameService, sessionManager))
                .addLast(new GamePlayHandler(gameService))

                .addLast(new GlobalExceptionHandler());
    }

    private static void shutDownHook(EventLoopGroup bossGroup, EventLoopGroup workGroup, GrpcChannelPool grpcPool) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stopping server...");
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            grpcPool.shutdown();
            log.info("Server stopped.");
        }));
    }
}
