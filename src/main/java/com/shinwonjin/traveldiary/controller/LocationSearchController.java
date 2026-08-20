package com.shinwonjin.traveldiary.controller;

import com.shinwonjin.traveldiary.dto.location.LocationSearchResponse;
import com.shinwonjin.traveldiary.service.KakaoLocationSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationSearchController {

    private final KakaoLocationSearchService kakaoLocationSearchService;

    public LocationSearchController(
            KakaoLocationSearchService kakaoLocationSearchService
    ) {
        this.kakaoLocationSearchService = kakaoLocationSearchService;
    }

    @GetMapping("/search")
    public List<LocationSearchResponse> searchLocations(
            @RequestParam String query
    ) {
        return kakaoLocationSearchService.search(query);
    }
}