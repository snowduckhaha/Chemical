package com.qidian.site.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminUploadService {
    private static final long MAX_INPUT_BYTES = 3L * 1024 * 1024;
    private static final long MAX_OUTPUT_BYTES = 1024L * 1024;
    private static final int MAX_EDGE = 8000;
    private static final long MAX_PIXELS = 40_000_000L;
    private final Path uploadRoot = Path.of("uploads").toAbsolutePath().normalize();

    public Map<String, Object> storeImage(MultipartFile file, String scene) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("image file is required");
        if (file.getSize() > MAX_INPUT_BYTES) throw new IllegalArgumentException("image cannot exceed 3 MB");
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
            throw new IllegalArgumentException("only JPG and PNG images are currently supported");
        }

        try {
            BufferedImage source = ImageIO.read(file.getInputStream());
            if (source == null) throw new IllegalArgumentException("invalid image file");
            if (Math.max(source.getWidth(), source.getHeight()) > MAX_EDGE || (long) source.getWidth() * source.getHeight() > MAX_PIXELS) {
                throw new IllegalArgumentException("image exceeds pixel safety limit");
            }

            BufferedImage canvas = createContainCanvas(source, scene);
            byte[] rendered = writeJpegUnderLimit(canvas);
            Files.createDirectories(uploadRoot);
            String name = UUID.randomUUID() + ".jpg";
            Files.write(uploadRoot.resolve(name), rendered);
            String url = "/uploads/" + name;
            return Map.of(
                "url", url,
                "sourceBytes", file.getSize(),
                "displayBytes", rendered.length,
                "width", source.getWidth(),
                "height", source.getHeight(),
                "displayMode", isCertificate(scene) ? "ORIGINAL" : "CONTAIN",
                "canvasRatio", canvasRatio(scene),
                "canvasBackground", "#FFFFFF"
            );
        } catch (IOException exception) {
            throw new IllegalArgumentException("unable to process image", exception);
        }
    }

    private BufferedImage createContainCanvas(BufferedImage source, String scene) {
        if (isCertificate(scene)) return source;
        int width = 1200;
        int height = isSeoOg(scene) ? 630 : isNewsCover(scene) ? 675 : 900;
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = canvas.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double scale = Math.min((double) width / source.getWidth(), (double) height / source.getHeight());
        int scaledWidth = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int scaledHeight = Math.max(1, (int) Math.round(source.getHeight() * scale));
        graphics.drawImage(source, (width - scaledWidth) / 2, (height - scaledHeight) / 2, scaledWidth, scaledHeight, null);
        graphics.dispose();
        return canvas;
    }

    private byte[] writeJpegUnderLimit(BufferedImage image) throws IOException {
        BufferedImage candidate = image;
        for (int step = 0; step < 8; step++) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(candidate, "jpg", output);
            if (output.size() <= MAX_OUTPUT_BYTES) return output.toByteArray();
            int width = Math.max(1, (int) (candidate.getWidth() * .85));
            int height = Math.max(1, (int) (candidate.getHeight() * .85));
            BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = scaled.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawImage(candidate, 0, 0, width, height, null);
            graphics.dispose();
            candidate = scaled;
        }
        throw new IllegalArgumentException("image could not be compressed below 1 MB");
    }

    private boolean isCertificate(String scene) {
        return "CERTIFICATE".equalsIgnoreCase(scene);
    }

    private boolean isSeoOg(String scene) {
        return "SEO_OG".equalsIgnoreCase(scene);
    }

    private boolean isNewsCover(String scene) {
        return "NEWS_COVER".equalsIgnoreCase(scene);
    }

    private String canvasRatio(String scene) {
        if (isCertificate(scene)) return "ORIGINAL";
        if (isSeoOg(scene)) return "1.91:1";
        return isNewsCover(scene) ? "16:9" : "4:3";
    }
}