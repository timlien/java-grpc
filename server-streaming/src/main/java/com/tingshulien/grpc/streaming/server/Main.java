package com.tingshulien.grpc.streaming.server;


import com.tingshulien.grpc.streaming.server.service.BankService;

public class Main {

  public static void main(String[] args) {
    GrpcServer.create(new BankService())
        .start()
        .await();
  }

}
