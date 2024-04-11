package com.tingshulien.grpc.unary;

import com.tingshulien.grpc.unary.service.BankService;

public class Main {

  public static void main(String[] args) {
    GrpcServer.create(new BankService())
        .start()
        .await();
  }

}
