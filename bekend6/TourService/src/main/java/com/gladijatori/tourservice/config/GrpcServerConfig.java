package com.gladijatori.tourservice.config;

import com.gladijatori.tourservice.grpc.TourGrpcService;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GrpcServerConfig {
    private Server server;

    @Bean
    Server grpcServer(
            TourGrpcService tourGrpcService,
            @Value("${grpc.server.port:9090}") int grpcPort) throws IOException {
        server = NettyServerBuilder
                .forPort(grpcPort)
                .addService(tourGrpcService)
                .build()
                .start();
        return server;
    }

    @PreDestroy
    public void stopGrpcServer() {
        if (server != null) {
            server.shutdown();
        }
    }
}
