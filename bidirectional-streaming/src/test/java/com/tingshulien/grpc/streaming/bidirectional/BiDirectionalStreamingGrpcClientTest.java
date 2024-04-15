package com.tingshulien.grpc.streaming.bidirectional;

import com.tingshulien.grpc.streaming.bidirectional.service.BankService;
import com.tingshulien.grpc.streaming.server.model.BankServiceGrpc;
import com.tingshulien.grpc.streaming.server.model.BankServiceGrpc.BankServiceStub;
import com.tingshulien.grpc.streaming.server.model.TransferRequest;
import com.tingshulien.grpc.streaming.server.model.TransferResponse;
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
class BiDirectionalStreamingGrpcClientTest {

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

    var responseObserver = new StreamObserver<TransferResponse>() {
      @Override
      public void onNext(TransferResponse value) {
        result.add(value.getToAccount().getBalance());
        log.info("Transfer status : {}", value.getStatus());
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

    var requestObserver = asyncBankService.transfer(responseObserver);
    var transferRequest = TransferRequest.newBuilder()
        .setFromAccount(1)
        .setToAccount(2)
        .setAmount(10)
        .build();

    requestObserver.onNext(transferRequest);
    requestObserver.onNext(transferRequest);
    requestObserver.onCompleted();

    latch.await();

    var amount = result.stream()
        .mapToInt(Integer::intValue)
        .max().orElseThrow();

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