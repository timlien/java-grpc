package com.tingshuliuen.grpc.streaming.client.service;

import com.tingshulien.grpc.streaming.server.model.AccountBalance;
import com.tingshulien.grpc.streaming.server.model.BankServiceGrpc;
import com.tingshulien.grpc.streaming.server.model.DepositRequest;
import com.tingshuliuen.grpc.streaming.client.repository.AccountRepository;
import com.tingshuliuen.grpc.streaming.client.requestHandler.DepositRequestHandler;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BankService extends BankServiceGrpc.BankServiceImplBase {

  private final AccountRepository accountRepository;

  public BankService() {
    this.accountRepository = new AccountRepository();
  }

  @Override
  public StreamObserver<DepositRequest> deposit(StreamObserver<AccountBalance> responseObserver) {
    return new DepositRequestHandler(responseObserver, accountRepository);
  }

}
