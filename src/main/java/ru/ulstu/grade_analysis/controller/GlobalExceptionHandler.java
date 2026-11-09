package ru.ulstu.grade_analysis.controller;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex) {
    String message = ex.getReason() != null ? ex.getReason() : "Ошибка запроса";
    return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", message));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
    String userMessage = "Ошибка базы данных. Проверьте уникальность полей.";

    if (msg.contains("record_book_number")) {
      userMessage = "Студент с таким номером зачетной книжки уже существует";
    } else if (msg.contains("username")) {
      userMessage = "Пользователь с таким логином уже существует";
    }

    return ResponseEntity.badRequest().body(Map.of("message", userMessage));
  }
}
