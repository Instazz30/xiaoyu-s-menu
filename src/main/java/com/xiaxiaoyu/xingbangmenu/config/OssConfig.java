package com.xiaxiaoyu.xingbangmenu.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.comm.SignVersion;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@EnableConfigurationProperties(OssProperties.class)
public class OssConfig {

    @Bean(destroyMethod = "shutdown")
    @Lazy
    public OSS ossClient(OssProperties properties) throws Exception {
        requireConfigured("OSS_ENDPOINT", properties.getEndpoint());
        requireConfigured("OSS_REGION", properties.getRegion());
        requireConfigured("OSS_BUCKET_NAME", properties.getBucketName());

        ClientBuilderConfiguration configuration = new ClientBuilderConfiguration();
        configuration.setSignatureVersion(SignVersion.V4);

        return OSSClientBuilder.create()
                .endpoint(properties.getEndpoint())
                .credentialsProvider(credentialsProvider(properties))
                .clientConfiguration(configuration)
                .region(properties.getRegion())
                .build();
    }

    private CredentialsProvider credentialsProvider(OssProperties properties) throws Exception {
        String accessKeyId = properties.getAccessKeyId();
        String accessKeySecret = properties.getAccessKeySecret();
        boolean hasAccessKeyId = accessKeyId != null && !accessKeyId.isBlank();
        boolean hasAccessKeySecret = accessKeySecret != null && !accessKeySecret.isBlank();

        if (hasAccessKeyId != hasAccessKeySecret) {
            throw new IllegalStateException("OSS AccessKey ID 和 Secret 必须同时配置");
        }
        if (hasAccessKeyId) {
            return CredentialsProviderFactory.newDefaultCredentialProvider(
                    accessKeyId.trim(), accessKeySecret.trim());
        }
        return CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
    }

    private void requireConfigured(String environmentName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("缺少阿里云 OSS 配置: " + environmentName);
        }
    }
}
