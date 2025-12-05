package com.Project.Member.Service.Impl;

import com.Project.Member.DTO.LoginDTO;
import com.Project.Member.DTO.LoginResponseDTO;
import com.Project.Member.DTO.ProfileResponseDTO;
import com.Project.Member.DTO.RegisterDTO;
import com.Project.Member.DTO.RegisterResponseDTO;
import com.Project.Member.DTO.StatusResponseDTO;
import com.Project.Member.Entity.Member;
import com.Project.Member.Repository.MemberRepository;
import com.Project.Member.Service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class MemberServiceImpl implements MemberService {

    private static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return pattern.matcher(email).matches();
    }

    @Override
    @CacheEvict(value = "memberStatus", allEntries = true)
    public RegisterResponseDTO registerMember(RegisterDTO registerDTO) {

        // Validate email format
        if (!isValidEmail(registerDTO.getEmail())) {
            RegisterResponseDTO response = new RegisterResponseDTO();
            response.setSuccess(false);
            response.setMessage("Invalid email format");
            return response;
        }

        //  email already exists
        Optional<Member> existingMember = memberRepository.findByEmail(registerDTO.getEmail());
        if (existingMember.isPresent()) {
            RegisterResponseDTO response = new RegisterResponseDTO();
            response.setSuccess(false);
            response.setMessage("Email already registered");
            return response;
        }

        Member member = new Member();
        member.setEmail(registerDTO.getEmail());
        member.setUserName(registerDTO.getUserName());
        // Hash password
        member.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        member.setFirstName(registerDTO.getFirstName());
        member.setLastName(registerDTO.getLastName());
        member.setPhone(registerDTO.getPhone());

        Member savedMember = memberRepository.save(member);

        RegisterResponseDTO response = new RegisterResponseDTO();
        if (savedMember != null && savedMember.getMemberId() != null) {
            response.setSuccess(true);
            response.setMessage("Member registered successfully with ID: " + savedMember.getMemberId());
        } else {
            response.setSuccess(false);
            response.setMessage("Failed to register member");
        }

        return response;
    }

    @Override
    public LoginResponseDTO loginMember(LoginDTO loginDTO) {
        LoginResponseDTO response = new LoginResponseDTO();


        if (!isValidEmail(loginDTO.getEmail())) {
            response.setSuccess(false);
            response.setMessage("Invalid email format");
            return response;
        }


        Optional<Member> memberOptional = memberRepository.findByEmail(loginDTO.getEmail());
        if (memberOptional.isEmpty()) {
            response.setSuccess(false);
            response.setMessage("Invalid email or password");
            return response;
        }

        Member member = memberOptional.get();


        if (!passwordEncoder.matches(loginDTO.getPassword(), member.getPassword())) {
            response.setSuccess(false);
            response.setMessage("Invalid email or password");
            return response;
        }


        response.setSuccess(true);
        response.setMessage("Login successful");
        response.setUserId(member.getMemberId());
        response.setEmail(member.getEmail());

        return response;
    }

    @Override
    public ProfileResponseDTO getProfile(Long userId) {
        ProfileResponseDTO response = new ProfileResponseDTO();

        Optional<Member> memberOptional = memberRepository.findById(userId);
        if (memberOptional.isEmpty()) {
            response.setSuccess(false);
            return response;
        }

        Member member = memberOptional.get();
        ProfileResponseDTO.ProfileData profileData = new ProfileResponseDTO.ProfileData();
        profileData.setId(member.getMemberId());
        profileData.setEmail(member.getEmail());
        profileData.setUsername(member.getUserName());
        profileData.setFirstName(member.getFirstName());
        profileData.setLastName(member.getLastName());
        profileData.setPhone(member.getPhone());
        profileData.setStatus(member.getStatus() != null ? member.getStatus().name().toLowerCase() : "active");

        response.setSuccess(true);
        response.setData(profileData);

        return response;
    }

    @Override
    @Cacheable(value = "memberStatus", key = "#memberId")
    public StatusResponseDTO getStatusByMemberId(Long memberId) {
        StatusResponseDTO response = new StatusResponseDTO();

        Optional<Member> memberOptional = memberRepository.findById(memberId);
        if (memberOptional.isEmpty()) {
            response.setSuccess(false);
            response.setMessage("Member not found");
            response.setMemberId(memberId);
            return response;
        }

        Member member = memberOptional.get();
        String status = member.getStatus() != null ? member.getStatus().name().toLowerCase() : "active";

        response.setSuccess(true);
        response.setMessage("Status retrieved successfully");
        response.setStatus(status);
        response.setMemberId(member.getMemberId());

        return response;
    }
}
