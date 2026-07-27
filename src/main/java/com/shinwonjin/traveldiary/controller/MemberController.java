package com.shinwonjin.traveldiary.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shinwonjin.traveldiary.dto.member.MemberCreateRequest;
import com.shinwonjin.traveldiary.dto.member.MemberLoginRequest;
import com.shinwonjin.traveldiary.dto.member.MemberLoginResponse;
import com.shinwonjin.traveldiary.dto.member.MemberResponse;
import com.shinwonjin.traveldiary.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberResponse> createMember(
            @Valid @RequestBody MemberCreateRequest request
    ) {
        MemberResponse response =
                memberService.createMember(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<MemberLoginResponse> login(
            @Valid @RequestBody MemberLoginRequest request
    ) {
        MemberLoginResponse response =
                memberService.login(request);

        return ResponseEntity.ok(response);
    }
}