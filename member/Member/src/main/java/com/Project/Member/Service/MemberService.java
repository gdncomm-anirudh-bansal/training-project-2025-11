package com.Project.Member.Service;

import com.Project.Member.DTO.LoginDTO;
import com.Project.Member.DTO.LoginResponseDTO;
import com.Project.Member.DTO.ProfileResponseDTO;
import com.Project.Member.DTO.RegisterDTO;
import com.Project.Member.DTO.RegisterResponseDTO;
import com.Project.Member.DTO.StatusResponseDTO;

public interface MemberService  {

    RegisterResponseDTO registerMember(RegisterDTO registerDTO);
    LoginResponseDTO loginMember(LoginDTO loginDTO);
    ProfileResponseDTO getProfile(Long userId);
    StatusResponseDTO getStatusByMemberId(Long memberId);
}
