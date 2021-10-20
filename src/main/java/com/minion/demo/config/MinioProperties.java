package com.minion.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @description:
 * @author: heqiang
 * @create: 2021-10-20 16:57
 **/
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    /**
     * 端点
     */
    private String endpoint;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 默认的桶名称
     */
    private String defaultBucketName;

    /**
     * 访问key
     */
    private String accessKey;

    /**
     * 密钥
     */
    private String secretKey;


}
