package com.example.diplomnabackend.auth

import com.example.diplomnabackend.config.JwtService
import com.example.diplomnabackend.entity.User
import com.example.diplomnabackend.repository.UserRepository
import com.example.diplomnabackend.entity.enums.Role
import lombok.RequiredArgsConstructor
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
@RequiredArgsConstructor
class AuthenticationService (

    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager

) {

    fun register(request: RegisterRequest): AuthenticationResponse {

        val user = User.builder()
            .email(request.email)
            .password(passwordEncoder.encode(request.password))
            .role(Role.USER)
            .build()

        userRepository.save(user)

        var token = jwtService.generateToken(user)

        return AuthenticationResponse.builder()
            .token(token)
            .build()

    }

    fun authenticate(request: AuthenticationRequest): AuthenticationResponse {

        authenticationManager.authenticate(
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                request.email,
                request.password
            )
        )
        var user = userRepository.findByEmail(request.email)

        var token = jwtService.generateToken(user!!)

        return AuthenticationResponse.builder()
            .token(token)
            .build()

    }

}