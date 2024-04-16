package com.tingshulien.grpc.unary.service;

import com.tingshulien.grpc.unary.model.AccountBalance;
import com.tingshulien.grpc.unary.model.BalanceCheckRequest;
import com.tingshulien.grpc.unary.model.BankServiceGrpc;
import com.tingshulien.grpc.unary.model.ValidationCode;
import com.tingshulien.grpc.unary.respository.AccountRepository;
import com.tingshulien.grpc.unary.validator.Validation;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class BankService extends BankServiceGrpc.BankServiceImplBase {

  private final AccountRepository accountRepository;

  public BankService() {
    this.accountRepository = new AccountRepository();
  }

  @Override
  public void getAccountBalance(BalanceCheckRequest request,
      StreamObserver<AccountBalance> responseObserver) {
    var balanceOptional = accountRepository.getBalance(request.getAccountNumber());
    if (balanceOptional.isEmpty()) {
      responseObserver.onError(Status.INVALID_ARGUMENT
          .withDescription("Invalid account number: " + request.getAccountNumber())
          .asException(Validation.toMetadata(ValidationCode.INVALID_ACCOUNT)));
      return;
    }

    var balance = balanceOptional.get();
    var accountBalance = AccountBalance.newBuilder()
        .setAccountNumber(request.getAccountNumber())
        .setBalance(balance)
        .build();

    responseObserver.onNext(accountBalance);
    responseObserver.onCompleted();
  }

}
