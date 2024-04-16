package com.tingshulien.grpc.streaming.server;

import com.tingshulien.grpc.streaming.server.model.BankServiceGrpc;
import com.tingshulien.grpc.streaming.server.model.BankServiceGrpc.BankServiceBlockingStub;
import com.tingshulien.grpc.streaming.server.model.BankServiceGrpc.BankServiceStub;
import com.tingshulien.grpc.streaming.server.model.Money;
import com.tingshulien.grpc.streaming.server.model.ValidationCode;
import com.tingshulien.grpc.streaming.server.model.WithdrawRequest;
import com.tingshulien.grpc.streaming.server.service.BankService;
import com.tingshulien.grpc.streaming.server.validator.Validation;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
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

    var iterator = blockingBankService
        .withDeadline(Deadline.after(4, TimeUnit.SECONDS))
        .withdraw(request);
    var amount = 0;
    for (Iterator<Money> it = iterator; it.hasNext(); ) {
      var money = it.next();
      amount += money.getAmount();
    }

    log.info("Blocking withdraw: {}", amount);
    Assertions.assertEquals(30, amount);
  }

  @Test
  void asyncWithdrawBalance() throws InterruptedException {
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

    asyncBankService
        .withDeadline(Deadline.after(4, TimeUnit.SECONDS))
        .withdraw(request, observer);

    latch.await();

    var amount = result.stream()
        .mapToInt(Money::getAmount)
        .sum();

    log.info("Async withdraw: {}", amount);
    Assertions.assertEquals(30, amount);
  }

  @Test
  void blockingWithdrawInvalidBalance() {
    var request = WithdrawRequest.newBuilder()
        .setAccountNumber(1)
        .setAmount(9999)
        .build();

    var exception = Assertions.assertThrows(Exception.class,
        () -> {
          var iterator = blockingBankService
              .withDeadline(Deadline.after(4, TimeUnit.SECONDS))
              .withdraw(request);
          for (Iterator<Money> it = iterator; it.hasNext(); ) {
            it.next();
          }
        }
    );
    Assertions.assertEquals(ValidationCode.INSUFFICIENT_BALANCE, Validation.from(exception));
  }

  @Test
  void asyncWithdrawInvalidBalance() throws InterruptedException {
    var request = WithdrawRequest.newBuilder()
        .setAccountNumber(1)
        .setAmount(9999)
        .build();

    var result = new ArrayList<Throwable>();

    var latch = new CountDownLatch(1);

    var observer = new StreamObserver<Money>() {
      @Override
      public void onNext(Money value) {
        log.info("Async next: {}", value.getAmount());
      }

      @Override
      public void onError(Throwable t) {
        result.add(t);
        latch.countDown();
      }

      @Override
      public void onCompleted() {
        latch.countDown();
      }
    };

    asyncBankService
        .withDeadline(Deadline.after(4, TimeUnit.SECONDS))
        .withdraw(request, observer);

    latch.await();

    var throwable = result.stream()
        .findFirst()
        .orElseThrow();

    Assertions.assertEquals(Status.FAILED_PRECONDITION.getCode(), Status.fromThrowable(throwable).getCode());
    Assertions.assertEquals(ValidationCode.INSUFFICIENT_BALANCE, Validation.from(throwable));
  }

  @AfterAll
  public static void tearDown() throws InterruptedException {
    channel.shutdownNow()
        .awaitTermination(5, TimeUnit.SECONDS);
    server.stop();
  }

}