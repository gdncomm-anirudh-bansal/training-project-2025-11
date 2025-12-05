package com.Project.Member.Controller;

import com.Project.Member.DTO.LoginDTO;
import com.Project.Member.DTO.LoginResponseDTO;
import com.Project.Member.DTO.ProfileResponseDTO;
import com.Project.Member.DTO.RegisterDTO;
import com.Project.Member.DTO.RegisterResponseDTO;
import com.Project.Member.DTO.StatusResponseDTO;
import com.Project.Member.Entity.Member;
import com.Project.Member.Repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        member.setPassword(passwordEncoder.encode("Test@1234"));
        member.setFirstName("Test");
        member.setLastName("User");
        member.setPhone("1234567890");
        member.setStatus(Member.memberStatus.ACTIVE);
    }

    @Test
    void testRegisterMember_HappyFlow() throws Exception {

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(member);


        MvcResult result = mockMvc.perform(post("/api/member/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();


        String responseBody = result.getResponse().getContentAsString();
        RegisterResponseDTO response = objectMapper.readValue(responseBody, RegisterResponseDTO.class);
        
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertTrue(response.getMessage().contains("Member registered successfully"));
    }

    @Test
    void testLoginMember_HappyFlow() throws Exception {

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));


        MvcResult result = mockMvc.perform(post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();


        String responseBody = result.getResponse().getContentAsString();
        LoginResponseDTO response = objectMapper.readValue(responseBody, LoginResponseDTO.class);
        
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Login successful", response.getMessage());
        assertEquals(1L, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void testGetProfile_HappyFlow() throws Exception {

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
 // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/member/profile")
                        .header("X-User-Id", "1")
                        .header("X-Auth-Needed", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();


        String responseBody = result.getResponse().getContentAsString();
        ProfileResponseDTO response = objectMapper.readValue(responseBody, ProfileResponseDTO.class);
        
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertNotNull(response.getData());
        assertEquals(1L, response.getData().getId());
        assertEquals("test@example.com", response.getData().getEmail());
        assertEquals("testuser", response.getData().getUsername());
        assertEquals("Test", response.getData().getFirstName());
        assertEquals("User", response.getData().getLastName());
        assertEquals("1234567890", response.getData().getPhone());
    }

    @Test
    void testGetStatusByMemberId_HappyFlow() throws Exception {

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));


        MvcResult result = mockMvc.perform(get("/api/member/status/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();


        String responseBody = result.getResponse().getContentAsString();
        StatusResponseDTO response = objectMapper.readValue(responseBody, StatusResponseDTO.class);
        
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Status retrieved successfully", response.getMessage());
        assertEquals("active", response.getStatus());
        assertEquals(1L, response.getMemberId());
    }

    @Test
    void testGetLogout_HappyFlow() throws Exception {

        mockMvc.perform(get("/api/member/logout")
                        .header("X-User-Id", "1")
                        .header("X-Auth-Needed", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}

