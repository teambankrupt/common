package com.example.common.utils

import java.io.File
import java.io.IOException
import java.util.*


class ReportUtil private constructor() {

    companion object {

        @Throws(IOException::class, InterruptedException::class)
        fun generatePdf(url: String): File {
            val fileName = UUID.randomUUID().toString() + ".pdf"
            Shell.exec("wkhtmltopdf $url $fileName")
            return File(fileName)
        }

        @Throws(IOException::class, InterruptedException::class)
        fun generateImage(url: String): File {
            val fileName = UUID.randomUUID().toString() + ".png"
            Shell.exec("wkhtmltoimage $url $fileName")
            return File(fileName)
        }

    }

}
