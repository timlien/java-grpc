package com.tingshulien.grpc.streaming.server;

import com.tingshulien.grpc.streaming.server.model.BankServiceGrpc;
import com.tingshulien.grpc.streaming.server.model.BankServiceGrpc.BankServiceBlockingStub;
import com.tingshulien.grpc.streaming.server.model.BankServiceGrpc.BankServiceStub;
import com.tingshulien.grpc.streaming.server.model.Money;
import com.tingshulien.grpc.streaming.server.model.WithdrawRequest;
import com.tingshulien.grpc.streaming.server.service.BankService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
class ServerStreamingGrpcClientTest {

  private static final String HOST = "localhost";

  private static GrpcServer server;

  private static ManagedChannel channel;

  private static BankServiceBlockingStub blockingBankService;

  private static BankServiceStub asyncBankService;

  @BeforeAll
  public static void setUp() {
    server = GrpcServer.create(new BankService())
        .start();

    channel = ManagedChannelBuilder.forAddress(HOST, GrpcServer.DEFAULT_PORT)
        .usePlaintext()
        .build();

    blockingBankService = BankServiceGrpc.newBlockingStub(channel);
    asyncBankService = BankServiceGrpc.newStub(channel);
  }

  @Test
  void blockingWithdraw() {
    var request = WithdrawRequest.newBuilder()
        .setAccountNumber(1)
        .setAmount(30)
        .build();

    var iterator = blockingBankService.withdraw(request);
    var amount = 0;
    for (Iterator<Money> it = iterator; it.hasNext(); ) {
      var money = it.next();
      amount += money.getAmount();
    }

    log.info("Blocking withdraw: {}", amount);
    Assertions.assertEquals(30, amount);
  }

  @Test
  void asyncGetAccountBalance() throws InterruptedException {
    var request = WithdrawRequest.newBuilder()
        .setAccountNumber(1)
        .setAmount(30)
        .build();

    var result = new ArrayList<Money>();

    var latch = new CountDownLatch(1);

    var observer = new StreamObserver<Money>() {
      @Override
      public void onNext(Money value) {
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

    asyncBankService.withdraw(request, observer);

    latch.await();

    var amount = result.stream()
        .mapToInt(Money::getAmount)
        .sum();

    log.info("Async withdraw: {}", amount);
    Assertions.assertEquals(30, amount);
  }

  @AfterAll
  public static void tearDown() throws InterruptedException {
    channel.shutdownNow()
        .awaitTermination(5, TimeUnit.SECONDS);
    server.stop();
  }

}