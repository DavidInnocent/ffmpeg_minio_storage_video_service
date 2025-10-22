package com.example.ffmpeg_minio_storage.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioClientConfig {
    @Bean
    public MinioClient provideMinioClient() {
        //TODO remove the credentials to an external config or env
        return
                MinioClient.builder()
                        .endpoint("http://ffmpeg-minio:9000")
                        .credentials("minioadmin", "minioadmin")
                        .build();
    }
    
}
