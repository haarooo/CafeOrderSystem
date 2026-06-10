package com.example.cafeordersystem.review.analysis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReviewAnalysisClient {

    private final ChatClient chatClient;

    public ReviewAnalysisClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String analyze(String reviewContent) {
        if (reviewContent == null || reviewContent.isBlank()) {
            throw new IllegalArgumentException("분석할 리뷰 내용이 비어 있습니다.");
        }

        return chatClient.prompt()
                .system(createSystemPrompt())
                .user("""
                        아래 카페 리뷰를 분석해라.

                        리뷰 원문:
                        %s
                        """.formatted(reviewContent))
                .call()
                .content();
    }

    private String createSystemPrompt() {
        return """
                너는 한국어 카페 리뷰를 구조화된 라벨로 분석하는 시스템이다.
                반드시 JSON만 반환한다.
                마크다운, 설명 문장, 코드블록은 절대 포함하지 않는다.

                분석 목표:
                - 리뷰 전체 감정을 POSITIVE, NEGATIVE, NEUTRAL 중 하나로 분류한다.
                - 리뷰에서 실제로 언급된 운영 카테고리를 찾는다.
                - 각 카테고리별 감정을 POSITIVE, NEGATIVE, NEUTRAL 중 하나로 분류한다.
                - 리뷰 대응 위험도를 LOW, MEDIUM, HIGH 중 하나로 분류한다.
                - evidence는 반드시 리뷰 원문에 실제로 존재하는 짧은 표현만 사용한다.

                overallSentiment 규칙:
                - POSITIVE: 긍정 카테고리만 있고 부정 카테고리가 없음
                - NEGATIVE: 부정 카테고리가 하나라도 있음
                - NEUTRAL: 감정이 약하거나 단순 정보 전달이라 긍정/부정을 판단하기 어려움

                중요:
                - 긍정과 부정이 함께 있는 리뷰는 NEGATIVE로 분류한다.
                - MIXED 값은 사용하지 않는다.
                - 사장님 운영 관점에서는 부정 요소가 있으면 대응 대상이므로 NEGATIVE로 본다.

                사용할 수 있는 category 값:
                - TASTE
                - SERVICE
                - PRICE
                - ATMOSPHERE
                - CLEANLINESS
                - FACILITY
                - WAITING
                - SEAT
                - PARKING
                - REVISIT

                category sentiment 값:
                - POSITIVE
                - NEGATIVE
                - NEUTRAL

                riskLevel 규칙:
                - LOW: 일반 긍정/중립 리뷰 또는 매우 가벼운 불만
                - MEDIUM: 서비스, 대기, 가격 등에 명확한 불만이 있음
                - HIGH: 위생 문제, 환불 요구, 강한 분노, 재방문 거부, 악성 확산 가능성이 큼

                categories 규칙:
                - 리뷰에 실제로 언급된 카테고리만 포함한다.
                - 억지로 모든 카테고리를 넣지 않는다.
                - 하나의 리뷰에 여러 카테고리가 있으면 모두 포함한다.
                - evidence는 원문에서 판단 근거가 되는 짧은 문구를 그대로 가져온다.
                - 원문에 없는 내용을 지어내지 않는다.
                - "다신 안 감", "다시는 안 가요", "다신 가지 마세요", "재방문 안 함", "비추천", "가지 마세요"처럼 다시 방문하지 않겠다는 표현은 REVISIT 카테고리 NEGATIVE로 분류한다.
                - overallSentiment가 NEGATIVE인데 categories가 비어 있으면 안 된다.
                - 구체적인 원인이 없어도 재방문 거부/비추천 표현이 있으면 REVISIT를 반드시 포함한다.


                반드시 아래 JSON 구조 그대로 반환한다.

                {
                  "overallSentiment": "NEGATIVE",
                  "riskLevel": "MEDIUM",
                  "categories": [
                    {
                      "category": "TASTE",
                      "sentiment": "POSITIVE",
                      "evidence": "리뷰 원문 근거"
                    }
                  ]
                }
                """;
    }
}
