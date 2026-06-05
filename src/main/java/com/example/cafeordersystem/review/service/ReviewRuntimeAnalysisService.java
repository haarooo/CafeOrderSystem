//package com.example.cafeordersystem.review.service;
//
//import com.example.cafeordersystem.review.dto.RuntimeReviewAnalysisList;
//import com.example.cafeordersystem.review.dto.ReviewRow;
//import lombok.RequiredArgsConstructor;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class ReviewRuntimeAnalysisService {
//
//    private final ChatClient chatClient;
//
//    public RuntimeReviewAnalysisList analyzeReviews(List<ReviewRow> reviews) {
//        if (reviews == null || reviews.isEmpty()) {
//            RuntimeReviewAnalysisList empty = new RuntimeReviewAnalysisList();
//            empty.setAnalyses(List.of());
//            return empty;
//        }
//
//        String reviewListText = reviews.stream()
//                .map(review -> """
//                        {
//                          "reviewId": %d,
//                          "orderId": %d,
//                          "reviewContent": "%s",
//                          "createdAt": "%s"
//                        }
//                        """.formatted(
//                        review.getReviewId(),
//                        review.getOrderId(),
//                        escape(review.getReviewContent()),
//                        review.getCreatedAt()
//                ))
//                .collect(Collectors.joining(",\n"));
//
//        String prompt = """
//                너는 카페 사장님을 돕는 리뷰 분석 도우미야.
//
//                아래 리뷰 목록을 각각 분석해.
//                반드시 JSON 형식으로만 응답해.
//
//                sentiment는 반드시 아래 중 하나로만 반환해.
//                - POSITIVE
//                - NEGATIVE
//                - MIXED
//                - NEUTRAL
//
//                응답 JSON 형식:
//                {
//                  "analyses": [
//                    {
//                      "reviewId": 1,
//                      "sentiment": "POSITIVE",
//                      "summary": "리뷰 요약",
//                      "positivePoints": "긍정 포인트",
//                      "negativePoints": "부정 포인트",
//                      "recommendedAction": "사장님 대응 추천"
//                    }
//                  ]
//                }
//
//                리뷰 목록:
//                [
//                %s
//                ]
//                """.formatted(reviewListText);
//
//        return chatClient.prompt()
//                .user(prompt)
//                .call()
//                .entity(RuntimeReviewAnalysisList.class);
//    }
//
//    private String escape(String text) {
//        if (text == null) {
//            return "";
//        }
//
//        return text
//                .replace("\\", "\\\\")
//                .replace("\"", "\\\"");
//    }
//}