package com.shinwonjin.traveldiary.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shinwonjin.traveldiary.dto.home.RecentActivityResponse;
import com.shinwonjin.traveldiary.entity.Member;
import com.shinwonjin.traveldiary.entity.TripMemberRole;
import com.shinwonjin.traveldiary.entity.TripMemberStatus;
import com.shinwonjin.traveldiary.entity.TripPhoto;
import com.shinwonjin.traveldiary.repository.TripMemberRepository;
import com.shinwonjin.traveldiary.repository.TripPhotoRepository;
import com.shinwonjin.traveldiary.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeService {

    private static final int ACTIVITY_GROUP_MINUTES = 10;
    private static final int RECENT_ACTIVITY_LIMIT = 5;

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final TripPhotoRepository tripPhotoRepository;

    @Transactional(readOnly = true)
    public List<RecentActivityResponse> getRecentActivities(Long memberId) {
        List<Long> tripIds = getAccessibleTripIds(memberId);

        if (tripIds.isEmpty()) {
            return List.of();
        }

        List<TripPhoto> photos =
                tripPhotoRepository
                        .findTop100ByTripIdInOrderByCreatedAtDesc(
                                tripIds
                        );

        List<ActivityGroup> groups = new ArrayList<>();
        Map<ActivityKey, ActivityGroup> latestGroupByKey =
                new HashMap<>();

        for (TripPhoto photo : photos) {
            Member uploadedBy = photo.getUploadedBy();

            if (uploadedBy == null) {
                continue;
            }

            ActivityKey key = new ActivityKey(
                    photo.getTrip().getId(),
                    uploadedBy.getId()
            );

            ActivityGroup group =
                    latestGroupByKey.get(key);

            boolean canJoinGroup =
                    group != null
                    && !photo.getCreatedAt().isBefore(
                            group.createdAt.minusMinutes(
                                    ACTIVITY_GROUP_MINUTES
                            )
                    );

            if (canJoinGroup) {
                group.photoCount++;
                continue;
            }

            ActivityGroup newGroup = new ActivityGroup(
                    photo.getTrip().getId(),
                    photo.getTrip().getTitle(),
                    uploadedBy.getNickname(),
                    photo.getCreatedAt()
            );

            groups.add(newGroup);
            latestGroupByKey.put(key, newGroup);
        }

        return groups.stream()
                .sorted(
                        Comparator.comparing(
                                (ActivityGroup group) ->
                                        group.createdAt
                        ).reversed()
                )
                .limit(RECENT_ACTIVITY_LIMIT)
                .map(group ->
                        new RecentActivityResponse(
                                group.tripId,
                                group.tripTitle,
                                group.actorNickname,
                                group.photoCount,
                                group.createdAt
                        )
                )
                .toList();
    }

    private List<Long> getAccessibleTripIds(Long memberId) {
        List<Long> ownedTripIds = tripRepository
                .findAllByOwnerIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(trip -> trip.getId())
                .toList();

        List<Long> participatingTripIds = tripMemberRepository
                .findAllByMemberIdAndRoleAndStatusOrderByCreatedAtDesc(
                        memberId,
                        TripMemberRole.MEMBER,
                        TripMemberStatus.ACCEPTED
                )
                .stream()
                .map(tripMember ->
                        tripMember.getTrip().getId()
                )
                .toList();

        return Stream.concat(
                ownedTripIds.stream(),
                participatingTripIds.stream()
        )
                .distinct()
                .toList();
    }

    private record ActivityKey(
            Long tripId,
            Long memberId
    ) {
    }

    private static class ActivityGroup {

        private final Long tripId;
        private final String tripTitle;
        private final String actorNickname;
        private final java.time.LocalDateTime createdAt;
        private int photoCount = 1;

        private ActivityGroup(
                Long tripId,
                String tripTitle,
                String actorNickname,
                java.time.LocalDateTime createdAt
        ) {
            this.tripId = tripId;
            this.tripTitle = tripTitle;
            this.actorNickname = actorNickname;
            this.createdAt = createdAt;
        }
    }
}