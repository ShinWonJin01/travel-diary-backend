package com.shinwonjin.traveldiary.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class ReverseGeocodingService {

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org/reverse";

    private static final long MIN_REQUEST_INTERVAL = 1000L;

    private final RestClient kakaoRestClient;

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private long lastRequestTime = 0L;

    public ReverseGeocodingService(
            @Value("${kakao.rest-api-key}") String restApiKey
    ) {
        this.kakaoRestClient = RestClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "KakaoAK " + restApiKey
                )
                .build();
    }

    public String getLocationName(
            Double latitude,
            Double longitude
    ) {
        if (latitude == null || longitude == null) {
            return null;
        }

        String kakaoLocation =
                getKakaoLocationName(
                        latitude,
                        longitude
                );

        if (
                kakaoLocation != null
                && !kakaoLocation.isBlank()
        ) {
            return kakaoLocation;
        }

        return getNominatimLocationName(
                latitude,
                longitude
        );
    }

    private String getKakaoLocationName(
            Double latitude,
            Double longitude
    ) {
        try {
            JsonNode response =
                    kakaoRestClient.get()
                            .uri(uriBuilder ->
                                    uriBuilder
                                            .path(
                                                    "/v2/local/geo/coord2address.json"
                                            )
                                            .queryParam(
                                                    "x",
                                                    longitude
                                            )
                                            .queryParam(
                                                    "y",
                                                    latitude
                                            )
                                            .queryParam(
                                                    "input_coord",
                                                    "WGS84"
                                            )
                                            .build()
                            )
                            .retrieve()
                            .body(JsonNode.class);

            if (
                    response == null
                    || !response.has("documents")
                    || response.get("documents").isEmpty()
            ) {
                return null;
            }

            JsonNode document =
                    response.get("documents").get(0);

            JsonNode roadAddress =
                    document.get("road_address");

            if (
                    roadAddress != null
                    && !roadAddress.isNull()
            ) {
                String buildingName =
                        getText(
                                roadAddress,
                                "building_name"
                        );

                String roadAddressName =
                        getText(
                                roadAddress,
                                "address_name"
                        );

                if (
                        buildingName != null
                        && !buildingName.isBlank()
                ) {
                    return buildingName;
                }

                if (
                        roadAddressName != null
                        && !roadAddressName.isBlank()
                ) {
                    return roadAddressName;
                }
            }

            JsonNode address =
                    document.get("address");

            if (
                    address != null
                    && !address.isNull()
            ) {
                return getText(
                        address,
                        "address_name"
                );
            }

            return null;

        } catch (RestClientResponseException exception) {
            return null;
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private synchronized String getNominatimLocationName(
            Double latitude,
            Double longitude
    ) {
        waitForRateLimit();

        String url = NOMINATIM_URL
                + "?format=jsonv2"
                + "&lat=" + latitude
                + "&lon=" + longitude
                + "&accept-language=ko,en";

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header(
                                "User-Agent",
                                "travel-diary-backend/1.0"
                        )
                        .GET()
                        .build();

        try {
            lastRequestTime =
                    System.currentTimeMillis();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {
                return null;
            }

            JsonNode root =
                    objectMapper.readTree(
                            response.body()
                    );

            JsonNode address =
                    root.get("address");

            if (
                    address == null
                    || address.isNull()
            ) {
                return null;
            }

            String city = firstNonBlank(
                    getText(address, "city"),
                    getText(address, "state"),
                    getText(address, "province")
            );

            String district = firstNonBlank(
                    getText(address, "borough"),
                    getText(address, "city_district"),
                    getText(address, "county")
            );

            String neighborhood = firstNonBlank(
                    getText(address, "suburb"),
                    getText(address, "neighbourhood"),
                    getText(address, "quarter"),
                    getText(address, "town"),
                    getText(address, "village")
            );

            return buildLocationName(
                    city,
                    district,
                    neighborhood
            );

        } catch (IOException exception) {
            return null;

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private void waitForRateLimit() {
        long elapsed =
                System.currentTimeMillis()
                        - lastRequestTime;

        if (elapsed >= MIN_REQUEST_INTERVAL) {
            return;
        }

        try {
            Thread.sleep(
                    MIN_REQUEST_INTERVAL - elapsed
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private String getText(
            JsonNode node,
            String fieldName
    ) {
        JsonNode value =
                node.get(fieldName);

        if (
                value == null
                || value.isNull()
                || value.asText().isBlank()
        ) {
            return null;
        }

        return value.asText();
    }

    private String firstNonBlank(
            String... values
    ) {
        for (String value : values) {
            if (
                    value != null
                    && !value.isBlank()
            ) {
                return value;
            }
        }

        return null;
    }

    private String buildLocationName(
            String city,
            String district,
            String neighborhood
    ) {
        StringBuilder location =
                new StringBuilder();

        appendLocation(location, city);
        appendLocation(location, district);
        appendLocation(location, neighborhood);

        return location.isEmpty()
                ? null
                : location.toString();
    }

    private void appendLocation(
            StringBuilder location,
            String value
    ) {
        if (
                value == null
                || value.isBlank()
        ) {
            return;
        }

        String current =
                location.toString();

        if (current.contains(value)) {
            return;
        }

        if (!location.isEmpty()) {
            location.append(" ");
        }

        location.append(value);
    }
}