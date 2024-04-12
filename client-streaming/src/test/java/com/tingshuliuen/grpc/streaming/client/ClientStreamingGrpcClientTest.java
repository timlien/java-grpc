package com.tingshuliuen.grpc.streaming.client;

import com.tingshulien.grpc.streaming.server.model.AccountBalance;
import com.tingshulien.grpc.streaming.server.model.BankServiceGrpc;
import com.tingshulien.grpc.streaming.server.model.BankServiceGrpc.BankServiceStub;
import com.tingshulien.grpc.streaming.server.model.DepositRequest;
import com.tingshuliuen.grpc.streaming.client.service.BankService;
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
class ClientStreamingGrpcClientTest {

  private static final String HOST = "localhost";

  private static GrpcServer server;

  private static ManagedChannel channel;

  // Client Streaming does not support blocking API
  // private static BankServiceBlockingStub blockingBankService;

  private static BankServiceStub asyncBankService;

  @BeforeAll
  public static void setUp() {
    server = GrpcServer.create(new BankService())
        .start();

    channel = ManagedChannelBuilder.forAddress(HOST, GrpcServer.DEFAULT_PORT)
        .usePlaintext()
        .build();

    // blockingBankService = BankServiceGrpc.newBlockingStub(channel);
    asyncBankService = BankServiceGrpc.newStub(channel);
  }

  @Test
  void asyncDepositAndGetAccountBalance() throws InterruptedException {
    var result = new ArrayList<Integer>();

    var latch = new CountDownLatch(1);

    var responseObserver = new StreamObserver<AccountBalance>() {
      @Override
      public void onNext(AccountBalance value) {
        result.add(value.getBalance());
      }

      @Override
      public void onError(Throwable t) {
        latch.countDown();
        log.error("Async error: {}", t.getMessage());
      }

      @Override
      public void onCompleted() {
        latch.countDown();
        log.info("Async completed");
      }
    };

    var requestObserver = asyncBankService.deposit(responseObserver);
    requestObserver.onNext(DepositRequest.newBuilder().setAccountNumber(1).build());
    requestObserver.onNext(DepositRequest.newBuilder().setAmount(10).build());
    requestObserver.onNext(DepositRequest.newBuilder().setAmount(10).build());
    // requestObserver.onError(new RuntimeException("Cancel deposit request")); // To invoke onError you need comment the line onCompleted
    requestObserver.onCompleted();

    latch.await();

    var amount = result.stream()
        .mapToInt(Integer::intValue)
        .sum();

    log.info("Async deposit balance: {}", amount);
    Assertions.assertEquals(120, amount);
  }

  @AfterAll
  public static void tearDown() throws InterruptedException {
    channel.shutdownNow()
        .awaitTermination(5, TimeUnit.SECONDS);
    server.stop();
  }

}