package com.example.common.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import java.io.File
import java.nio.charset.StandardCharsets


class BarcodeUtility {

    companion object {

        fun generate(barcodeFormat: BarcodeFormat, data: Map<String,Any>, height: Int, width: Int): File {
            val matrix = MultiFormatWriter().encode(
                    String(ObjectMapper().writeValueAsBytes(data), StandardCharsets.UTF_8),
                    barcodeFormat, width, height)
            val file = File.createTempFile("barcode", ".png")
            MatrixToImageWriter.writeToFile(
                    matrix,
                    file.path.substring(file.path.lastIndexOf('.') + 1),
                    file)
            return file
        }

    }
}