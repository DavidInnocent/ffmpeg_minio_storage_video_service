package com.example.ffmpeg_minio_storage.controller;

import com.example.ffmpeg_minio_storage.service.VideoUploaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping(VideoUploaderController.VIDEO_UPLOADER)
public class VideoUploaderController {
    
    public static final String VIDEO_UPLOADER = "/video-uploader";
    
    @Autowired
    VideoUploaderService videoUploaderService;
    
    /**
     * The following will create a thumbnail from video uploaded and save the video into minio
     *
     * @param video the video to be uploaded
     * @return just something random
     * @throws Exception handle this from your controller advice
     */
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadVideo(
            @RequestParam("video") MultipartFile video) throws Exception {
        return ResponseEntity.ok(videoUploaderService.uploadVideo(video));
    }
}
