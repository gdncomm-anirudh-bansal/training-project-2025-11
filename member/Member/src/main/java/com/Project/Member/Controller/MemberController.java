package com.Project.Member.Controller;


import com.Project.Member.DTO.*;
import com.Project.Member.Service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
@CrossOrigin(origins = "*")
public class MemberController {

    @Autowired
    MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> registerMember(@Valid @RequestBody RegisterDTO registerDTO) {

        RegisterResponseDTO registerResponseDTO = memberService.registerMember(registerDTO);

        if (registerResponseDTO.getSuccess()) {
            return new ResponseEntity<>(registerResponseDTO, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(registerResponseDTO, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginMember(@Valid @RequestBody LoginDTO loginDTO) {

        LoginResponseDTO loginResponseDTO = memberService.loginMember(loginDTO);

        if (loginResponseDTO.getSuccess()) {
            return new ResponseEntity<>(loginResponseDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(loginResponseDTO, HttpStatus.UNAUTHORIZED);
        }
    }



    @GetMapping("/logout")
    public ResponseEntity<LogoutDTO> getLogout(@RequestHeader("X-User-Id") String userIdHeader, @RequestHeader(value ="X-Auth-Needed",defaultValue = "true") String isAuthNeeded) {
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            LogoutDTO logoutDTO = new LogoutDTO();
            logoutDTO.setMessage("Provide the User");
            return new ResponseEntity<>(logoutDTO, HttpStatus.BAD_REQUEST);
        }

        LogoutDTO logoutDTO = new LogoutDTO();
        logoutDTO.setMessage("Logout Success");
        return new ResponseEntity<>(logoutDTO, HttpStatus.OK);


    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDTO> getProfile(@RequestHeader("X-User-Id") String userIdHeader, @RequestHeader(value ="X-Auth-Needed",defaultValue = "true") String isAuthNeeded) {
        if (userIdHeader == null || userIdHeader.isEmpty()) {
            ProfileResponseDTO response = new ProfileResponseDTO();
            response.setSuccess(false);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            Long userId = Long.parseLong(userIdHeader);
            ProfileResponseDTO profileResponseDTO = memberService.getProfile(userId);

            if (profileResponseDTO.getSuccess()) {
                return new ResponseEntity<>(profileResponseDTO, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(profileResponseDTO, HttpStatus.NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            ProfileResponseDTO response = new ProfileResponseDTO();
            response.setSuccess(false);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/status/{memberId}")
    public ResponseEntity<StatusResponseDTO> getStatusByMemberId(@PathVariable Long memberId) {
        StatusResponseDTO statusResponseDTO = memberService.getStatusByMemberId(memberId);

        if (statusResponseDTO.getSuccess()) {
            return new ResponseEntity<>(statusResponseDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(statusResponseDTO, HttpStatus.NOT_FOUND);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RegisterResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        RegisterResponseDTO response = new RegisterResponseDTO();
        response.setSuccess(false);
        response.setMessage("Validation failed: " + errors.values().iterator().next());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
