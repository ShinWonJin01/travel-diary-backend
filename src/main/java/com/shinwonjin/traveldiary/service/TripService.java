package com.shinwonjin.traveldiary.service;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shinwonjin.traveldiary.dto.trip.TripCreateRequest;
import com.shinwonjin.traveldiary.dto.trip.TripResponse;
import com.shinwonjin.traveldiary.entity.Member;
import com.shinwonjin.traveldiary.entity.Trip;
import com.shinwonjin.traveldiary.repository.MemberRepository;
import com.shinwonjin.traveldiary.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public TripResponse createTrip(
            Long memberId,
            TripCreateRequest request
    ) {
        validateTripDates(request);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원 정보를 찾을 수 없습니다."
                        )
                );

        Trip trip = Trip.create(
                member,
                request.title(),
                request.destination(),
                request.startDate(),
                request.endDate(),
                request.description()
        );

        Trip savedTrip = tripRepository.save(trip);

        return TripResponse.from(savedTrip);
    }

    @Transactional(readOnly = true)
    public List<TripResponse> getMyTrips(Long memberId) {

        return tripRepository
                .findAllByOwnerIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(TripResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripResponse getTrip(
            Long memberId,
            Long tripId
    ) {
        Trip trip = tripRepository
                .findByIdAndOwnerId(tripId, memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "여행 정보를 찾을 수 없습니다."
                        )
                );

        return TripResponse.from(trip);
    }

    private void validateTripDates(TripCreateRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException(
                    "여행 종료일은 시작일보다 빠를 수 없습니다."
            );
        }
    }
}