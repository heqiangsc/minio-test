package com.minio.demo.controller;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.util.Objects;

import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import io.minio.errors.MinioException;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description: d
 * @author: heqiang
 * @create: 2021-10-20 17:25
 **/
@Slf4j
public class MinioControllerTest {

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeyException {
        try {
            // 使用MinIO服务的URL，端口，Access key和Secret key创建一个MinioClient对象
            MinioClient minioClient = MinioClient.builder()
                    .endpoint("http://minio.hesc.site", 80, false)
                    .credentials("admin", "admin123")
                    .build();
            String defaultBucketName = "ldx";

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

            upload(minioClient, defaultBucketName, new File("/Users/heqiang/Desktop/pic/11_eee.png"));
            log.info("/Users/ludangxin/temp/舞狮.png is successfully uploaded as lion.jpg to `test` bucket.");


        } catch(MinioException e) {
            log.info("Error occurred: " + e);
        }
    }

    public static ObjectWriteResponse upload(MinioClient minioClient, String bucketName, File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            final String fileName = file.getName();
            String contentType = "file/stream";
            PutObjectArgs objectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(is, is.available(), -1)
                    .contentType(contentType)
                    .build();
            return minioClient.putObject(objectArgs);
        }
        catch(Exception e) {
            log.error(e.getMessage());
        }
        finally {
            try {
                if(Objects.nonNull(is)) {
                    is.close();
                }
            } catch(IOException e) {
                log.error(e.getMessage());
            }
        }
        return null;
    }
}
