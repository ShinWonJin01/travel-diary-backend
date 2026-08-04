package com.shinwonjin.traveldiary.repository;

import com.shinwonjin.traveldiary.entity.TripPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripPhotoRepository extends JpaRepository<TripPhoto, Long> {

    List<TripPhoto> findAllByTripIdOrderByCreatedAtAsc(Long tripId);

    void deleteAllByTripId(Long tripId);

    @Modifying
    @Query("""
            update TripPhoto photo
            set photo.uploadedBy = null
            where photo.uploadedBy.id = :memberId
            """)
    void clearUploadedByMemberId(
            @Param("memberId") Long memberId
    );
}