# 🎥 FFmpeg–MinIO Video Uploader

This project is a **Spring Boot API** that allows users to upload videos via a `multipart/form-data` request.  
Once uploaded, the service automatically:

1. Generates a **thumbnail** using `FFmpeg`.
2. Uploads both the video and the thumbnail to a **MinIO** object storage (S3-compatible).
3. Returns **public URLs** that can be accessed by external applications.

All dependencies—Spring Boot app, FFmpeg service, and MinIO—run seamlessly together using **Docker Compose**.

---

## 🧱 Project Architecture

```
+--------------------+          +-------------------+
|  Postman / Client  |  --->    |  Spring Boot App  |
|  (multipart file)  |          | (VideoUploaderAPI)|
+--------------------+          +--------+----------+
                                         |
                                         | Uploads
                                         v
                                 +---------------+
                                 |    MinIO      |
                                 | Object Storage|
                                 +---------------+

(FFmpeg is installed inside the app container for thumbnail generation)
```

---

## 🚀 Quick Start

### 1️⃣ Prerequisites

Make sure you have:
- **Docker** and **Docker Compose** installed
- **Postman** or any API testing tool

---

### 2️⃣ Build & Run the Containers

```bash
docker compose up --build
```

This will start the following services:

| Service | Description | Port |
|----------|--------------|------|
| `ffmpeg-minio-backend` | Spring Boot API | `8080` |
| `ffmpeg-minio` | MinIO storage | `9000` (API), `9001` (Console) |
| `ffmpeg-minio-init` | Initializes MinIO bucket | — |
| `ffmpeg` | FFmpeg service (optional for debugging) | — |

---

### 3️⃣ Access MinIO Console

Open your browser and go to:

```
http://localhost:9001
```

Login credentials:
- **Username:** `minioadmin`
- **Password:** `minioadmin`

A bucket named `data` will be created automatically.

---

## 🧩 API Usage

### 📤 Upload a Video

**Endpoint:**
```
POST http://localhost:8080/video-uploader
```

**Body (form-data):**

| Key | Type | Description |
|-----|------|--------------|
| `videoFile` | File | The video file to upload |

**Example in Postman:**
1. Set request type to `POST`
2. Set body type to `form-data`
3. Add a key `videoFile` of type “File”
4. Choose a `.mp4` file
5. Send the request

---

### ✅ API Workflow

1. The API receives the uploaded file at `/video-uploader`.
2. The file is stored temporarily inside the container.
3. `FFmpeg` is executed to extract a thumbnail.
4. Both the video and thumbnail are uploaded to the MinIO bucket (`data`).
5. The service generates **public URLs** for both:
   ```
   https://minio-subdomain.mywebsite.com/data/videos/{user_id}/{uuid}{filename}
   https://minio-subdomain.mywebsite.com/data/thumbnails/{user_id}/{uuid}.jpg
   ```
6. The API responds with success confirmation and URLs.

---

## ⚙️ Configuration

The Spring Boot app is configured via environment variables (see `docker-compose.yml`):

```yaml
environment:
  MINIO_ENDPOINT: http://ffmpeg-minio:9000
  MINIO_ACCESS_KEY: minioadmin
  MINIO_SECRET_KEY: minioadmin
  MINIO_BUCKET: data
  MINIO_URL_PREFIX: http://localhost:9000
```

---

## 🧰 Tech Stack

| Component | Technology |
|------------|-------------|
| **Backend** | Spring Boot 3 + Java 17 |
| **Storage** | MinIO (S3-compatible) |
| **Media Processing** | FFmpeg |
| **Containerization** | Docker & Docker Compose |
| **Build Tool** | Maven |

---

## 🧹 Cleanup

To stop and remove all containers:

```bash
docker compose down -v
```

---

## 🧾 License

This project is open-source and available under the [MIT License](LICENSE).

---

**Author:** [David Innocent](mailto:davidinnocent@live.com)  
**Description:** Video upload and thumbnail generation service powered by Spring Boot, FFmpeg, and MinIO.
