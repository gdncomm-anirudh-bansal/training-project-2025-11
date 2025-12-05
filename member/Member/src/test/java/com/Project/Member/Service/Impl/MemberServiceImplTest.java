package com.Project.Member.Service.Impl;

import com.Project.Member.DTO.LoginDTO;
import com.Project.Member.DTO.LoginResponseDTO;
import com.Project.Member.DTO.RegisterDTO;
import com.Project.Member.DTO.RegisterResponseDTO;
import com.Project.Member.Entity.Member;
import com.Project.Member.Repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;
    private Member member;

    @BeforeEach
    void setUp() {
        registerDTO = new RegisterDTO();
        registerDTO.setEmail("test@example.com");
        registerDTO.setUserName("testuser");
        registerDTO.setPassword("Test@1234");
        registerDTO.setFirstName("Test");
        registerDTO.setLastName("User");
        registerDTO.setPhone("1234567890");

        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@example.com");
        loginDTO.setPassword("Test@1234");

        member = new Member();
        member.setMemberId(1L);
        member.setEmail("test@example.com");
        member.setUserName("testuser");
        member.setPassword("$2a$10$hashedPassword");
        member.setFirstName("Test");
        member.setLastName("User");
        member.setPhone("1234567890");
        member.setStatus(Member.memberStatus.ACTIVE);
    }

    @Test
    void testRegisterMember_HappyFlow() {

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(member);


        RegisterResponseDTO response = memberService.registerMember(registerDTO);


        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertTrue(response.getMessage().contains("Member registered successfully"));
    }

    @Test
    void testRegisterMember_NegativeFlow_EmailAlreadyExists() {

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));


        RegisterResponseDTO response = memberService.registerMember(registerDTO);


        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals("Email already registered", response.getMessage());
    }

    @Test
    void testRegisterMember_NegativeFlow_InvalidEmail() {

        registerDTO.setEmail("invalid-email");


        RegisterResponseDTO response = memberService.registerMember(registerDTO);


        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals("Invalid email format", response.getMessage());
    }

    @Test
    void testLoginMember_HappyFlow() {
        // Arrange
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act
        LoginResponseDTO response = memberService.loginMember(loginDTO);

        // Assert
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals(1L, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void testLoginMember_NegativeFlow_InvalidCredentials() {
        // Arrange
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act
        LoginResponseDTO response = memberService.loginMember(loginDTO);

        // Assert
        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals("Invalid email or password", response.getMessage());
    }

    @Test
    void testLoginMember_NegativeFlow_EmailNotFound() {
        // Arrange
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        LoginResponseDTO response = memberService.loginMember(loginDTO);

        // Assert
        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals("Invalid email or password", response.getMessage());
    }

    @Test
    void testLoginMember_NegativeFlow_InvalidEmailFormat() {

        loginDTO.setEmail("invalid-email");


        LoginResponseDTO response = memberService.loginMember(loginDTO);


        assertNotNull(response);
        assertFalse(response.getSuccess());
        assertEquals("Invalid email format", response.getMessage());
    }
}

