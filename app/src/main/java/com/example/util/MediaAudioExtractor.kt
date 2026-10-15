package com.example.util

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer

data class MediaFileInfo(
    val name: String,
    val sizeBytes: Long,
    val durationSeconds: Long,
    val mimeType: String,
    val isVideo: Boolean,
    val width: Int = 0,
    val height: Int = 0
)

data class ExtractedAudioData(
    val base64Data: String,
    val mimeType: String,
    val originalFileInfo: MediaFileInfo,
    val isExtractedFromVideo: Boolean = false
)

object MediaAudioExtractor {

    private const val MAX_INLINE_BYTES = 18 * 1024 * 1024 // 18MB max inline payload for Gemini API

    suspend fun getMediaInfo(context: Context, uri: Uri): MediaFileInfo = withContext(Dispatchers.IO) {
        var name = "فایل انتخاب شده"
        var size: Long = 0L

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
            }
        }

        val retriever = MediaMetadataRetriever()
        var durationMs: Long = 0L
        var mimeType = context.contentResolver.getType(uri) ?: "video/mp4"
        var width = 0
        var height = 0
        var hasVideo = false

        try {
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val extractedMime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val hasVideoStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)

            if (!durationStr.isNullOrBlank()) durationMs = durationStr.toLongOrNull() ?: 0L
            if (!extractedMime.isNullOrBlank()) mimeType = extractedMime
            if (!widthStr.isNullOrBlank()) width = widthStr.toIntOrNull() ?: 0
            if (!heightStr.isNullOrBlank()) height = heightStr.toIntOrNull() ?: 0
            hasVideo = hasVideoStr == "yes" || mimeType.startsWith("video") ||
                    name.endsWith(".mp4", true) || name.endsWith(".mkv", true) ||
                    name.endsWith(".mov", true) || name.endsWith(".avi", true) ||
                    name.endsWith(".3gp", true) || name.endsWith(".webm", true)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {}
        }

        MediaFileInfo(
            name = name,
            sizeBytes = size,
            durationSeconds = durationMs / 1000L,
            mimeType = mimeType,
            isVideo = hasVideo,
            width = width,
            height = height
        )
    }

    /**
     * Extracts pure audio track from a video file into a standard M4A (AAC) container.
     * This dramatically reduces payload size from tens/hundreds of MBs to a small 1-3MB audio file,
     * which prevents corrupt container errors and API payload overflow.
     */
    private fun extractAudioTrackFromVideo(context: Context, uri: Uri): Pair<ByteArray, String>? {
        val pfd = try {
            context.contentResolver.openFileDescriptor(uri, "r")
        } catch (e: Exception) {
            null
        } ?: return null

        var extractor: MediaExtractor? = null
        var muxer: MediaMuxer? = null
        val tempFile = File(context.cacheDir, "extracted_audio_${System.currentTimeMillis()}.m4a")

        try {
            extractor = MediaExtractor()
            extractor.setDataSource(pfd.fileDescriptor)

            var audioTrackIndex = -1
            var audioFormat: MediaFormat? = null

            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    audioFormat = format
                    break
                }
            }

            if (audioTrackIndex == -1 || audioFormat == null) {
                return null
            }

            muxer = MediaMuxer(tempFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val muxerTrackIndex = muxer.addTrack(audioFormat)
            muxer.start()

            extractor.selectTrack(audioTrackIndex)
            val maxBufferSize = if (audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                val size = audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                if (size > 0) size else 512 * 1024
            } else {
                512 * 1024
            }

            val buffer = ByteBuffer.allocate(maxBufferSize)
            val bufferInfo = MediaCodec.BufferInfo()

            while (true) {
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)
                if (bufferInfo.size < 0) {
                    break
                }
                bufferInfo.presentationTimeUs = extractor.sampleTime
                bufferInfo.flags = extractor.sampleFlags
                muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo)
                extractor.advance()
            }

            muxer.stop()
            muxer.release()
            muxer = null

            extractor.release()
            extractor = null

            if (tempFile.exists() && tempFile.length() > 0) {
                val bytes = tempFile.readBytes()
                return Pair(bytes, "audio/m4a")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try { muxer?.release() } catch (ignored: Exception) {}
            try { extractor?.release() } catch (ignored: Exception) {}
            try { pfd.close() } catch (ignored: Exception) {}
            try { tempFile.delete() } catch (ignored: Exception) {}
        }
        return null
    }

    /**
     * Resolves the exact MIME type accepted by Google Gemini multimodal API:
     * Supported audio: audio/mp3, audio/wav, audio/m4a, audio/aac, audio/ogg, audio/flac, audio/mp4
     * Supported video: video/mp4, video/webm, video/quicktime
     */
    fun resolveProperMimeType(fileName: String, systemMime: String): String {
        val lowerName = fileName.lowercase()
        val lowerMime = systemMime.lowercase()

        return when {
            lowerName.endsWith(".mp3") || lowerMime.contains("mpeg") || lowerMime.contains("mp3") -> "audio/mp3"
            lowerName.endsWith(".wav") || lowerMime.contains("wav") -> "audio/wav"
            lowerName.endsWith(".m4a") || lowerMime.contains("m4a") || lowerMime.contains("x-m4a") -> "audio/m4a"
            lowerName.endsWith(".aac") || lowerMime.contains("aac") -> "audio/aac"
            lowerName.endsWith(".ogg") || lowerName.endsWith(".opus") || lowerMime.contains("ogg") || lowerMime.contains("opus") -> "audio/ogg"
            lowerName.endsWith(".flac") || lowerMime.contains("flac") -> "audio/flac"
            lowerName.endsWith(".webm") || lowerMime.contains("webm") -> "video/webm"
            lowerName.endsWith(".mov") || lowerMime.contains("quicktime") -> "video/quicktime"
            lowerMime.startsWith("audio/") -> "audio/mp4"
            else -> "video/mp4"
        }
    }

    suspend fun extractAudioBytesForGemini(
        context: Context,
        uri: Uri
    ): ExtractedAudioData = withContext(Dispatchers.IO) {
        val info = getMediaInfo(context, uri)

        // Step 1: If it is a video (or media with an audio track), try extracting pure audio track first
        if (info.isVideo) {
            val extractedAudio = extractAudioTrackFromVideo(context, uri)
            if (extractedAudio != null) {
                val (rawBytes, targetMime) = extractedAudio
                if (rawBytes.size > MAX_INLINE_BYTES) {
                    val sizeMb = rawBytes.size / (1024f * 1024f)
                    throw IllegalArgumentException(
                        "حجم جریان صوتی استخراج شده (${String.format("%.1f", sizeMb)} مگابایت) بیش از حد مجاز هوش مصنوعی است. لطفاً فایل کوتاه‌تری انتخاب نمایید."
                    )
                }
                val base64 = Base64.encodeToString(rawBytes, Base64.NO_WRAP)
                return@withContext ExtractedAudioData(
                    base64Data = base64,
                    mimeType = targetMime,
                    originalFileInfo = info,
                    isExtractedFromVideo = true
                )
            }
        }

        // Step 2: If not extracted (direct audio or fallback for small video)
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("امکان باز کردن فایل انتخاب شده وجود ندارد.")

        val buffer = ByteArrayOutputStream()
        inputStream.use { input ->
            val data = ByteArray(16384)
            var totalRead = 0
            var nRead: Int
            while (input.read(data, 0, data.size).also { nRead = it } != -1) {
                buffer.write(data, 0, nRead)
                totalRead += nRead
                if (totalRead > MAX_INLINE_BYTES) {
                    val sizeMb = totalRead / (1024f * 1024f)
                    throw IllegalArgumentException(
                        "حجم فایل بیش از ۱۸ مگابایت است (${String.format("%.1f", sizeMb)} MB). ارسال مستقیم به هوش مصنوعی با این حجم ممکن نیست. لطفاً یک فایل صوتی کم‌حجم‌تر انتخاب کنید."
                    )
                }
            }
        }

        val rawBytes = buffer.toByteArray()
        if (rawBytes.isEmpty()) {
            throw IllegalStateException("فایل انتخاب شده خالی است یا داده‌ای از آن خوانده نشد.")
        }

        val targetMime = resolveProperMimeType(info.name, info.mimeType)
        val base64 = Base64.encodeToString(rawBytes, Base64.NO_WRAP)

        ExtractedAudioData(
            base64Data = base64,
            mimeType = targetMime,
            originalFileInfo = info,
            isExtractedFromVideo = false
        )
    }
}
