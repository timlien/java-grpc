package com.tingshulien.grpc.unary.service;

import com.tingshulien.grpc.model.AccountBalance;
import com.tingshulien.grpc.model.BalanceCheckRequest;
import com.tingshulien.grpc.model.BankServiceGrpc;
import com.tingshulien.grpc.unary.respository.AccountRepository;
import io.grpc.stub.StreamObserver;

public class BankService extends BankServiceGrpc.BankServiceImplBase {

  private final AccountRepository accountRepository;

  public BankService() {
    this.accountRepository = new AccountRepository();
  }

  @Override
  public void getAccountBalance(BalanceCheckRequest request,
      StreamObserver<AccountBalance> responseObserver) {
    var balance = accountRepository.getBalance(request.getAccountNumber());
    var accountBalance = AccountBalance.newBuilder()
        .setAccountNumber(request.getAccountNumber())
        .setBalance(balance)
        .build();

    responseObserver.onNext(accountBalance);
    responseObserver.onCompleted();
  }

}
