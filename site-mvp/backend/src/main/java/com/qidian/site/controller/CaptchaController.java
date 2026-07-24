package com.qidian.site.controller;

import com.qidian.site.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CaptchaController {

    private static final String CAPTCHA_SESSION_KEY = "captcha_code";
    private static final String CAPTCHA_TIMESTAMP_KEY = "captcha_ts";
    private static final long CAPTCHA_TTL_MS = 5 * 60 * 1000;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    @GetMapping("/captcha")
    public ApiResponse<Map<String, String>> generateCaptcha(HttpServletRequest request) {
        String code = generateCode(4);
        BufferedImage image = createCaptchaImage(code);
        String base64 = imageToBase64(image);

        HttpSession session = request.getSession(true);
        session.setAttribute(CAPTCHA_SESSION_KEY, code.toLowerCase());
        session.setAttribute(CAPTCHA_TIMESTAMP_KEY, System.currentTimeMillis());

        return ApiResponse.ok(Map.of(
            "image", "data:image/png;base64," + base64,
            "sessionId", session.getId()
        ));
    }

    @PostMapping("/captcha/verify")
    public ApiResponse<Map<String, Boolean>> verifyCaptcha(
        @RequestBody Map<String, String> body,
        HttpServletRequest request
    ) {
        String input = body.get("code");
        if (input == null || input.isBlank()) {
            return ApiResponse.ok(Map.of("valid", false));
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return ApiResponse.ok(Map.of("valid", false));
        }

        String stored = (String) session.getAttribute(CAPTCHA_SESSION_KEY);
        Long ts = (Long) session.getAttribute(CAPTCHA_TIMESTAMP_KEY);

        if (stored == null || ts == null || System.currentTimeMillis() - ts > CAPTCHA_TTL_MS) {
            session.invalidate();
            return ApiResponse.ok(Map.of("valid", false));
        }

        boolean valid = stored.equalsIgnoreCase(input.trim());
        if (valid) {
            session.removeAttribute(CAPTCHA_SESSION_KEY);
            session.removeAttribute(CAPTCHA_TIMESTAMP_KEY);
        }
        return ApiResponse.ok(Map.of("valid", valid));
    }

    static boolean verifyFromSession(HttpServletRequest request, String input) {
        if (input == null || input.isBlank()) return false;
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        String stored = (String) session.getAttribute(CAPTCHA_SESSION_KEY);
        Long ts = (Long) session.getAttribute(CAPTCHA_TIMESTAMP_KEY);
        if (stored == null || ts == null || System.currentTimeMillis() - ts > CAPTCHA_TTL_MS) {
            session.invalidate();
            return false;
        }
        boolean valid = stored.equalsIgnoreCase(input.trim());
        if (valid) {
            session.removeAttribute(CAPTCHA_SESSION_KEY);
            session.removeAttribute(CAPTCHA_TIMESTAMP_KEY);
        }
        return valid;
    }

    private String generateCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private BufferedImage createCaptchaImage(String code) {
        int width = 120, height = 48;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(245, 245, 245));
        g.fillRect(0, 0, width, height);

        Font font = new Font("SansSerif", Font.BOLD, 28);
        g.setFont(font);

        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(
                30 + RANDOM.nextInt(80),
                30 + RANDOM.nextInt(80),
                100 + RANDOM.nextInt(100)
            ));
            double angle = (RANDOM.nextDouble() - 0.5) * 0.4;
            int x = 12 + i * 26;
            int y = 32 + (int) ((RANDOM.nextDouble() - 0.5) * 8);
            g.rotate(angle, x, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-angle, x, y);
        }

        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(
                150 + RANDOM.nextInt(100),
                150 + RANDOM.nextInt(100),
                150 + RANDOM.nextInt(100)
            ));
            g.drawLine(RANDOM.nextInt(width), RANDOM.nextInt(height),
                       RANDOM.nextInt(width), RANDOM.nextInt(height));
        }

        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(
                180 + RANDOM.nextInt(75),
                180 + RANDOM.nextInt(75),
                180 + RANDOM.nextInt(75)
            ));
            g.fillRect(RANDOM.nextInt(width), RANDOM.nextInt(height), 1, 1);
        }

        g.dispose();
        return image;
    }

    private String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode captcha image", e);
        }
    }
}
