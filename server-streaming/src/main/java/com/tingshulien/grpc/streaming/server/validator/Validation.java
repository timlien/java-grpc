package com.tingshulien.grpc.streaming.server.validator;

import com.tingshulien.grpc.streaming.server.model.ValidationCode;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.Status;

public class Validation {

  public static final Key<String> VALIDATION_CODE_KEY = Key.of("validation-code", Metadata.ASCII_STRING_MARSHALLER);

  public static Metadata toMetadata(ValidationCode validationCode) {
    var metadata = new Metadata();
    metadata.put(VALIDATION_CODE_KEY, validationCode.name());
    return metadata;
  }

  public static ValidationCode from(Throwable throwable) {
    var metadata = Status.trailersFromThrowable(throwable);
    var value = metadata.get(VALIDATION_CODE_KEY);
    return ValidationCode.valueOf(value);
  }

}
