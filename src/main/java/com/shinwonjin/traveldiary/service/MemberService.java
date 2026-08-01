package com.shinwonjin.traveldiary.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shinwonjin.traveldiary.dto.member.MemberCreateRequest;
import com.shinwonjin.traveldiary.dto.member.MemberDeleteRequest;
import com.shinwonjin.traveldiary.dto.member.MemberLoginRequest;
import com.shinwonjin.traveldiary.dto.member.MemberLoginResponse;
import com.shinwonjin.traveldiary.dto.member.MemberPasswordChangeRequest;
import com.shinwonjin.traveldiary.dto.member.MemberResponse;
import com.shinwonjin.traveldiary.entity.Member;
import com.shinwonjin.traveldiary.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Transactional
    public MemberResponse createMember(MemberCreateRequest request) {
        validateDuplicateMember(request);

        String encodedPassword =
                passwordEncoder.encode(request.password());

        Member member = Member.create(
                request.name().trim(),
                request.email().trim(),
                encodedPassword,
                request.nickname().trim()
        );

        Member savedMember =
                memberRepository.save(member);

        return MemberResponse.from(savedMember);
    }

    @Transactional(readOnly = true)
    public MemberLoginResponse login(
            MemberLoginRequest request
    ) {
        Member member = memberRepository
                .findByEmail(request.email())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "이메일 또는 비밀번호가 올바르지 않습니다."
                        )
                );

        if (!passwordEncoder.matches(
                request.password(),
                member.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }

        String accessToken =
                jwtTokenService.createAccessToken(member);

        return new MemberLoginResponse(
                accessToken,
                "Bearer",
                jwtTokenService
                        .getAccessTokenExpirationSeconds(),
                MemberResponse.from(member)
        );
    }

    @Transactional
    public void changePassword(
            Long memberId,
            MemberPasswordChangeRequest request
    ) {
        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원 정보를 찾을 수 없습니다."
                        )
                );

        if (!passwordEncoder.matches(
                request.currentPassword(),
                member.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "현재 비밀번호가 올바르지 않습니다."
            );
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                member.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "새 비밀번호는 현재 비밀번호와 다르게 입력해 주세요."
            );
        }

        String encodedNewPassword =
                passwordEncoder.encode(
                        request.newPassword()
                );

        member.changePassword(encodedNewPassword);
    }

    @Transactional
    public void deleteMember(
            Long memberId,
            MemberDeleteRequest request
    ) {
        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원 정보를 찾을 수 없습니다."
                        )
                );

        if (!passwordEncoder.matches(
                request.password(),
                member.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "비밀번호가 올바르지 않습니다."
            );
        }

        memberRepository.delete(member);
    }

    private void validateDuplicateMember(
            MemberCreateRequest request
    ) {
        if (memberRepository.existsByEmail(
                request.email()
        )) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 이메일입니다."
            );
        }

        if (memberRepository.existsByNickname(
                request.nickname()
        )) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 닉네임입니다."
            );
        }
    }
}