package com.tingshulien.grpc.streaming.bidirectional;

import com.tingshulien.grpc.streaming.bidirectional.service.BankService;

public class Main {

  public static void main(String[] args) {
    GrpcServer.create(new BankService())
        .start()
        .await();
  }

}
