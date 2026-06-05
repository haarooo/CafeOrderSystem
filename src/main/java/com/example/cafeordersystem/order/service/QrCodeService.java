package com.example.cafeordersystem.order.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class QrCodeService {

    private static final String SAVE_DIR = "uploads/qrcodes";
    private static final int QR_SIZE = 300;

    // reviewPageUrl을 QR 이미지로 생성하고, 이미지 상대 경로를 반환한다.
    public String createQrImage(Long orderId, String reviewPageUrl) {
        try {
            Files.createDirectories(Path.of(SAVE_DIR));

            String fileName = "order-" + orderId + ".png";
            Path filePath = Path.of(SAVE_DIR, fileName);

            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(reviewPageUrl, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);

            return "/qrcodes/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("QR 이미지 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
