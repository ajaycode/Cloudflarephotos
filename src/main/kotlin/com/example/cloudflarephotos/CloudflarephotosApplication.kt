package com.example.cloudflarephotos

import com.example.cloudflarephotos.service.CloudImageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import java.nio.file.Paths

/**
 * @link https://spring.io/guides/gs/uploading-files/
 * @link https://dimitr.im/consuming-rest-apis-with-spring
 */

@SpringBootApplication
class CloudflarephotosApplication

fun main(args: Array<String>) {
    runApplication<CloudflarephotosApplication>(*args)
}


@Component
class Initializer: CommandLineRunner {
    @Autowired
    lateinit var cloudImageService: CloudImageService

    //File to be uploaded must exist at this location
    @Value("\${application.image-to-upload}")
    private lateinit var pathToImageFile: String

    @Override
    override fun run(vararg args: String?) {
        cloudImageService.requestUploadURL()

        var imageId : String? = cloudImageService.uploadImage(Paths.get(pathToImageFile))
        if (!imageId.isNullOrEmpty())
        {
            println ("Image was uploaded.")
            println ("Checking if image exists.")
            val bool = cloudImageService.imageExists(imageId)
            if (bool) {
                println("Image exists")
                println ("Deleting image...")
                cloudImageService.deleteImage(imageId)
            }
        }

    }
}