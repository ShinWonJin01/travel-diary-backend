package com.shinwonjin.traveldiary.controller;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public Map<String, Object> getCurrentMember(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return Map.of(
                "memberId", Long.valueOf(jwt.getSubject()),
                "email", jwt.getClaimAsString("email"),
                "nickname", jwt.getClaimAsString("nickname")
        );
    }
}