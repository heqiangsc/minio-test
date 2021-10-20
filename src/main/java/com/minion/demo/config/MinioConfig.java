package com.minion.demo.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * @description:
 * @author: heqiang
 * @create: 2021-10-20 16:59
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {
    @Bean
    @SneakyThrows
    public MinioClient minioClient(MinioProperties minioProperties) {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint(), minioProperties.getPort(), false)
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
        String defaultBucketName = minioProperties.getDefaultBucketName();

        if(Objects.nonNull(defaultBucketName)) {
            BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder()
                    .bucket(defaultBucketName)
                    .build();
            // 创建默认的bucket
            if(!minioClient.bucketExists(bucketExistsArgs)) {
                MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                        .bucket(defaultBucketName)
                        .build();
                minioClient.makeBucket(makeBucketArgs);
                log.info("create default bucket \"{}\" success", defaultBucketName);
            }
        }

        return minioClient;
    }
}
