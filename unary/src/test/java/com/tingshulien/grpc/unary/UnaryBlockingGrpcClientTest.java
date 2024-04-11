package com.tingshulien.grpc.unary;

import com.tingshulien.grpc.model.AccountBalance;
import com.tingshulien.grpc.model.BalanceCheckRequest;
import com.tingshulien.grpc.model.BankServiceGrpc;
import com.tingshulien.grpc.model.BankServiceGrpc.BankServiceBlockingStub;
import com.tingshulien.grpc.model.BankServiceGrpc.BankServiceStub;
import com.tingshulien.grpc.unary.service.BankService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class UnaryBlockingGrpcClientTest {

  private static final String HOST = "localhost";

  private static GrpcServer server;

  private static ManagedChannel channel;

  private static BankServiceBlockingStub blockingBankService;

  private static BankServiceStub bankService;

  @BeforeAll
  public static void setUp() {
    server = GrpcServer.create(new BankService())
        .start();

    channel = ManagedChannelBuilder.forAddress(HOST, GrpcServer.DEFAULT_PORT)
        .usePlaintext()
        .build();

    blockingBankService = BankServiceGrpc.newBlockingStub(channel);
    bankService = BankServiceGrpc.newStub(channel);
  }

  @Test
  void blockingGetAccountBalance() {
    var request = BalanceCheckRequest.newBuilder()
        .setAccountNumber(1)
        .build();

    var value = blockingBankService.getAccountBalance(request);
    log.info("Blocking balance: {}", value.getBalance());
    Assertions.assertEquals(100, value.getBalance());
  }

  @Test
  void asyncGetAccountBalance() throws InterruptedException {
    var request = BalanceCheckRequest.newBuilder()
        .setAccountNumber(1)
        .build();

    var result = new ArrayList<AccountBalance>();

    var latch = new CountDownLatch(1);

    var observer = new StreamObserver<AccountBalance>() {
      @Override
      public void onNext(AccountBalance value) {
        log.info("Async balance: {}", value.getBalance());
        result.add(value);
      }

      @Override
      public void onError(Throwable t) {
        log.error("Async error: {}", t.getMessage());
        latch.countDown();
      }

      @Override
      public void onCompleted() {
        latch.countDown();
      }
    };

    bankService.getAccountBalance(request, observer);

    latch.await();

    var balance = result.stream()
        .map(AccountBalance::getBalance)
        .findFirst()
        .orElse(-1);

    Assertions.assertEquals(100, balance);
  }

  @AfterAll
  public static void tearDown() throws InterruptedException {
    channel.shutdownNow()
        .awaitTermination(5, TimeUnit.SECONDS);
    server.stop();
  }

}
