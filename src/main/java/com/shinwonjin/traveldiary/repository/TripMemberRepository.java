package com.shinwonjin.traveldiary.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shinwonjin.traveldiary.entity.TripMember;
import com.shinwonjin.traveldiary.entity.TripMemberRole;
import com.shinwonjin.traveldiary.entity.TripMemberStatus;

public interface TripMemberRepository
        extends JpaRepository<TripMember, Long> {

    boolean existsByTripIdAndMemberIdAndStatus(
            Long tripId,
            Long memberId,
            TripMemberStatus status
    );

    boolean existsByTripIdAndMemberId(
            Long tripId,
            Long memberId
    );

    Optional<TripMember> findByTripIdAndMemberId(
            Long tripId,
            Long memberId
    );

    Optional<TripMember> findByIdAndMemberId(
            Long invitationId,
            Long memberId
    );

    Optional<TripMember> findByIdAndTrip_Owner_Id(
            Long invitationId,
            Long ownerId
    );

    List<TripMember>
    findAllByMemberIdAndStatusOrderByCreatedAtDesc(
            Long memberId,
            TripMemberStatus status
    );

    List<TripMember>
    findAllByMemberIdAndRoleAndStatusOrderByCreatedAtDesc(
            Long memberId,
            TripMemberRole role,
            TripMemberStatus status
    );

    List<TripMember>
    findAllByTrip_Owner_IdAndRoleOrderByCreatedAtDesc(
            Long ownerId,
            TripMemberRole role
    );

    List<TripMember>
    findAllByTripIdAndStatusOrderByCreatedAtAsc(
            Long tripId,
            TripMemberStatus status
    );

    long countByTripIdAndStatus(
            Long tripId,
            TripMemberStatus status
    );

    long countByMemberIdAndRoleAndStatus(
            Long memberId,
            TripMemberRole role,
            TripMemberStatus status
    );

    void deleteAllByTripId(Long tripId);
}