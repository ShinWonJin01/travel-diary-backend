package com.shinwonjin.traveldiary.service;

import com.shinwonjin.traveldiary.dto.location.LocationSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class KakaoLocationSearchService {

    private static final int RESULT_LIMIT = 5;

    private final RestClient restClient;

    public KakaoLocationSearchService(
            @Value("${kakao.rest-api-key}") String restApiKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "KakaoAK " + restApiKey
                )
                .build();
    }

    public List<LocationSearchResponse> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String trimmedQuery = query.trim();

        try {
            List<LocationSearchResponse> results = new ArrayList<>();

            results.addAll(searchByKeyword(trimmedQuery));
            results.addAll(searchByAddress(trimmedQuery));

            return removeDuplicates(results)
                    .stream()
                    .limit(RESULT_LIMIT)
                    .toList();

        } catch (RestClientResponseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "장소 검색 외부 API 요청에 실패했습니다."
            );

        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "장소 검색 외부 API에 연결할 수 없습니다."
            );
        }
    }

    private List<LocationSearchResponse> searchByKeyword(String query) {
        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", query)
                        .queryParam("size", RESULT_LIMIT)
                        .build())
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.has("documents")) {
            return List.of();
        }

        List<LocationSearchResponse> results = new ArrayList<>();

        for (JsonNode document : response.get("documents")) {
            String id = "place:" + document.path("id").asText();

            String name = document.path("place_name").asText();

            String roadAddress =
                    document.path("road_address_name").asText();

            String address =
                    document.path("address_name").asText();

            String displayAddress =
                    !roadAddress.isBlank()
                            ? roadAddress
                            : address;

            double longitude =
                    document.path("x").asDouble();

            double latitude =
                    document.path("y").asDouble();

            results.add(
                    new LocationSearchResponse(
                            id,
                            name,
                            displayAddress,
                            latitude,
                            longitude
                    )
            );
        }

        return results;
    }

    private List<LocationSearchResponse> searchByAddress(String query) {
        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/address.json")
                        .queryParam("query", query)
                        .queryParam("size", RESULT_LIMIT)
                        .build())
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.has("documents")) {
            return List.of();
        }

        List<LocationSearchResponse> results = new ArrayList<>();

        for (JsonNode document : response.get("documents")) {
            String addressName =
                    document.path("address_name").asText();

            double longitude =
                    document.path("x").asDouble();

            double latitude =
                    document.path("y").asDouble();

            String id =
                    "address:"
                            + longitude
                            + ":"
                            + latitude;

            results.add(
                    new LocationSearchResponse(
                            id,
                            addressName,
                            addressName,
                            latitude,
                            longitude
                    )
            );
        }

        return results;
    }

    private List<LocationSearchResponse> removeDuplicates(
            List<LocationSearchResponse> results
    ) {
        Map<String, LocationSearchResponse> uniqueResults =
                new LinkedHashMap<>();

        for (LocationSearchResponse result : results) {
            String key =
                    result.latitude()
                            + ","
                            + result.longitude();

            uniqueResults.putIfAbsent(key, result);
        }

        return new ArrayList<>(uniqueResults.values());
    }
}