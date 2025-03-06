package com.teya.interviews.ledger.service

import com.teya.interviews.ledger.exceptions.ResourceNotFoundException
import com.teya.interviews.ledger.model.dto.CreateUserRequestDTO
import com.teya.interviews.ledger.model.dto.DTOConverter
import com.teya.interviews.ledger.model.dto.UserDTO
import com.teya.interviews.ledger.repository.UserRepository
import com.teya.interviews.ledger.repository.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService @Autowired constructor(private val userDAO: UserRepository) {
    fun listUsers(): List<UserDTO> {
        return userDAO.findAll().stream().map { a -> DTOConverter.MAPPER.map(a, UserDTO::class.java) }.toList()
    }

    fun getUserDetails(userId: String): UserDTO {
        val user = userDAO.findById(userId)
        if (!user.isPresent) {
            throw ResourceNotFoundException("User with id $userId not found")
        }
        return DTOConverter.MAPPER.map(user.get(), UserDTO::class.java)
    }

    fun createUser(user: CreateUserRequestDTO): UserDTO {
        val userId = UUID.randomUUID().toString()
        val userEntity = User(id = userId, name = user.name, email = user.email, phone = user.phone, role = user.role)
        val savedUser = userDAO.save(userEntity)
        return DTOConverter.MAPPER.map(savedUser, UserDTO::class.java)
    }

}