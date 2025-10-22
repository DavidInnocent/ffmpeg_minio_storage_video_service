package com.example.ffmpeg_minio_storage.service;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface VideoUploaderService {
    String uploadVideo(MultipartFile videoFile) throws Exception;
}
