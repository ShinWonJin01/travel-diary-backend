package com.shinwonjin.traveldiary.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class ReverseGeocodingService {

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org/reverse";

    private static final long MIN_REQUEST_INTERVAL = 1000L;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private long lastRequestTime = 0L;

    public synchronized String getLocationName(
            Double latitude,
            Double longitude
    ) {
        if (latitude == null || longitude == null) return null;

        waitForRateLimit();

        String url = NOMINATIM_URL
                + "?format=jsonv2"
                + "&lat=" + latitude
                + "&lon=" + longitude
                + "&accept-language=ko,en";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(
                        "User-Agent",
                        "travel-diary-backend/1.0"
                )
                .GET()
                .build();

        try {
            lastRequestTime = System.currentTimeMillis();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) return null;

            JsonNode root =
                    objectMapper.readTree(response.body());

            JsonNode address = root.get("address");

            if (address == null || address.isNull()) return null;

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

        if (elapsed >= MIN_REQUEST_INTERVAL) return;

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
        JsonNode value = node.get(fieldName);

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
            if (value != null && !value.isBlank()) {
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
        StringBuilder location = new StringBuilder();

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
        if (value == null || value.isBlank()) return;

        String current = location.toString();

        if (current.contains(value)) return;

        if (!location.isEmpty()) {
            location.append(" ");
        }

        location.append(value);
    }
}