package com.example.common.utils

import com.opencsv.CSVReader
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

/**
 * @project IntelliJ IDEA
 * @author mir00r on 2/4/21
 */
class CSVUtil {
    companion object {

        @JvmStatic
        @Throws(IOException::class)
        fun getReader(path: String): CSVReader {
            val inputStream: InputStream = ClassPathResource(path).inputStream
            return CSVUtil.getReader(inputStream)
        }


        @JvmStatic
        @Throws(IOException::class)
        fun getReader(inputStream: InputStream): CSVReader {
            val reader: Reader = InputStreamReader(inputStream, "UTF-8")
            return CSVReader(reader)
        }

    }
}
