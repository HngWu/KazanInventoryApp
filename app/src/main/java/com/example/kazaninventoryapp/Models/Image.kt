package com.example.kazaninventoryapp.Models

import kotlinx.serialization.Serializable

@Serializable
data class ImageData(
    val id: String,
    val name: ByteArray,
    val data: ByteArray,
    val length: Int,
    val width: Int,
    val height: Int,
    val contentType: String
)