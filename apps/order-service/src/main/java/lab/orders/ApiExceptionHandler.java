package lab.orders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ProblemDetail invalidRequest(Exception exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request");
    }

    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail requestError(ResponseStatusException exception) {
        return ProblemDetail.forStatusAndDetail(exception.getStatusCode(), exception.getReason() == null ? "Request failed" : exception.getReason());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception exception) {
        log.atError().addKeyValue("event", "request.failed").addKeyValue("exception.type", exception.getClass().getSimpleName()).log("Unexpected request failure");
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected application error");
    }
}
