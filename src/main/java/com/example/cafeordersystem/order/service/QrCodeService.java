package com.example.cafeordersystem.order.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class QrCodeService {

    @Value("${app.qr.save-dir}")
    private String saveDir;

    @Value("${app.qr.public-prefix}")
    private String publicPrefix;

    private static final int QR_SIZE = 300;

    public String createQrImage(Long orderId, String reviewPageUrl) {
        try {
            Files.createDirectories(Path.of(saveDir));

            String fileName = "order-" + orderId + ".png";
            Path filePath = Path.of(saveDir, fileName);

            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(reviewPageUrl, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);

            return publicPrefix + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("QR 이미지 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }
}