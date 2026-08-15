package com.shinwonjin.traveldiary.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shinwonjin.traveldiary.dto.invitation.TripInvitationRequest;
import com.shinwonjin.traveldiary.dto.invitation.TripInvitationResponse;
import com.shinwonjin.traveldiary.entity.Member;
import com.shinwonjin.traveldiary.entity.NotificationType;
import com.shinwonjin.traveldiary.entity.Trip;
import com.shinwonjin.traveldiary.entity.TripMember;
import com.shinwonjin.traveldiary.entity.TripMemberRole;
import com.shinwonjin.traveldiary.entity.TripMemberStatus;
import com.shinwonjin.traveldiary.repository.MemberRepository;
import com.shinwonjin.traveldiary.repository.TripMemberRepository;
import com.shinwonjin.traveldiary.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripInvitationService {

    private final TripRepository tripRepository;
    private final MemberRepository memberRepository;
    private final TripMemberRepository tripMemberRepository;
    private final NotificationService notificationService;

    /**
     * 여행에 회원 초대
     */
    @Transactional
    public TripInvitationResponse inviteMember(
            Long ownerId,
            Long tripId,
            TripInvitationRequest request
    ) {
        Trip trip = tripRepository
                .findByIdAndOwnerId(tripId, ownerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "여행 정보를 찾을 수 없거나 초대 권한이 없습니다."
                        )
                );

        String nickname = request.nickname().strip();

        Member invitee = memberRepository
                .findByNickname(nickname)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 닉네임을 사용하는 회원을 찾을 수 없습니다."
                        )
                );

        if (ownerId.equals(invitee.getId())) {
            throw new IllegalArgumentException(
                    "자기 자신은 초대할 수 없습니다."
            );
        }

        boolean alreadyRegistered =
                tripMemberRepository
                        .existsByTripIdAndMemberId(
                                tripId,
                                invitee.getId()
                        );

        if (alreadyRegistered) {
            throw new IllegalArgumentException(
                    "이미 초대했거나 여행에 참여 중인 회원입니다."
            );
        }

        TripMember invitation =
                TripMember.createInvitation(
                        trip,
                        invitee
                );

        TripMember savedInvitation =
                tripMemberRepository.save(invitation);

        Member owner = trip.getOwner();

        notificationService.createNotification(
                invitee,
                owner,
                trip,
                NotificationType.TRIP_INVITED,
                owner.getNickname()
                        + "님이 '"
                        + trip.getTitle()
                        + "'에 초대했습니다."
        );

        long currentParticipantCount =
                countAcceptedParticipants(tripId);

        return TripInvitationResponse.from(
                savedInvitation,
                currentParticipantCount
        );
    }

    /**
     * 내가 받은 대기 중인 초대 조회
     */
    @Transactional(readOnly = true)
    public List<TripInvitationResponse>
    getReceivedInvitations(Long memberId) {

        return tripMemberRepository
                .findAllByMemberIdAndRoleAndStatusOrderByCreatedAtDesc(
                        memberId,
                        TripMemberRole.MEMBER,
                        TripMemberStatus.PENDING
                )
                .stream()
                .map(tripMember ->
                        TripInvitationResponse.from(
                                tripMember,
                                countAcceptedParticipants(
                                        tripMember
                                                .getTrip()
                                                .getId()
                                )
                        )
                )
                .toList();
    }

    /**
     * 내가 보낸 초대 조회
     */
    @Transactional(readOnly = true)
    public List<TripInvitationResponse>
    getSentInvitations(Long ownerId) {

        return tripMemberRepository
                .findAllByTrip_Owner_IdAndRoleOrderByCreatedAtDesc(
                        ownerId,
                        TripMemberRole.MEMBER
                )
                .stream()
                .map(tripMember ->
                        TripInvitationResponse.from(
                                tripMember,
                                countAcceptedParticipants(
                                        tripMember
                                                .getTrip()
                                                .getId()
                                )
                        )
                )
                .toList();
    }

    /**
     * 초대 수락
     */
    @Transactional
    public TripInvitationResponse acceptInvitation(
            Long memberId,
            Long invitationId
    ) {
        TripMember invitation =
                findMyInvitation(
                        memberId,
                        invitationId
                );

        invitation.accept();

        Trip trip = invitation.getTrip();

        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원 정보를 찾을 수 없습니다."
                        )
                );

        notificationService.createNotification(
                trip.getOwner(),
                member,
                trip,
                NotificationType.INVITATION_ACCEPTED,
                member.getNickname()
                        + "님이 '"
                        + trip.getTitle()
                        + "' 초대를 수락했습니다."
        );

        long currentParticipantCount =
                countAcceptedParticipants(trip.getId());

        return TripInvitationResponse.from(
                invitation,
                currentParticipantCount
        );
    }

    /**
     * 초대 거절
     */
    @Transactional
    public TripInvitationResponse declineInvitation(
            Long memberId,
            Long invitationId
    ) {
        TripMember invitation =
                findMyInvitation(
                        memberId,
                        invitationId
                );

        invitation.decline();

        Trip trip = invitation.getTrip();

        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원 정보를 찾을 수 없습니다."
                        )
                );

        notificationService.createNotification(
                trip.getOwner(),
                member,
                trip,
                NotificationType.INVITATION_REJECTED,
                member.getNickname()
                        + "님이 '"
                        + trip.getTitle()
                        + "' 초대를 거절했습니다."
        );

        long currentParticipantCount =
                countAcceptedParticipants(trip.getId());

        return TripInvitationResponse.from(
                invitation,
                currentParticipantCount
        );
    }

    /**
     * 보낸 초대 취소
     */
    @Transactional
    public void cancelInvitation(
            Long ownerId,
            Long invitationId
    ) {
        TripMember invitation =
                tripMemberRepository
                        .findByIdAndTrip_Owner_Id(
                                invitationId,
                                ownerId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "보낸 초대 정보를 찾을 수 없습니다."
                                )
                        );

        if (invitation.getRole()
                != TripMemberRole.MEMBER) {
            throw new IllegalArgumentException(
                    "여행 생성자 정보는 취소할 수 없습니다."
            );
        }

        if (invitation.getStatus()
                != TripMemberStatus.PENDING) {
            throw new IllegalArgumentException(
                    "대기 중인 초대만 취소할 수 있습니다."
            );
        }

        tripMemberRepository.delete(invitation);
    }

    private TripMember findMyInvitation(
            Long memberId,
            Long invitationId
    ) {
        TripMember invitation =
                tripMemberRepository
                        .findByIdAndMemberId(
                                invitationId,
                                memberId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "초대 정보를 찾을 수 없습니다."
                                )
                        );

        if (invitation.getRole()
                != TripMemberRole.MEMBER) {
            throw new IllegalArgumentException(
                    "처리할 수 없는 초대 정보입니다."
            );
        }

        return invitation;
    }

    private long countAcceptedParticipants(
            Long tripId
    ) {
        return tripMemberRepository
                .countByTripIdAndStatus(
                        tripId,
                        TripMemberStatus.ACCEPTED
                );
    }
}