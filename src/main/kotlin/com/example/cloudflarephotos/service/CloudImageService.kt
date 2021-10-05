package com.example.cloudflarephotos.service

import java.nio.file.Path


/**
 * Handles the upload, deletion and management of images that are stored in the cloud
 * @see <a href="https://developers.cloudflare.com/images/">Cloudflare images</a>
 */
interface CloudImageService  {
    fun uploadImage (path: Path): String?
    fun imageExists (imageId: String) : Boolean
    fun deleteImage (imageId: String) : Boolean
    fun requestUploadURL () : String?
}