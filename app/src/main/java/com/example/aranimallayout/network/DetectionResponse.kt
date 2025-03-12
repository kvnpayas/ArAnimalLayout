package com.example.aranimallayout.network.models

data class DetectionResponse(
    val images: List<ImageResult>?,
    val metadata: Metadata?
)

data class ImageResult(
    val results: List<Detection>?,
    val shape: List<Int>?,
    val speed: Speed?
)

data class Detection(
    val box: BoundingBox?,
    val `class`: Int?,
    val confidence: Double?,
    val name: String?
)

data class BoundingBox(
    val x1: Double?,
    val x2: Double?,
    val y1: Double?,
    val y2: Double?
)

data class Speed(
    val inference: Double?,
    val postprocess: Double?,
    val preprocess: Double?
)

data class Metadata(
    val functionTimeAlive: Double?,
    val functionTimeCall: Double?,
    val imageCount: Int?,
    val model: String?,
    val version: Version?
)

data class Version(
    val python: String?,
    val torch: String?,
    val torchvision: String?,
    val ultralytics: String?
)