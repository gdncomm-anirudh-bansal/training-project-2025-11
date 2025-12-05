package com.Project.Member.Controller;

import com.Project.Member.DTO.LoginDTO;
import com.Project.Member.DTO.LoginResponseDTO;
import com.Project.Member.DTO.RegisterDTO;
import com.Project.Member.DTO.RegisterResponseDTO;
import com.Project.Member.Service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    private RegisterDTO registerDTO;
    private RegisterResponseDTO registerResponseDTO;
    private LoginDTO loginDTO;
    private LoginResponseDTO loginResponseDTO;

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
    }

    @Test
    void testRegisterMember_HappyFlow() {

        registerResponseDTO = new RegisterResponseDTO();
        registerResponseDTO.setSuccess(true);
        registerResponseDTO.setMessage("Member registered successfully with ID: 1");

        when(memberService.registerMember(any(RegisterDTO.class))).thenReturn(registerResponseDTO);


        ResponseEntity<RegisterResponseDTO> response = memberController.registerMember(registerDTO);


        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Member registered successfully with ID: 1", response.getBody().getMessage());
    }

    @Test
    void testRegisterMember_NegativeFlow() {

        registerResponseDTO = new RegisterResponseDTO();
        registerResponseDTO.setSuccess(false);
        registerResponseDTO.setMessage("Email already registered");

        when(memberService.registerMember(any(RegisterDTO.class))).thenReturn(registerResponseDTO);


        ResponseEntity<RegisterResponseDTO> response = memberController.registerMember(registerDTO);


        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().getSuccess());
        assertEquals("Email already registered", response.getBody().getMessage());
    }

    @Test
    void testLoginMember_HappyFlow() {

        loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setSuccess(true);
        loginResponseDTO.setMessage("Login successful");
        loginResponseDTO.setUserId(1L);
        loginResponseDTO.setEmail("test@example.com");

        when(memberService.loginMember(any(LoginDTO.class))).thenReturn(loginResponseDTO);


        ResponseEntity<LoginResponseDTO> response = memberController.loginMember(loginDTO);


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Login successful", response.getBody().getMessage());
        assertEquals(1L, response.getBody().getUserId());
        assertEquals("test@example.com", response.getBody().getEmail());
    }

    @Test
    void testLoginMember_NegativeFlow() {

        loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setSuccess(false);
        loginResponseDTO.setMessage("Invalid email or password");

        when(memberService.loginMember(any(LoginDTO.class))).thenReturn(loginResponseDTO);


        ResponseEntity<LoginResponseDTO> response = memberController.loginMember(loginDTO);


        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().getSuccess());
        assertEquals("Invalid email or password", response.getBody().getMessage());
    }
}

