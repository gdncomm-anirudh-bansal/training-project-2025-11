package com.Project.Member.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusResponseDTO {
    private Boolean success;
    private String message;
    private String status;
    private Long memberId;
}

