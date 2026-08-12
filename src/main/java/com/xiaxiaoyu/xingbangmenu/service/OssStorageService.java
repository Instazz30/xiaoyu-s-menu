package com.xiaxiaoyu.xingbangmenu.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.xiaxiaoyu.xingbangmenu.config.OssProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
public class OssStorageService {

    private final OSS ossClient;
    private final OssProperties properties;

    public OssStorageService(@Lazy OSS ossClient, OssProperties properties) {
        this.ossClient = ossClient;
        this.properties = properties;
    }

    public String upload(String objectKey, byte[] content, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(content.length);
        metadata.setContentType(contentType);
        ossClient.putObject(properties.getBucketName(), objectKey,
                new ByteArrayInputStream(content), metadata);
        return publicUrl(objectKey);
    }

    String publicUrl(String objectKey) {
        String configuredBaseUrl = properties.getPublicBaseUrl();
        String baseUrl;
        if (configuredBaseUrl != null && !configuredBaseUrl.isBlank()) {
            baseUrl = configuredBaseUrl;
        } else {
            String endpointHost = properties.getEndpoint()
                    .replaceFirst("^https?://", "")
                    .replaceAll("/+$", "");
            baseUrl = "https://" + properties.getBucketName() + "." + endpointHost;
        }
        return baseUrl.replaceAll("/+$", "") + "/" + objectKey;
    }
}
