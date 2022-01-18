package ru.gosuslugi.pgu.fs.service;

import org.springframework.http.HttpStatus;

public interface ConfirmEmailService {

    HttpStatus sendConfirmationEmail(Long orderId);

    HttpStatus checkConfirmationEmail(String confirmationEmail, Long orderId);
}
