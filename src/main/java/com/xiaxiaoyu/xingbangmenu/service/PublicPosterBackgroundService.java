package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import jakarta.annotation.PostConstruct;

@Service
public class PublicPosterBackgroundService {

    private static final Set<String> EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");
    private static final int THUMBNAIL_WIDTH = 180;
    private static final int THUMBNAIL_HEIGHT = 280;

    @Value("${upload.storage-path:./uploads}")
    private String storagePath;

    @PostConstruct
    public void warmUpThumbnails() {
        Path directory = backgroundDirectory();
        if (!Files.isDirectory(directory)) return;
        try (var files = Files.list(directory)) {
            files.filter(Files::isRegularFile)
                    .filter(this::isSupported)
                    .map(path -> stripExtension(path.getFileName().toString()))
                    .forEach(this::resolveThumbnailUrl);
        } catch (IOException ignored) {
            // 缩略图预热失败时，接口读取会自动重试或回退原图。
        }
    }

    public List<Map<String, String>> list() {
        Path directory = backgroundDirectory();
        if (!Files.isDirectory(directory)) return List.of();
        try (var files = Files.list(directory)) {
            return files.filter(Files::isRegularFile)
                    .filter(this::isSupported)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(path -> {
                        String filename = path.getFileName().toString();
                        String id = stripExtension(filename);
                        return Map.of(
                                "id", id,
                                "name", displayName(id),
                                "url", "/uploads/backgrounds/" + filename,
                                "thumbnailUrl", resolveThumbnailUrl(id)
                        );
                    })
                    .toList();
        } catch (IOException e) {
            throw new BusinessException(500, "公共背景读取失败");
        }
    }

    public boolean exists(String id) {
        return findById(id) != null;
    }

    public String resolvePath(String id) {
        Path path = findById(id);
        if (path == null) throw new BusinessException(400, "所选公共背景不存在");
        return path.toAbsolutePath().normalize().toString();
    }

    public String resolveUrl(String id) {
        Path path = findById(id);
        if (path == null) return null;
        return "/uploads/backgrounds/" + path.getFileName();
    }

    public synchronized String resolveThumbnailUrl(String id) {
        Path source = findById(id);
        if (source == null) return null;
        Path thumbnail = thumbnailDirectory().resolve(id + ".jpg");
        try {
            if (!Files.isRegularFile(thumbnail) ||
                    Files.getLastModifiedTime(thumbnail).compareTo(Files.getLastModifiedTime(source)) < 0) {
                createThumbnail(source, thumbnail);
            }
            return "/uploads/backgrounds/thumbnails/" + thumbnail.getFileName();
        } catch (IOException e) {
            return resolveUrl(id);
        }
    }

    private void createThumbnail(Path source, Path target) throws IOException {
        BufferedImage image = ImageIO.read(source.toFile());
        if (image == null) throw new IOException("无法读取背景图片");
        Files.createDirectories(target.getParent());
        BufferedImage thumbnail = new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumbnail.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, null);
        g.dispose();
        ImageIO.write(thumbnail, "jpg", target.toFile());
    }

    private Path findById(String id) {
        if (id == null || id.isBlank() || id.contains("..") || id.contains("/") || id.contains("\\")) {
            return null;
        }
        Path directory = backgroundDirectory();
        for (String extension : EXTENSIONS) {
            Path candidate = directory.resolve(id + "." + extension).normalize();
            if (candidate.startsWith(directory) && Files.isRegularFile(candidate)) return candidate;
        }
        return null;
    }

    private Path backgroundDirectory() {
        return Paths.get(storagePath, "backgrounds").toAbsolutePath().normalize();
    }

    private Path thumbnailDirectory() {
        return backgroundDirectory().resolve("thumbnails");
    }

    private boolean isSupported(Path path) {
        String filename = path.getFileName().toString();
        int dot = filename.lastIndexOf('.');
        return dot > 0 && EXTENSIONS.contains(filename.substring(dot + 1).toLowerCase(Locale.ROOT));
    }

    private String stripExtension(String filename) {
        return filename.substring(0, filename.lastIndexOf('.'));
    }

    private String displayName(String id) {
        if ("menu_bg".equals(id)) return "经典浅色背景";
        return id.replace('_', ' ').replace('-', ' ');
    }
}
