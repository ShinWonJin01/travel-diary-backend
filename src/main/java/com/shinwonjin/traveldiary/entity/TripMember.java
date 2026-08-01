package com.shinwonjin.traveldiary.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "trip_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_trip_member",
                        columnNames = {
                                "trip_id",
                                "member_id"
                        }
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "trip_id",
            nullable = false
    )
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "member_id",
            nullable = false
    )
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private TripMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private TripMemberStatus status;

    @CreationTimestamp
    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    private TripMember(
            Trip trip,
            Member member,
            TripMemberRole role,
            TripMemberStatus status
    ) {
        this.trip = trip;
        this.member = member;
        this.role = role;
        this.status = status;
    }

    public static TripMember createOwner(
            Trip trip,
            Member owner
    ) {
        return new TripMember(
                trip,
                owner,
                TripMemberRole.OWNER,
                TripMemberStatus.ACCEPTED
        );
    }

    public static TripMember createInvitation(
            Trip trip,
            Member member
    ) {
        return new TripMember(
                trip,
                member,
                TripMemberRole.MEMBER,
                TripMemberStatus.PENDING
        );
    }

    public void accept() {
        if (status != TripMemberStatus.PENDING) {
            throw new IllegalStateException(
                    "대기 중인 초대만 수락할 수 있습니다."
            );
        }

        status = TripMemberStatus.ACCEPTED;
    }

    public void decline() {
        if (status != TripMemberStatus.PENDING) {
            throw new IllegalStateException(
                    "대기 중인 초대만 거절할 수 있습니다."
            );
        }

        status = TripMemberStatus.DECLINED;
    }
}