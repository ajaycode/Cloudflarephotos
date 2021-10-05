package com.example.cloudflarephotos.controller


import com.example.cloudflarephotos.exception.StorageFileNotFoundException
import com.example.cloudflarephotos.service.CloudImageService
import com.example.cloudflarephotos.service.FileSystemService
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.nio.file.Paths


@Controller
class FileUploadController {

    @Autowired
    lateinit var fileSystemService: FileSystemService

    @Autowired
    lateinit var cloudImageService: CloudImageService

    @Value("\${application.images.directory-root}")
    private lateinit var imageDirectoryRoot: String

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        var logger: Log = LogFactory.getLog(javaClass.enclosingClass)
    }


    @GetMapping("/")
    fun index(): ModelAndView {
        var mv = ModelAndView()
        mv.viewName = "fileUpload"
        /*var actionURL = cloudImageService.requestUploadURL()
        if (!actionURL.isNullOrEmpty()){
            mv.addObject("directUploadURL", actionURL)
        } else
            mv.addObject("errorMessage", "Cannot upload photos. Please retry later.")*/
        return mv
    }

    @PostMapping("/file/upload")
    fun fileUpload(@RequestParam("file") file: MultipartFile, redirectAttributes: RedirectAttributes): String {
        var exceptionHappened = false
        if (file.isEmpty) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload")
            return "redirect:/uploadStatus"
        }
        try {
            fileSystemService.writeToFileSystem(file)
        } catch (e: Exception) {
            exceptionHappened = true
            logger.info(e.printStackTrace())
            logger.info(e.message)
            redirectAttributes.addFlashAttribute("message", "Error occurred uploading the file.")
            return "redirect:/uploadStatus"
        }
        if (!exceptionHappened) {
            redirectAttributes.addFlashAttribute("message", "File was uploaded to the server successfully.")
            var path = Paths.get(imageDirectoryRoot, file.originalFilename)
            cloudImageService.uploadImage(path)
        }

        return "uploadStatus"
    }

    /*@GetMapping("/files/{filename:.+}")
    @ResponseBody
    fun serveFile(@PathVariable filename: String?): ResponseEntity<Resource?>? {
        val file: Resource? = filename?.let { fileSystemService.loadAsResource(it) }
        if (file != null) {
            return ResponseEntity.ok()
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename().toString() + "\""
                )
                .body<Resource?>(file)
        } else {
            return null
        }
    }*/


    @ExceptionHandler(StorageFileNotFoundException::class)
    fun handleStorageFileNotFound(exc: StorageFileNotFoundException?): ResponseEntity<*>? {
        return ResponseEntity.notFound().build<Any>()
    }

}
