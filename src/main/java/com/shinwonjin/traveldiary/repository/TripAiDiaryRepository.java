package com.shinwonjin.traveldiary.repository;

import com.shinwonjin.traveldiary.entity.TripAiDiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripAiDiaryRepository extends JpaRepository<TripAiDiary, Long> {

    Optional<TripAiDiary> findByTripId(Long tripId);

    void deleteByTripId(Long tripId);
}