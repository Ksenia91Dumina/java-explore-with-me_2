package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import ru.practicum.controllers.AdminController;
import ru.practicum.controllers.PrivateController;
import ru.practicum.controllers.PublicController;


@Slf4j
@RestControllerAdvice(assignableTypes = {AdminController.class, PublicController.class, PrivateController.class})
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestException(final BadRequestException e) {
        log.info("400 {}", e.getMessage(), e);
        return new ErrorResponse(
            "400 - BAD_REQUEST",
            e.getMessage(),
            ErrorStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException e) {
        log.info("404 {}", e.getMessage(), e);
        return new ErrorResponse(
            "404 - NOT_FOUND",
            e.getMessage(),
            ErrorStatus.NOT_FOUND);
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictingRequestException(final ConflictException e) {
        log.info("409 {}", e.getMessage(), e);
        return new ErrorResponse(
            "409 - CONFLICT",
            e.getMessage(),
            ErrorStatus.CONFLICT);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerError(final HttpServerErrorException.InternalServerError e) {
        log.info("500 {}", e.getMessage(), e);
        return new ErrorResponse(
            "500 - INTERNAL_SERVER_ERROR",
            e.getMessage(),
            ErrorStatus.INTERNAL_SERVER_ERROR);
    }
}
