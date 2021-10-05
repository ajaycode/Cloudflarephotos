package com.example.cloudflarephotos.service

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.fileSize


class JSONInputToRequestDownloadURL {
    var requireSignedURLs: Boolean = true
}

class Result {
    var id: String? = null
    var uploadURL: String? = null
}

/**
 * @See <a href="https://developers.cloudflare.com/images/#making-your-first-api-request">JSON Response from Cloudflare</a>
 * JSON converted to POJO at https://json2csharp.com/json-to-pojo
 */
class CloudFlareResponse {
    var result: Result? = null
    var result_info: Any? = null
    var success = false
    var errors: List<Any> = listOf()
    var messages: List<Any>? = listOf()
}

/**
 * Handles the upload, deletion and management of images that are stored in the cloud
 * @see <a href="https://developers.cloudflare.com/images/">Cloudflare images</a>
 */
@Service
class CloudImageServiceImpl : CloudImageService {

    @Value("\${cloudflare.bearer-token}")
    private lateinit var bearerToken: String

    @Value("\${cloudflare.image.direct-upload-request-url}")
    private lateinit var requestURL: String

    @Value("\${cloudflare.image.delete-url.prefix}")
    private lateinit var deleteURLPrefix: String

    @Value("\${cloudflare.image.upload-url}")
    private lateinit var uploadURL: String

    @Value("\${application.images.directory-root}")
    private lateinit var imageDirectoryRoot: String

    @Value("\${cloudflare.image.details-url.prefix}")
    private lateinit var imageDetailsURLPrefix: String

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        var logger: Log = LogFactory.getLog(javaClass.enclosingClass)
    }


    /**
     * Uploads an image to Cloudflare
     * @return - the id where the image is available
     *         - null, if the upload was not successful
     */
    override fun uploadImage(path: Path): String? {
        if (!path.exists() || path.fileSize() == 0L) {
            logger.info("File does not exist or is an empty file. ${path.fileName}")
            return null
        }
        //Security check: File to be uploaded must be from the designated folder
        if (path.parent.toAbsolutePath().toString() != imageDirectoryRoot){
            logger.error ("Image upload requested from non designated folder. Aborting upload ...")
            return null
        }
        var httpHeaders = HttpHeaders()
        httpHeaders.setBearerAuth(bearerToken)
        httpHeaders.contentType = MediaType.MULTIPART_FORM_DATA

        val map = LinkedMultiValueMap<String, Any>()
        map.add("file", FileSystemResource(path.toFile()))

        val requestEntity = HttpEntity(map, httpHeaders)

        var responseJSON: ResponseEntity<CloudFlareResponse>? = null
        var restTemplate = RestTemplate()
        try {
            responseJSON = restTemplate.exchange(
                uploadURL, HttpMethod.POST, requestEntity,
                CloudFlareResponse::class.java
            )
        } catch (e: Exception) {
            logger.error("Exception during image upload. ${e.message}")
            return null
        }

        if (responseJSON?.body != null && responseJSON.body?.success == true) {
            /*logger.info ("Image deletion status : ${responseJSON.body?.success}")
            logger.info ("returning  ${responseJSON.body?.success == true}")*/
            var imageId = responseJSON.body?.result?.id
            logger.info ("Id of the image: ${imageId}")
            return (imageId)
        } else {
            /*logger.info ("Returning false")*/
            return null
        }
    }

    override fun imageExists(imageId: String): Boolean {
        var httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        httpHeaders.setBearerAuth(bearerToken)
        val imageDetailsURI = imageDetailsURLPrefix + imageId
        val request = HttpEntity<Any>(httpHeaders)
        var restTemplate = RestTemplate()
        var responseJSON : ResponseEntity<CloudFlareResponse>? = null
        try {
            responseJSON = restTemplate.exchange(imageDetailsURI, HttpMethod.GET, request, CloudFlareResponse::class.java)
        } catch (e: Exception) {
            logger.error("Exception during retrieval of image meta-data. ${e.message}")
            return false
        }
        //logger.info (responseJSON)
        return (responseJSON.body?.success == true)
    }

    /**
     * @see <a href="https://developers.cloudflare.com/images/deleting-images">Delete Images on Cloudflare</a>
     */
    override fun deleteImage(imageId: String): Boolean {
        var httpHeaders = HttpHeaders()
        httpHeaders.setBearerAuth(bearerToken)
        val deleteImageURI = deleteURLPrefix + imageId

        val request: HttpEntity<*> = HttpEntity<Any>(httpHeaders)
        var restTemplate = RestTemplate()
        var responseJSON: ResponseEntity<CloudFlareResponse>? = null
        try {
            responseJSON =
                restTemplate.exchange(deleteImageURI, HttpMethod.DELETE, request, CloudFlareResponse::class.java)
        } catch (e: Exception) {
            logger.error("Exception during image deletion. ${e.message}")
        }
        if (responseJSON?.body != null) {
            /*logger.info ("Image deletion status : ${responseJSON.body?.success}")
            logger.info ("returning  ${responseJSON.body?.success == true}")*/
            return (responseJSON.body?.success == true)
        } else {
            /*logger.info ("Returning false")*/
            return false
        }
    }

    /**
     * Requests Cloudflare for a URL that may be placed in the form
     * Solves the issue of having intermediate storage/upload to the application server
     * The file may be uploaded directly to the storage provider
     * @return: URL to be used in the form or null, if the request was not successful
     */
    override fun requestUploadURL(): String? {
        var imageUploadURL: String? = null
        var httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        httpHeaders.setBearerAuth(bearerToken)

        val inputData = JSONInputToRequestDownloadURL()
        var httpEntity = HttpEntity(inputData, httpHeaders)
        //logger.info (httpEntity.body?.requireSignedURLs ?: "If not found")

        var uri = URI(requestURL)
        var restTemplate = RestTemplate()
        val responseJSON = restTemplate.postForObject(uri, httpEntity, CloudFlareResponse::class.java)
        if (responseJSON != null) {
            if (responseJSON.success == true) {
                imageUploadURL = responseJSON.result?.uploadURL
                logger.info(imageUploadURL)
            } else if (responseJSON.errors.isNotEmpty()) {
                for (error in responseJSON.errors)
                    logger.error(error.toString())
            }
        }
        if (imageUploadURL == null) {
            logger.error("Did not receive a direct upload URL from Cloudflare. User cannot upload images.")
        }
        return imageUploadURL
    }
}