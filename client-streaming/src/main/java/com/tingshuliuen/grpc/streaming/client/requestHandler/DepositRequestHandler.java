package com.tingshuliuen.grpc.streaming.client.requestHandler;

import com.tingshulien.grpc.streaming.server.model.AccountBalance;
import com.tingshulien.grpc.streaming.server.model.DepositRequest;
import com.tingshulien.grpc.streaming.server.model.ValidationCode;
import com.tingshuliuen.grpc.streaming.client.repository.AccountRepository;
import com.tingshuliuen.grpc.streaming.client.validator.Validation;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DepositRequestHandler implements StreamObserver<DepositRequest> {

  private final StreamObserver<AccountBalance> responseObserver;

  private final AccountRepository accountRepository;

  private int accountNumber;

  @Override
  public void onNext(DepositRequest depositRequest) {
    switch (depositRequest.getRequestCase()) {
      case ACCOUNT_NUMBER -> {
        this.accountNumber = depositRequest.getAccountNumber();
        if (!accountRepository.exists(this.accountNumber)) {
          responseObserver.onError(Status.INVALID_ARGUMENT
              .withDescription("Account number " + accountNumber + " not exists")
              .asException(Validation.toMetadata(ValidationCode.INVALID_ACCOUNT)));
        }
      }
      case AMOUNT -> accountRepository.addAmount(this.accountNumber, depositRequest.getAmount());
    }
  }

  @Override
  public void onError(Throwable throwable) {
    log.info("client error {}", throwable.getMessage());
  }

  @Override
  public void onCompleted() {
    var balance = accountRepository.getBalance(this.accountNumber);
    var accountBalance = AccountBalance.newBuilder()
        .setAccountNumber(accountNumber)
        .setBalance(balance)
        .build();
    responseObserver.onNext(accountBalance);
    responseObserver.onCompleted();
  }

}
