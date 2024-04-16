package com.tingshulien.grpc.streaming.server.service;

import com.google.common.util.concurrent.Uninterruptibles;
import com.tingshulien.grpc.streaming.server.model.BankServiceGrpc;
import com.tingshulien.grpc.streaming.server.model.Money;
import com.tingshulien.grpc.streaming.server.model.ValidationCode;
import com.tingshulien.grpc.streaming.server.model.WithdrawRequest;
import com.tingshulien.grpc.streaming.server.respository.AccountRepository;
import com.tingshulien.grpc.streaming.server.validator.Validation;
import io.grpc.Context;
import io.grpc.Status;
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
    var accountBalanceOptional = accountRepository.getBalance(accountNumber);

    if (accountBalanceOptional.isEmpty()) {
      responseObserver.onError(Status.INVALID_ARGUMENT
          .withDescription("Account " + accountNumber + " does not exist")
          .asException(Validation.toMetadata(ValidationCode.INVALID_ACCOUNT))
      );
      return;
    }

    var accountBalance = accountBalanceOptional.get();
    if (requestedAmount > accountBalance) {
      responseObserver.onError(Status.FAILED_PRECONDITION
          .withDescription(
              "Insufficient balance " + accountBalance + " for account " + accountNumber
                  + " amount " + requestedAmount)
          .asException(Validation.toMetadata(ValidationCode.INSUFFICIENT_BALANCE))
      );
      return;
    }

    for (int i = 0; i < (requestedAmount / 10); i++) {
      // Stop the process if the client has cancelled the request or timeout
      if (Context.current().isCancelled()) {
        break;
      }

      var money = Money.newBuilder().setAmount(10).build();
      responseObserver.onNext(money);
      accountRepository.deductAmount(accountNumber, 10);
      log.info("Withdrawn {} from account: {}", money.getAmount(), accountNumber);
      Uninterruptibles.sleepUninterruptibly(1, java.util.concurrent.TimeUnit.SECONDS);
    }

    responseObserver.onCompleted();
  }

}
