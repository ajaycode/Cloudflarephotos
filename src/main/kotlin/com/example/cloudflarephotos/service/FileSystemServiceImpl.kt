package com.example.cloudflarephotos.service


import com.example.cloudflarephotos.exception.StorageException
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Stream
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isRegularFile


@Service
class FileSystemServiceImpl : FileSystemService {

    @Value("\${application.images.directory-root:images}")
    override lateinit var imageDirectoryRoot: Path

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        var logger: Log = LogFactory.getLog(javaClass.enclosingClass)
    }

    override fun init() {
        try {
            Files.createDirectories(imageDirectoryRoot)
        } catch (e: IOException) {
            throw StorageException("Could not initialize storage", e)
        }
    }

    override fun writeToFileSystem(file: MultipartFile) {
        try {
            if (file.isEmpty) {
                throw StorageException("Failed to store empty file.")
            }
            val destinationFile: Path = this.imageDirectoryRoot.resolve(
                Paths.get(file.originalFilename)
            )
                .normalize().toAbsolutePath()
            val destinationDirectory = Paths.get(destinationFile.parent.toString())
            logger.info("Destination directory: ${destinationDirectory.toString()}")
            if (!Files.exists(destinationDirectory)) {
                logger.error("Destination directory ${destinationDirectory.toString()} does not exist. It must be created manually.")
                throw StorageException("Destination directory does not exist.")
            }
            if (destinationFile.parent != this.imageDirectoryRoot.toAbsolutePath()) {
                // This is a security check
                throw StorageException(
                    "Cannot store file outside current directory."
                )
            }
            file.inputStream.use { inputStream ->
                Files.copy(
                    inputStream, destinationFile,
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        } catch (e: IOException) {
            throw StorageException("Failed to store file.", e)
        }
    }

    override fun loadAll(): Stream<Path?>? {
        return try {
            Files.walk(this.imageDirectoryRoot, 1)
                .filter { path -> !path.equals(this.imageDirectoryRoot) }
                .map(this.imageDirectoryRoot::relativize)
        } catch (e: IOException) {
            throw StorageException("Failed to read stored files", e)
        }
    }

    override fun load(filename: String?): Path? {
        TODO("Not yet implemented")
    }

    /*override fun loadAsResource(filename: String): Resource? {
        return try {
            val file = load(filename) ?: return null
            val resource: Resource = UrlResource(file.toUri())
            if (resource.exists() || resource.isReadable) {
                resource
            } else {
                throw StorageFileNotFoundException(
                    "Could not read file: $filename"
                )
            }
        } catch (e: MalformedURLException) {
            throw StorageFileNotFoundException("Could not read file: $filename", e)
        }
    }*/

    /**
     * Deletes a file
     * Will not delete a directory
     * @return true - if file was deleted
     */
    override fun delete(filename: String): Boolean {
        var path: Path = Paths.get(filename)

        //Delete files only in the designated folder
        //Delete file (not directory)
        if (path.parent.toAbsolutePath() != imageDirectoryRoot && path.isRegularFile()) {
            CloudImageServiceImpl.logger.error("File deletion requested from non designated folder. Aborting upload ...")
            return false
        }
        var isDeleted : Boolean = false
        try {
            isDeleted = path.deleteIfExists()
        } catch (e: Exception) {
            logger.error ("Exception deleting file. ${e.message}")
        }
        return (isDeleted)
    }

    override fun deleteAll() {
        FileSystemUtils.deleteRecursively(imageDirectoryRoot.toFile());
    }
}