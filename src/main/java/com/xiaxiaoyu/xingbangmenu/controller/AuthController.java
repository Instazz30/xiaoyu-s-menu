package com.xiaxiaoyu.xingbangmenu.controller;

import com.xiaxiaoyu.xingbangmenu.common.Result;
import com.xiaxiaoyu.xingbangmenu.dto.LoginResult;
import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.exception.BusinessException;
import com.xiaxiaoyu.xingbangmenu.service.AuthService;
import com.xiaxiaoyu.xingbangmenu.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final PermissionService permissionService;

    @Value("${upload.storage-path:./uploads}")
    private String storagePath;

    public AuthController(AuthService authService, PermissionService permissionService) {
        this.authService = authService;
        this.permissionService = permissionService;
    }

    /** 微信登录（开发模式自动降级为 mock） */
    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody(required = false) Map<String, String> body) {
        String code = body != null ? body.get("code") : null;
        String nickname = body != null ? body.get("nickname") : null;
        String avatarUrl = body != null ? body.get("avatarUrl") : null;
        return Result.ok(authService.login(code, nickname, avatarUrl));
    }

    /** 当前用户信息 */
    @GetMapping("/profile")
    public Result<SysUser> profile(HttpServletRequest request) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(authService.getProfile(user.getId()));
    }

    /** 更新昵称/头像 */
    @PutMapping("/profile")
    public Result<SysUser> updateProfile(HttpServletRequest request,
                                         @RequestBody Map<String, String> body) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(authService.updateProfile(user.getId(), body.get("nickname"), body.get("avatarUrl")));
    }

    /**
     * 上传头像（微信「头像昵称填写能力」chooseAvatar 返回临时文件，压缩到 200x200 后落盘）。
     * 返回相对 URL（如 /uploads/avatars/xxx.jpg），前端展示时拼接 STATIC_URL。
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(HttpServletRequest request,
                                                    @RequestParam("file") MultipartFile file) throws IOException {
        // 登录校验（AuthInterceptor 已拦截 /api/**，此处仅取当前用户）
        permissionService.currentUser(request);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件为空");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException(400, "头像图片不能超过 2MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(400, "仅支持图片文件");
        }

        BufferedImage image;
        try {
            image = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException(400, "图片解析失败");
        }
        if (image == null) {
            throw new BusinessException(400, "图片解析失败");
        }

        Path avatarDir = Paths.get(storagePath, "avatars");
        Files.createDirectories(avatarDir);
        String filename = UUID.randomUUID().toString().replace("-", "") + ".jpg";
        Path target = avatarDir.resolve(filename);
        Thumbnails.of(image)
                .size(200, 200)
                .outputFormat("jpg")
                .toFile(target.toFile());

        return Result.ok(Map.of("url", "/uploads/avatars/" + filename));
    }
}
