package com.shinwonjin.traveldiary.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.shinwonjin.traveldiary.dto.member.MemberCreateRequest;
import com.shinwonjin.traveldiary.dto.member.MemberDeleteRequest;
import com.shinwonjin.traveldiary.dto.member.MemberLoginRequest;
import com.shinwonjin.traveldiary.dto.member.MemberLoginResponse;
import com.shinwonjin.traveldiary.dto.member.MemberPasswordChangeRequest;
import com.shinwonjin.traveldiary.dto.member.MemberProfileUpdateRequest;
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

    @PatchMapping("/me")
    public ResponseEntity<MemberResponse> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MemberProfileUpdateRequest request
    ) {
        Long memberId =
                Long.valueOf(jwt.getSubject());

        MemberResponse response =
                memberService.updateProfile(
                        memberId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping(
            value = "/me/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MemberResponse> uploadProfileImage(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("file") MultipartFile file
    ) {
        Long memberId =
                Long.valueOf(jwt.getSubject());

        MemberResponse response =
                memberService.uploadProfileImage(
                        memberId,
                        file
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me/profile-image")
    public ResponseEntity<MemberResponse> resetProfileImage(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long memberId =
                Long.valueOf(jwt.getSubject());

        MemberResponse response =
                memberService.resetProfileImage(
                        memberId
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MemberPasswordChangeRequest request
    ) {
        Long memberId =
                Long.valueOf(jwt.getSubject());

        memberService.changePassword(
                memberId,
                request
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMember(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody MemberDeleteRequest request
    ) {
        Long memberId =
                Long.valueOf(jwt.getSubject());

        memberService.deleteMember(
                memberId,
                request
        );

        return ResponseEntity.noContent().build();
    }
}