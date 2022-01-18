package ru.gosuslugi.pgu.fs.service;

import org.springframework.http.HttpStatus;

public interface ConfirmCodeService {

    HttpStatus sendConfirmationCode(Long orderId);

    HttpStatus checkConfirmationCode(String confirmationCode, Long orderId);

}
