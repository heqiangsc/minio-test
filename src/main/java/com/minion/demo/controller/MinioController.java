package com.minion.demo.controller;


import com.minion.demo.service.MinioUtils;
import io.minio.ObjectWriteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @description:
 * @author: heqiang
 * @create: 2021-10-20 17:05
 **/
@RestController
@RequestMapping("minio")
@RequiredArgsConstructor
public class MinioController {

    private final MinioUtils minioUtils;

    @GetMapping
    public String getUrl(String fileName) {
        return minioUtils.getFileUrl(fileName);
    }

    @PostMapping
    public ObjectWriteResponse upload(MultipartFile file) {
        return minioUtils.upload(file);
    }

    @GetMapping("download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        minioUtils.download(fileName, response);
    }

    @GetMapping("download/{bucketName}/{fileName}")
    public void download(@PathVariable String bucketName, @PathVariable String fileName) {
        minioUtils.download(bucketName, fileName, "/Users/ludangxin/temp/" + fileName);
    }

    @DeleteMapping
    public void del(String fileName) {
        minioUtils.delFile(fileName);
    }

}
