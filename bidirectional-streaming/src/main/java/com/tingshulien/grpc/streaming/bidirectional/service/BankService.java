package com.tingshulien.grpc.streaming.bidirectional.service;

import com.tingshulien.grpc.streaming.bidirectional.repository.AccountRepository;
import com.tingshulien.grpc.streaming.bidirectional.requestHandler.TransferRequestHandler;
import com.tingshulien.grpc.streaming.server.model.BankServiceGrpc;
import com.tingshulien.grpc.streaming.server.model.TransferRequest;
import com.tingshulien.grpc.streaming.server.model.TransferResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BankService extends BankServiceGrpc.BankServiceImplBase {

  private final AccountRepository accountRepository;

  public BankService() {
    this.accountRepository = new AccountRepository();
  }

  @Override
  public StreamObserver<TransferRequest> transfer(
      StreamObserver<TransferResponse> responseObserver) {
    return new TransferRequestHandler(responseObserver, accountRepository);
  }

}
