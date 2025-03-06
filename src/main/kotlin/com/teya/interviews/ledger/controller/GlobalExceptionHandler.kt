package com.teya.interviews.ledger.controller

import com.teya.interviews.ledger.exceptions.AuthenticationRequiredException
import com.teya.interviews.ledger.exceptions.AccessDeniedException
import com.teya.interviews.ledger.exceptions.InsufficientBalanceException
import com.teya.interviews.ledger.exceptions.ResourceNotFoundException
import com.teya.interviews.ledger.model.dto.ErrorResponseDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    //TODO: Localize error messages.
    @ExceptionHandler(AuthenticationRequiredException::class)
    fun handleAccessDenied(ex: AuthenticationRequiredException): ResponseEntity<ErrorResponseDTO> {
        val errorResponse = ErrorResponseDTO(
            message = ex.message ?: "Authentication required",
            errorCode = "MISSING_AUTHENTICATION",
        )
        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ErrorResponseDTO> {
        // Access denied implies someone trying to access a resource that does not belong to them,
        // so we should just return a 404 (not found), instead of 403 (forbidden) to avoid leaking
        // information about the existence of the resource
        val errorResponse = ErrorResponseDTO(
            message = "Resource not found",
            errorCode = "NOT_FOUND",
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(InsufficientBalanceException::class)
    fun handleInsufficientBalance(ex: InsufficientBalanceException): ResponseEntity<ErrorResponseDTO> {
        val errorResponse = ErrorResponseDTO(
            message = ex.message ?: "Insufficient Balance",
            errorCode = "INSUFFICIENT_BALANCE",
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponseDTO> {
        val errorResponse = ErrorResponseDTO(
            message = ex.message ?: "Resource not found",
            errorCode = "NOT_FOUND",
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArguments(ex: IllegalArgumentException): ResponseEntity<ErrorResponseDTO> {
        val errorResponse = ErrorResponseDTO(
            message = ex.message!!,
            errorCode = "INVALID_REQUEST",
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleIllegalArguments(ex: Exception): ResponseEntity<ErrorResponseDTO> {
        val errorResponse = ErrorResponseDTO(
            message = ex.message?: "An error occurred",
            errorCode = "SERVER_ERROR",
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}

