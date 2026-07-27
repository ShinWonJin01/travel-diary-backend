package com.shinwonjin.traveldiary.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of(
            "message", "Spring Boot 서버가 정상적으로 실행되었습니다."
        );
    }
}