package com.example.cafeordersystem.review.service;

import com.example.cafeordersystem.review.dto.RuntimeReviewAnalysisList;
import com.example.cafeordersystem.review.dto.ReviewRow;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewRuntimeAnalysisService {

    /**
     * ChatClient를 직접 주입받으면 Bean이 없어서 실행 오류가 날 수 있다.
     * 그래서 Spring AI가 자동 제공하는 ChatClient.Builder를 주입받고,
     * 호출 시점에 build()해서 사용한다.
     */
    private final ChatClient.Builder chatClientBuilder;

    public RuntimeReviewAnalysisList analyzeReviews(List<ReviewRow> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return RuntimeReviewAnalysisList.builder()
                    .analyses(List.of())
                    .build();
        }

        String reviewListText = reviews.stream()
                .map(review -> """
                        {
                          "reviewId": %d,
                          "reviewContent": "%s"
                        }
                        """.formatted(
                        review.getReviewId(),
                        escape(review.getReviewContent())
                ))
                .collect(Collectors.joining(",\n"));

        String prompt = """
                너는 카페 사장님을 돕는 리뷰 분석 도우미다.

                아래 리뷰 목록을 각각 분석해라.
                반드시 JSON 형식으로만 응답해라.
                JSON 외의 설명 문장, 마크다운, 코드블록은 절대 포함하지 마라.

                sentiment는 반드시 아래 중 하나로만 반환해라.
                - POSITIVE: 긍정 리뷰
                - NEGATIVE: 부정 리뷰
                - MIXED: 긍정과 부정이 함께 있는 리뷰
                - NEUTRAL: 감정 판단이 어렵거나 단순 정보성 리뷰

                각 리뷰마다 아래 값만 반환해라.

                1. reviewId
                2. sentiment
                3. summary
                4. operationInsight

                summary는 리뷰 내용을 한 문장으로 요약해라.

                operationInsight는 단순 답글 문구가 아니라,
                사장님이 운영적으로 참고할 수 있는 개선 액션이나 대응 방향으로 작성해라.

                응답 JSON 형식:
                {
                  "analyses": [
                    {
                      "reviewId": 1,
                      "sentiment": "MIXED",
                      "summary": "커피 맛은 좋지만 대기시간에 불만이 있는 리뷰입니다.",
                      "operationInsight": "대기시간 불만이 있으므로 피크타임 제조 동선이나 인력 배치를 점검하는 것이 좋습니다."
                    }
                  ]
                }

                리뷰 목록:
                [
                %s
                ]
                """.formatted(reviewListText);

        ChatClient chatClient = chatClientBuilder.build();

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(RuntimeReviewAnalysisList.class);
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}