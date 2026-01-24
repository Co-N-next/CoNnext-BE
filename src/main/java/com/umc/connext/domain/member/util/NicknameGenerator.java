package com.umc.connext.domain.member.util;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class NicknameGenerator {

    private static final List<String> ADJECTIVES = List.of(
            "행복한", "귀여운", "졸린", "용감한", "빠른", "조용한"
    );

    private static final List<String> NOUNS = List.of(
            "고양이", "강아지", "토끼", "판다", "여우", "곰"
    );

    private final Random random = new Random();

    public String generate() {
        String adj = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String noun = NOUNS.get(random.nextInt(NOUNS.size()));
        int number = random.nextInt(1000); // 0~999

        return adj + noun + number;
    }
}
