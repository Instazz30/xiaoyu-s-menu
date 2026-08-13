package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.dto.LoginResult;
import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.exception.BusinessException;
import com.xiaxiaoyu.xingbangmenu.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AuthService {

    private static final long TOKEN_TTL_MILLIS = 7L * 24 * 3600 * 1000;

    private final SysUserMapper userMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, TokenEntry> tokenStore = new ConcurrentHashMap<>();

    @Value("${wechat.appid:}")
    private String appid;

    @Value("${wechat.secret:}")
    private String secret;

    public AuthService(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /** 微信登录：code 换 openid；未配置 appid/secret 时走 mock 模式 */
    public LoginResult login(String code, String nickname, String avatarUrl) {
        String openid = resolveOpenid(code);
        return issueToken(openid, nickname, avatarUrl);
    }

    public SysUser getByToken(String token) {
        TokenEntry entry = tokenStore.get(token);
        if (entry == null) return null;
        if (System.currentTimeMillis() > entry.expiresAt) {
            tokenStore.remove(token);
            return null;
        }
        return userMapper.selectById(entry.userId);
    }

    public SysUser getProfile(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return user;
    }

    public SysUser updateProfile(Long userId, String nickname, String avatarUrl) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        if (nickname != null && !nickname.isBlank()) {
            user.setNickname(nickname.trim());
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        userMapper.update(user);
        return userMapper.selectById(userId);
    }

    private LoginResult issueToken(String openid, String nickname, String avatarUrl) {
        SysUser user = userMapper.selectByOpenid(openid);
        if (user == null) {
            user = new SysUser();
            user.setOpenid(openid);
            user.setNickname(nickname != null && !nickname.isBlank() ? nickname.trim() : "微信用户");
            user.setAvatarUrl(avatarUrl);
            userMapper.insert(user);
        } else if (nickname != null && !nickname.isBlank()
                && (user.getNickname() == null || user.getNickname().isBlank()
                    || "微信用户".equals(user.getNickname()))) {
            user.setNickname(nickname.trim());
            if (avatarUrl != null) user.setAvatarUrl(avatarUrl);
            userMapper.update(user);
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, new TokenEntry(user.getId(), System.currentTimeMillis() + TOKEN_TTL_MILLIS));
        return new LoginResult(token, user);
    }

    private String resolveOpenid(String code) {
        if (appid == null || appid.isBlank() || secret == null || secret.isBlank()) {
            // 开发模式：不调用微信接口，使用固定 mock 身份，避免每次启动变成新用户
            return "mock_dev_user";
        }
        try {
            String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid
                    + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Map<?, ?> body = objectMapper.readValue(response.body(), Map.class);
            Object openid = body.get("openid");
            if (openid == null) {
                throw new BusinessException(401, "微信登录失败: " + body.get("errmsg"));
            }
            return openid.toString();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(401, "微信登录失败: " + e.getMessage());
        }
    }

    private static class TokenEntry {
        final Long userId;
        final long expiresAt;

        TokenEntry(Long userId, long expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }
    }
}
