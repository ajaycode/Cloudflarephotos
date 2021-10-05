package com.example.cloudflarephotos.service

import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path
import java.util.stream.Stream

/**
 * @link: based on <a href="https://github.com/spring-guides/gs-uploading-files">Spring examples</a>
 */
interface FileSystemService  {

    var imageDirectoryRoot : Path

    fun init()

    fun writeToFileSystem(file: MultipartFile)

    fun loadAll(): Stream<Path?>?

    fun load(filename: String?): Path?

    fun delete (filename: String): Boolean

    fun deleteAll()

}