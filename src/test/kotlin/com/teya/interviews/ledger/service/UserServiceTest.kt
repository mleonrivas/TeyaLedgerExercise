package com.teya.interviews.ledger.service

import com.teya.interviews.ledger.exceptions.ResourceNotFoundException
import com.teya.interviews.ledger.model.UserRole

import com.teya.interviews.ledger.model.dto.CreateUserRequestDTO
import com.teya.interviews.ledger.repository.UserRepository
import com.teya.interviews.ledger.repository.entity.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.util.*

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        userService = UserService(userRepository)
    }

    @Test
    fun `listUsers should return list of users`() {
        val user = User(id = "user1", name = "Alice", email = "alice@teya.com", phone = "1234567890", role = UserRole.USER)
        whenever(userRepository.findAll()).thenReturn(listOf(user))

        val users = userService.listUsers()

        assertEquals(1, users.size)
        assertEquals("user1", users[0].id)
    }

    @Test
    fun `getUserDetails should return user details`() {
        val userId = "user1"
        val user = User(id = "user1", name = "Alice", email = "alice@teya.com", phone = "1234567890", role = UserRole.USER)
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        val userDetails = userService.getUserDetails(userId)

        assertEquals(userId, userDetails?.id)
    }

    @Test
    fun `getUserDetails should throw ResourceNotFoundException if user does not exist`() {
        val userId = "user1"
        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())

        assertThrows<ResourceNotFoundException> {
            userService.getUserDetails(userId)
        }
    }

    @Test
    fun `createUser should create and return user`() {
        val userId = "user1"
        val request = CreateUserRequestDTO(name = "John Doe", email = "john.doe@example.com", phone = "1234567890", role = UserRole.USER)
        val user = User(id = userId, name = request.name, email = request.email, phone = request.phone, role = request.role)
        whenever(userRepository.save(any(User::class.java))).thenReturn(user)

        val createdUser = userService.createUser(request)

        assertEquals(userId, createdUser.id)
    }
}