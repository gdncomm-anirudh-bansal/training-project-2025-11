package com.Project.Member.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponseDTO {

    private Boolean success;
    private ProfileData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileData {
        private Long id;
        private String email;
        private String username;
        private String firstName;
        private String lastName;
        private String phone;
        private String status;
    }
}

