package com.tingshulien.grpc.unary.respository;

import java.util.Map;
import java.util.Optional;

public class AccountRepository {

  private final Map<Integer, Integer> balanceByAccountNumber;

  public AccountRepository() {
    this.balanceByAccountNumber = Map.of(
        1, 100,
        2, 100,
        3, 100,
        4, 100,
        5, 100,
        6, 100,
        7, 100,
        8, 100,
        9, 100,
        10, 100
    );
  }

  public Optional<Integer> getBalance(int accountNumber) {
    return Optional.ofNullable(balanceByAccountNumber.get(accountNumber));
  }

}
