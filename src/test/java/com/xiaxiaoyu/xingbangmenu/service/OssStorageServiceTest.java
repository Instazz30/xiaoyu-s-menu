package com.xiaxiaoyu.xingbangmenu.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.xiaxiaoyu.xingbangmenu.config.OssProperties;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OssStorageServiceTest {

    @Test
    void uploadsObjectAndReturnsPublicUrl() {
        OSS client = mock(OSS.class);
        OssProperties properties = new OssProperties();
        properties.setEndpoint("https://oss-cn-hangzhou.aliyuncs.com");
        properties.setRegion("cn-hangzhou");
        properties.setBucketName("test-bucket");
        OssStorageService service = new OssStorageService(client, properties);

        String url = service.upload("recipes/1/images/test.jpg", new byte[]{1, 2, 3}, "image/jpeg");

        assertEquals("https://test-bucket.oss-cn-hangzhou.aliyuncs.com/recipes/1/images/test.jpg", url);
        verify(client).putObject(eq("test-bucket"), eq("recipes/1/images/test.jpg"),
                any(InputStream.class), any(ObjectMetadata.class));
    }

    @Test
    void usesConfiguredPublicBaseUrl() {
        OssProperties properties = new OssProperties();
        properties.setEndpoint("https://oss-cn-hangzhou.aliyuncs.com");
        properties.setBucketName("test-bucket");
        properties.setPublicBaseUrl("https://cdn.example.com/menu/");
        OssStorageService service = new OssStorageService(mock(OSS.class), properties);

        assertEquals("https://cdn.example.com/menu/recipes/1/poster.jpg",
                service.publicUrl("recipes/1/poster.jpg"));
    }
}
