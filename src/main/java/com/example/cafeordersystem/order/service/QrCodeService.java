package com.example.cafeordersystem.order.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * QR 코드를 파일로 저장하지 않고,
 * 메모리에서 PNG로 생성한 뒤 Base64 data URL로 반환한다.
 *
 * 장점:
 * - S3 필요 없음
 * - 로컬 파일 저장 필요 없음
 * - 배포 서버 파일시스템 의존 없음
 * - 프론트 <img src="data:image/png;base64,..."> 로 바로 표시 가능
 */
@Service
public class QrCodeService {

    private static final int QR_SIZE = 300;

    public String createQrImage(Long orderId, String reviewPageUrl) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(reviewPageUrl, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    bitMatrix,
                    "PNG",
                    outputStream
            );

            String base64 = Base64.getEncoder()
                    .encodeToString(outputStream.toByteArray());

            return "data:image/png;base64," + base64;

        } catch (Exception e) {
            throw new RuntimeException("QR 이미지 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }
}