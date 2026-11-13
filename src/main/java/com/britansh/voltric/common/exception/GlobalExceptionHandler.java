
package com.britansh.voltric.common.exception;

import com.britansh.voltric.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception exception
    ) {

        ApiResponse<Void> response = ApiResponse.failure(
                "An unexpected error occurred"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException exception
    ) {

        ApiResponse<Void> response = ApiResponse.failure(
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {

        String errorMessage = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiResponse<Void> response = ApiResponse.failure(errorMessage);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedException exception
    ) {

        ApiResponse<Void> response = ApiResponse.failure(
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }



    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            BadRequestException exception
    ) {

        ApiResponse<Void> response = ApiResponse.failure(
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


}
