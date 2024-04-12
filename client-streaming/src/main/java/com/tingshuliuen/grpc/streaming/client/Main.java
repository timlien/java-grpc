package com.tingshuliuen.grpc.streaming.client;

import com.tingshuliuen.grpc.streaming.client.service.BankService;

public class Main {

  public static void main(String[] args) {
    GrpcServer.create(new BankService())
        .start()
        .await();
  }

}
