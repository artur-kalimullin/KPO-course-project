package ru.ulstu.grade_analysis.controller;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
    } else if (msg.contains("uk_group_fullname") || msg.contains("groups_prefix_course_subgroup")) {
      userMessage = "Группа с таким названием уже создана";
    }

    return ResponseEntity.badRequest().body(Map.of("message", userMessage));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(
                err ->
                    err.getDefaultMessage() != null ? err.getDefaultMessage() : "Ошибка валидации")
            .orElse("Ошибка валидации");
    return ResponseEntity.badRequest().body(Map.of("message", message));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<?> handleNotReadable(HttpMessageNotReadableException ex) {
    return ResponseEntity.badRequest().body(Map.of("message", "Некорректный JSON"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGenericException(Exception ex) {
    String message = ex.getMessage() != null ? ex.getMessage() : "Внутренняя ошибка сервера";
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", message));
  }
}
