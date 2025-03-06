package com.teya.interviews.ledger.controller

import com.teya.interviews.ledger.context.AuthHelper
import com.teya.interviews.ledger.model.dto.CreateUserRequestDTO
import com.teya.interviews.ledger.model.dto.UserDTO
import com.teya.interviews.ledger.service.UserService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController @Autowired constructor(private val userService: UserService){

    @Operation(summary = "Create a new user", description = "Creates a new user, only available to ADMIN users")
    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequestDTO): ResponseEntity<UserDTO> {
        AuthHelper.requireAdmin()
        val result = userService.createUser(request)
        return ResponseEntity.ok(result)
    }

    @Operation(summary = "List existing users", description = "Retrieves all users, only available to ADMIN users")
    @GetMapping
    fun listUsers(): ResponseEntity<List<UserDTO>> {
        AuthHelper.requireAdmin()
        val result = userService.listUsers()
        return ResponseEntity.ok(result)
    }
}