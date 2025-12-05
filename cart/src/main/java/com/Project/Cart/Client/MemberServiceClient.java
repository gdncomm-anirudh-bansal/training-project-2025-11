package com.Project.Cart.Client;

import com.Project.Cart.DTO.MemberStatusDTO;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface MemberServiceClient {

    @RequestLine("GET /api/member/status/{memberId}")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    MemberStatusDTO getMemberStatus(@Param("memberId") Long memberId);
}

