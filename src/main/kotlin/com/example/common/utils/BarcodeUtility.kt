package com.example.common.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.HybridBinarizer
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.util.*
import javax.imageio.ImageIO


class BarcodeUtility {

    companion object {

        fun generate(barcodeFormat: BarcodeFormat, data: Map<String, Any>, height: Int, width: Int): File {
            val hints: MutableMap<EncodeHintType, Any> = EnumMap(com.google.zxing.EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 2

            val matrix = MultiFormatWriter().encode(
                    String(ObjectMapper().writeValueAsBytes(data), StandardCharsets.UTF_8),
                    barcodeFormat, width, height, hints)
            val file = File.createTempFile("barcode", ".png")
            MatrixToImageWriter.writeToFile(
                    matrix,
                    file.path.substring(file.path.lastIndexOf('.') + 1),
                    file)
            return file
        }

        fun read(file: File): Map<String, Any> {
            val binaryBitmap = BinaryBitmap(
                    HybridBinarizer(
                            BufferedImageLuminanceSource(
                                    ImageIO.read(FileInputStream(file))
                            )
                    )
            )
            val result: Result = MultiFormatReader().decode(binaryBitmap)
            return ObjectMapper().readValue(result.text)
        }

    }
}