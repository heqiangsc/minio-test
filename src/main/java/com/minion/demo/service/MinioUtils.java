package com.minion.demo.service;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: heqiang
 * @create: 2021-10-20 17:01
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtils {
    @Value("${minio.default-bucket-name}")
    private String defaultBucketName;

    private final MinioClient minioClient;

    /**
     * 获取全部bucket
     *
     * @return all bucket
     */
    @SneakyThrows
    public List<Bucket> getAllBuckets() {
        return minioClient.listBuckets();
    }

    /**
     * 判断 bucket是否存在
     *
     * @param bucketName 桶名称
     * @return true 存在
     */
    @SneakyThrows
    public boolean bucketExists(String bucketName){
        BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder()
                .bucket(bucketName)
                .build();
        return minioClient.bucketExists(bucketExistsArgs);
    }

    /**
     * 创建 bucket
     *
     * @param bucketName 桶名称
     */
    @SneakyThrows
    public void createBucket(String bucketName){
        boolean isExist = this.bucketExists(bucketName);
        if(!isExist) {
            MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build();
            minioClient.makeBucket(makeBucketArgs);
        }
    }

    /**
     * 文件上传
     *
     * @param bucketName 桶名称
     * @param fileName 上传后的文件名称
     * @param fileAbsolutePath 文件的绝对路径
     * @return 文件url
     */
    @SneakyThrows
    public ObjectWriteResponse upload(String bucketName, String fileName, String fileAbsolutePath){
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket(bucketName)
                .filename(fileAbsolutePath)
                .object(fileName)
                .build();
        return minioClient.uploadObject(uploadObjectArgs);
    }

    /**
     * 文件上传
     *
     * @param fileName 上传后的文件名称
     * @param stream 文件输入流
     * @return 文件url
     */
    @SneakyThrows
    public String upload(String fileName, InputStream stream){
        this.upload(defaultBucketName, fileName, stream);
        return getFileUrl(defaultBucketName, fileName);
    }

    /**
     * 文件上传
     *
     * @param bucketName 桶名称
     * @param fileName 上传后的文件名称
     * @param stream 文件输入流
     * @return 文件url
     */
    @SneakyThrows
    public ObjectWriteResponse upload(String bucketName, String fileName, InputStream stream){
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(stream, stream.available(), -1)
                    .build();
            return minioClient.putObject(objectArgs);
        }
        catch(Exception e) {
            log.error(e.getMessage());
        }
        finally {
            try {
                if(Objects.nonNull(stream)) {
                    stream.close();
                }
            } catch(IOException e) {
                log.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * 文件上传
     *
     * @param file 文件
     * @return 文件url
     */
    public ObjectWriteResponse upload(MultipartFile file) {
        return this.upload(defaultBucketName, file);
    }

    /**
     * 文件上传
     *
     * @param bucketName 桶名称
     * @param file 文件
     * @return 文件url
     */
    public ObjectWriteResponse upload(String bucketName, MultipartFile file) {
        InputStream is = null;
        try {
            is = file.getInputStream();
            final String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
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

    /**
     * 附件下载
     *
     * @param fileName 附件名称
     */
    @SneakyThrows
    public void download(String fileName, HttpServletResponse response) {
        this.download(defaultBucketName, fileName, response);
    }

    /**
     * 附件下载
     *
     * @param bucketName 桶名称
     * @param fileName 附件名称
     */
    @SneakyThrows
    public void download(String bucketName, String fileName, HttpServletResponse response) {
        GetObjectArgs build = GetObjectArgs.builder().bucket(bucketName).object(fileName).build();
        OutputStream out = null;
        try(GetObjectResponse object = minioClient.getObject(build)) {
            int len = 0;
            byte[] buffer = new byte[1024];
            out = response.getOutputStream();
            response.reset();
            String fileName1 = new String(fileName.getBytes(), StandardCharsets.ISO_8859_1);
            response.addHeader("Content-Disposition", " attachment;filename=" + fileName1);
            response.setContentType("application/octet-stream");

            while((len = object.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch(Exception e) {
            log.error(e.getMessage());
        } finally {
            if(out != null) {
                try {
                    out.close();
                } catch(IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

    /**
     * 附件下载
     *
     * @param fileName 附件名称
     */
    @SneakyThrows
    public void download(String bucketName, String fileName, String fileAbsolutePath) {
        DownloadObjectArgs downloadObjectArgs = DownloadObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .filename(fileAbsolutePath)
                .build();
        minioClient.downloadObject(downloadObjectArgs);
    }

    /**
     * 删除文件
     *
     * @param fileName 文件名称
     */
    @SneakyThrows
    public void delFile(String fileName){
        this.delFile(defaultBucketName, fileName);
    }

    /**
     * 删除文件
     *
     * @param bucketName 桶名称
     * @param fileName 文件名称
     */
    @SneakyThrows
    public void delFile(String bucketName, String fileName){
        RemoveObjectArgs removeObjectsArgs = RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build();
        minioClient.removeObject(removeObjectsArgs);
    }

    /**
     * 获取minio文件的下载地址
     *
     * @param fileName 文件名
     */
    @SneakyThrows
    public String getFileUrl(String fileName) {
        return this.getFileUrl(defaultBucketName, fileName);
    }

    /**
     * 获取minio文件的下载地址
     *
     * @param bucketName 桶名称
     * @param fileName 文件名
     */
    @SneakyThrows
    public String getFileUrl(String bucketName, String fileName) {
        GetPresignedObjectUrlArgs objectUrlArgs = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(fileName)
                .build();
        return minioClient.getPresignedObjectUrl(objectUrlArgs);
    }

    /**
     * 获取minio文件的下载地址
     *
     * @param fileName 文件名
     */
    @SneakyThrows
    public String getFileUrl(String fileName, Integer duration, TimeUnit unit) {
        return this.getFileUrl(defaultBucketName, fileName, duration, unit);
    }

    /**
     * 获取minio文件的下载地址
     *
     * @param bucketName 桶名称
     * @param fileName 文件名
     */
    @SneakyThrows
    public String getFileUrl(String bucketName, String fileName, Integer duration, TimeUnit unit) {
        GetPresignedObjectUrlArgs objectUrlArgs = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucketName)
                .object(fileName)
                .expiry(duration, unit)
                .build();
        return minioClient.getPresignedObjectUrl(objectUrlArgs);
    }

    /**
     * 设置桶策略
     *
     * @param bucketName 桶名称
     * @param policy 策略
     */
    @SneakyThrows
    public void setBucketPolicy(String bucketName, String policy) {
        SetBucketPolicyArgs bucketPolicyArgs = SetBucketPolicyArgs.builder()
                .bucket(bucketName)
                .config(policy)
                .build();
        minioClient.setBucketPolicy(bucketPolicyArgs);
    }
}
