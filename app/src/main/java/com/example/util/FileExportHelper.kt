package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.TranscriptItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportFormat(val extension: String, val displayName: String, val mimeType: String) {
    TXT("txt", "متن ساده (TXT)", "text/plain"),
    SRT("srt", "زیرنویس فیلم (SRT)", "application/x-subrip"),
    VTT("vtt", "زیرنویس وب (VTT)", "text/vtt"),
    MARKDOWN("md", "سند مارک‌داون (MD)", "text/markdown"),
    JSON("json", "ساختاریافته (JSON)", "application/json")
}

object FileExportHelper {

    fun generateExportContent(item: TranscriptItem, format: ExportFormat): String {
        return when (format) {
            ExportFormat.TXT -> buildString {
                appendLine("عنوان: ${item.title}")
                appendLine("فایل مبدا: ${item.sourceFileName}")
                appendLine("مدت زمان: ${item.formattedDuration} | تعداد کلمات: ${item.wordCount}")
                appendLine("تاریخ استخراج: ${formatDate(item.createdAt)}")
                appendLine("----------------------------------------")
                appendLine()
                if (!item.summary.isNullOrBlank()) {
                    appendLine("خلاصه:")
                    appendLine(item.summary)
                    appendLine()
                    appendLine("----------------------------------------")
                    appendLine()
                }
                appendLine("متن استخراج شده:")
                appendLine(item.rawText)
            }

            ExportFormat.SRT -> {
                if (!item.timestampsText.isNullOrBlank() && item.timestampsText.contains("-->")) {
                    item.timestampsText
                } else {
                    convertToSrt(item.rawText)
                }
            }

            ExportFormat.VTT -> {
                buildString {
                    appendLine("WEBVTT")
                    appendLine()
                    appendLine("NOTE عنوان: ${item.title}")
                    appendLine()
                    if (!item.timestampsText.isNullOrBlank() && item.timestampsText.contains("-->")) {
                        appendLine(item.timestampsText.replace(",", "."))
                    } else {
                        appendLine(convertToVtt(item.rawText))
                    }
                }
            }

            ExportFormat.MARKDOWN -> buildString {
                appendLine("# ${item.title}")
                appendLine()
                appendLine("- **فایل منبع:** `${item.sourceFileName}`")
                appendLine("- **مدت زمان:** `${item.formattedDuration}`")
                appendLine("- **تعداد کلمات:** `${item.wordCount}`")
                appendLine("- **تاریخ استخراج:** `${formatDate(item.createdAt)}`")
                appendLine()
                if (!item.summary.isNullOrBlank()) {
                    appendLine("## 📌 خلاصه هوشمند")
                    appendLine(item.summary)
                    appendLine()
                }
                if (!item.keyPoints.isNullOrBlank()) {
                    appendLine("## 💡 نکات کلیدی")
                    appendLine(item.keyPoints)
                    appendLine()
                }
                appendLine("## 📝 متن پیاده‌سازی شده")
                appendLine(item.rawText)
            }

            ExportFormat.JSON -> buildString {
                appendLine("{")
                appendLine("  \"title\": \"${escapeJson(item.title)}\",")
                appendLine("  \"sourceFile\": \"${escapeJson(item.sourceFileName)}\",")
                appendLine("  \"durationSeconds\": ${item.durationSeconds},")
                appendLine("  \"wordCount\": ${item.wordCount},")
                appendLine("  \"createdAt\": ${item.createdAt},")
                appendLine("  \"summary\": \"${escapeJson(item.summary ?: "")}\",")
                appendLine("  \"keyPoints\": \"${escapeJson(item.keyPoints ?: "")}\",")
                appendLine("  \"transcript\": \"${escapeJson(item.rawText)}\"")
                appendLine("}")
            }
        }
    }

    private fun convertToSrt(rawText: String): String {
        val sentences = rawText.split(Regex("(?<=[.!?؟\n])\\s+"))
            .filter { it.isNotBlank() }
        if (sentences.isEmpty()) return "1\n00:00:00,000 --> 00:00:05,000\n$rawText"

        val sb = StringBuilder()
        var currentSecond = 0
        sentences.forEachIndexed { index, sentence ->
            val duration = (sentence.length / 15).coerceIn(3, 8)
            val startMs = currentSecond * 1000L
            val endMs = (currentSecond + duration) * 1000L
            currentSecond += duration

            sb.appendLine("${index + 1}")
            sb.appendLine("${formatSrtTime(startMs)} --> ${formatSrtTime(endMs)}")
            sb.appendLine(sentence.trim())
            sb.appendLine()
        }
        return sb.toString().trim()
    }

    private fun convertToVtt(rawText: String): String {
        return convertToSrt(rawText).replace(",", ".")
    }

    private fun formatSrtTime(millis: Long): String {
        val hours = millis / 3600000
        val minutes = (millis % 3600000) / 60000
        val seconds = (millis % 60000) / 1000
        val ms = millis % 1000
        return String.format(Locale.US, "%02d:%02d:%02d,%03d", hours, minutes, seconds, ms)
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun escapeJson(text: String): String {
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    fun copyToClipboard(context: Context, text: String, label: String = "متن رونویسی شده") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "متن در حافظه کپی شد", Toast.LENGTH_SHORT).show()
    }

    fun shareText(context: Context, text: String, title: String = "اشتراک‌گذاری متن استخراج شده") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_TITLE, title)
        }
        val chooser = Intent.createChooser(intent, title)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    suspend fun writeContentToUri(context: Context, uri: Uri, content: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
                outputStream?.use { stream ->
                    stream.write(content.toByteArray(Charsets.UTF_8))
                    stream.flush()
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
}
