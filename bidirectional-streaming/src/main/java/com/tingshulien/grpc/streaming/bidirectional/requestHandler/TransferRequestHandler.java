package com.tingshulien.grpc.streaming.bidirectional.requestHandler;

import com.tingshulien.grpc.streaming.bidirectional.repository.AccountRepository;
import com.tingshulien.grpc.streaming.server.model.AccountBalance;
import com.tingshulien.grpc.streaming.server.model.TransferRequest;
import com.tingshulien.grpc.streaming.server.model.TransferResponse;
import com.tingshulien.grpc.streaming.server.model.TransferStatus;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TransferRequestHandler implements StreamObserver<TransferRequest> {

  private final StreamObserver<TransferResponse> responseObserver;

  private final AccountRepository accountRepository;

  @Override
  public void onNext(TransferRequest transferRequest) {
    TransferStatus transferStatus = transfer(transferRequest);
    var transferResponse = TransferResponse.newBuilder()
        .setStatus(transferStatus)
        .setFromAccount(getAccountBalance(transferRequest.getFromAccount()))
        .setToAccount(getAccountBalance(transferRequest.getToAccount()))
        .build();
    responseObserver.onNext(transferResponse);
  }

  @Override
  public void onError(Throwable throwable) {
    log.error("Transfer error", throwable);
  }

  @Override
  public void onCompleted() {
    log.info("Transfer completed");
    responseObserver.onCompleted();
  }

  private TransferStatus transfer(TransferRequest transferRequest) {
    log.info("Transfer request: {}", transferRequest);
    int fromAccount = transferRequest.getFromAccount();
    int toAccount = transferRequest.getToAccount();
    int amount = transferRequest.getAmount();

    if (fromAccount == toAccount) {
      return TransferStatus.REJECTED;
    }

    if (accountRepository.getBalance(fromAccount) < amount) {
      return TransferStatus.REJECTED;
    }

    accountRepository.deductAmount(fromAccount, amount);
    accountRepository.addAmount(toAccount, amount);
    return TransferStatus.COMPLETED;
  }

  private AccountBalance getAccountBalance(int accountNumber) {
    return AccountBalance.newBuilder()
        .setAccountNumber(accountNumber)
        .setBalance(accountRepository.getBalance(accountNumber))
        .build();
  }

}
