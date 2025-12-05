package com.Project.Member.DTO;

import lombok.Data;

@Data
public class LoginResponseDTO {

    private String message;
    private Boolean success;
    private Long userId;
    private String email;
}

