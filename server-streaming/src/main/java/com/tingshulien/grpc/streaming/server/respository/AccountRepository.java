package com.tingshulien.grpc.streaming.server.respository;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AccountRepository {

  private final Map<Integer, Integer> balanceByAccountNumber;

  public AccountRepository() {
    this.balanceByAccountNumber = IntStream.rangeClosed(1, 10)
        .boxed()
        .collect(Collectors.toConcurrentMap(
            Function.identity(),
            v -> 100
        ));
  }

  public Integer getBalance(int accountNumber) {
    return balanceByAccountNumber.get(accountNumber);
  }

  public void deductAmount(int accountNumber, int amount) {
    balanceByAccountNumber.computeIfPresent(accountNumber, (k, v) -> v - amount);
  }

}
