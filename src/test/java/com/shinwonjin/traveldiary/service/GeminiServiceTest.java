package com.shinwonjin.traveldiary.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GeminiServiceTest {

    @Autowired
    private GeminiService geminiService;

    @Test
    void generateTest() {
        String response = geminiService.generate(
                "안녕하세요. 한 문장으로 인사해 주세요."
        );

        System.out.println("Gemini 응답:");
        System.out.println(response);
    }
}