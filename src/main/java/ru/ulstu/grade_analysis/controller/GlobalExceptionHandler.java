package ru.ulstu.grade_analysis.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.ulstu.grade_analysis.dto.MessageResponse;
import ru.ulstu.grade_analysis.service.exception.BadRequestException;
import ru.ulstu.grade_analysis.service.exception.ForbiddenException;
import ru.ulstu.grade_analysis.service.exception.NotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<MessageResponse> handleNotFound(NotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(ex.getMessage()));
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<MessageResponse> handleBadRequest(BadRequestException ex) {
    return ResponseEntity.badRequest().body(new MessageResponse(ex.getMessage()));
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<MessageResponse> handleForbidden(ForbiddenException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(ex.getMessage()));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<MessageResponse> handleAuthentication(AuthenticationException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new MessageResponse("Неверный логин или пароль"));
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<MessageResponse> handleResponseStatusException(ResponseStatusException ex) {
    String message = ex.getReason() != null ? ex.getReason() : "Ошибка запроса";
    return ResponseEntity.status(ex.getStatusCode()).body(new MessageResponse(message));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<MessageResponse> handleDataIntegrityViolation(
      DataIntegrityViolationException ex) {
    String message = resolveIntegrityMessage(ex);
    return ResponseEntity.badRequest().body(new MessageResponse(message));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<MessageResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(
                error ->
                    error.getDefaultMessage() != null
                        ? error.getDefaultMessage()
                        : "Ошибка валидации")
            .orElse("Ошибка валидации");
    return ResponseEntity.badRequest().body(new MessageResponse(message));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<MessageResponse> handleNotReadable(HttpMessageNotReadableException ex) {
    return ResponseEntity.badRequest().body(new MessageResponse("Некорректный JSON"));
  }

  /**
   * Неизвестный адрес без точки в последнем сегменте считается клиентским маршрутом SPA: отдаём
   * {@code index.html}, а страницу «404» покажет React Router.
   */
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<?> handleNoResourceFound(
      NoResourceFoundException ex, HttpServletRequest request) throws IOException {
    if (!isSpaRoute(request.getRequestURI())) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new MessageResponse("Страница не найдена"));
    }

    try (InputStream index = new ClassPathResource("static/index.html").getInputStream()) {
      String html = new String(index.readAllBytes(), StandardCharsets.UTF_8);
      return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<MessageResponse> handleGenericException(Exception ex) {
    log.error("Необработанная ошибка", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new MessageResponse("Внутренняя ошибка сервера"));
  }

  private boolean isSpaRoute(String path) {
    if (path.startsWith("/api/")) {
      return false;
    }

    String lastSegment = path.substring(path.lastIndexOf('/') + 1);
    return !lastSegment.contains(".");
  }

  private String resolveIntegrityMessage(DataIntegrityViolationException ex) {
    String details = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

    if (details.contains("record_book_number")) {
      return "Студент с таким номером зачетной книжки уже существует";
    }
    if (details.contains("username")) {
      return "Пользователь с таким логином уже существует";
    }
    if (details.contains("uk_group_fullname")
        || details.contains("groups_prefix_course_subgroup")) {
      return "Группа с таким названием уже создана";
    }
    return "Ошибка базы данных. Проверьте уникальность полей.";
  }
}
