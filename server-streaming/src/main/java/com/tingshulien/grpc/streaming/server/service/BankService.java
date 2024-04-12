package com.tingshulien.grpc.streaming.server.service;

import com.tingshulien.grpc.streaming.server.model.BankServiceGrpc;
import com.tingshulien.grpc.streaming.server.model.Money;
import com.tingshulien.grpc.streaming.server.model.WithdrawRequest;
import com.tingshulien.grpc.streaming.server.respository.AccountRepository;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BankService extends BankServiceGrpc.BankServiceImplBase {

  private final AccountRepository accountRepository;

  public BankService() {
    this.accountRepository = new AccountRepository();
  }

  @Override
  public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {
    var accountNumber = request.getAccountNumber();
    var requestedAmount = request.getAmount();
    var accountBalance = accountRepository.getBalance(accountNumber);

    if (requestedAmount > accountBalance) {
      responseObserver.onCompleted();
      return;
    }

    for (int i = 0; i < (requestedAmount / 10); i++) {
      var money = Money.newBuilder().setAmount(10).build();
      responseObserver.onNext(money);
      accountRepository.deductAmount(accountNumber, 10);
      log.info("Withdrawn {} from account: {}", money.getAmount(), accountNumber);
    }

    responseObserver.onCompleted();
  }

}
