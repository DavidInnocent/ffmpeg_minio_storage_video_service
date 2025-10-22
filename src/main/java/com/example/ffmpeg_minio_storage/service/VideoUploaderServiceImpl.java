package com.example.ffmpeg_minio_storage.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

@Service
public class VideoUploaderServiceImpl implements VideoUploaderService {
    @Autowired
    MinioClient minioClient;
    
    public static final Logger log = LoggerFactory.getLogger(VideoUploaderServiceImpl.class);
    
    
    @Override
    public String uploadVideo(MultipartFile videoFile) throws Exception {
        var newFileName  = UUID.randomUUID().toString();
        File tempVideo = File.createTempFile("video-", newFileName);
        videoFile.transferTo(tempVideo);
        
        File thumbnail = File.createTempFile("thumb-", ".jpg");
        
        try {
            generateThumbnailWithRetry(tempVideo, thumbnail);
        } catch (Exception e) {
            log.error("Thumbnail generation failed: {}", e.getMessage());
            throw new Exception("Thumbnail generation failed: " + e.getMessage());
        }
        
        
        if (!thumbnail.exists() || thumbnail.length() == 0) {
            throw new Exception("Thumbnail file is empty after generation to be handled later");
        }
        
        
        UUID randomLinker = UUID.randomUUID();
        
        String videoObjectKey =
                "videos/" + "user_identifier" + "/" + randomLinker + newFileName;
        String thumbnailObjectKey = "thumbnails/" + "user_identifier" + "/" + randomLinker + ".jpg";
        
        String bucketName = "data".toLowerCase();
        
        String videoUrl = "https://minio-subdomain.mywebsite.com/" + bucketName + "/" + videoObjectKey;
        String thumbnailUrl = "https://minio-subdomain.mywebsite.com/" + bucketName + "/" + thumbnailObjectKey;
        
        
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
        
        minioClient.uploadObject(UploadObjectArgs.builder()
                .bucket(bucketName)
                .object(videoObjectKey)
                .filename(tempVideo.getAbsolutePath())
                .contentType("video/mp4")
                .build());
        
        minioClient.uploadObject(UploadObjectArgs.builder()
                .bucket(bucketName)
                .object(thumbnailObjectKey)
                .filename(thumbnail.getAbsolutePath())
                .contentType("image/jpeg")
                .build());
        
        
        log.info("Video and thumbnail uploaded successfully to MinIO.{} {}", videoUrl, thumbnailUrl);
        return "DONE";
    }
    
    private void generateThumbnailWithRetry(File video, File thumbnail) throws IOException, InterruptedException {
        // First try capture at 1 second because sometimes the frame may not have dataa
        if (!tryGenerateThumbnail(video, thumbnail, "00:00:01")) {
            // Second try   capture at 0 seconds to retry capture thumbnail
            if (!tryGenerateThumbnail(video, thumbnail, "00:00:00")) {
                // Third try ffirst frame without seeking so that it continues
                tryGenerateFirstFrame(video, thumbnail);
            }
        }
    }
    
    private boolean tryGenerateThumbnail(File video, File thumbnail, String timestamp) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", video.getAbsolutePath(),
                "-ss", timestamp,
                "-vframes", "1",
                "-q:v", "2",
                thumbnail.getAbsolutePath()
        );
        return executeFfmpegCommand(processBuilder);
    }
    
    private boolean tryGenerateFirstFrame(File video, File thumbnail) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", video.getAbsolutePath(),
                "-vf", "select=eq(n\\,0)",
                "-q:v", "2",
                "-vframes", "1",
                thumbnail.getAbsolutePath()
        );
        return executeFfmpegCommand(processBuilder);
    }
    
    private boolean executeFfmpegCommand(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        // Capture FFmpeg output for debugging purposes
        StringBuilder output = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode == 0) {
            return true;
        } else {
            log.error("FFmpeg failed with code {}: {}", exitCode, output);
            return false;
        }
    }
}
