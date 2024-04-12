package com.tingshuliuen.grpc.streaming.client;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcServer {

  public static final int DEFAULT_PORT = 6565;

  private final Server server;

  private GrpcServer(Server server) {
    this.server = server;
  }

  public static GrpcServer create(BindableService... services) {
    return create(DEFAULT_PORT, services);
  }

  public static GrpcServer create(int port, BindableService... services) {
    var builder = ServerBuilder.forPort(port);
    Arrays.asList(services).forEach(builder::addService);
    return new GrpcServer(builder.build());
  }

  public GrpcServer start() {
    var services = server.getServices()
        .stream()
        .map(ServerServiceDefinition::getServiceDescriptor)
        .map(ServiceDescriptor::getName)
        .toList();
    try {
      server.start();
      log.info("Server started. listening on port {}. services: {}", server.getPort(), services);
      return this;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void await() {
    try {
      server.awaitTermination();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    server.shutdownNow();
    log.info("Server stopped");
  }

}
