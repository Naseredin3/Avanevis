package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiEndpoints {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiEndpoints by lazy {
        retrofit.create(GeminiEndpoints::class.java)
    }

    fun getApiKey(customKey: String? = null): String {
        if (!customKey.isNullOrBlank()) {
            return customKey.trim()
        }
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNotBlank() && !key.contains("MY_GEMINI_API_KEY")) {
                key.trim()
            } else {
                ""
            }
        } catch (e: Throwable) {
            ""
        }
    }

    suspend fun testApiKeyConnection(apiKey: String): Result<String> {
        return try {
            val testRequest = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = "Respond with 'OK' only."))
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.1f,
                    maxOutputTokens = 10
                )
            )
            val response = service.generateContent(apiKey.trim(), testRequest)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (response.error != null) {
                Result.failure(Exception(response.error.message ?: "خطای ناشناخته از سرور Gemini"))
            } else if (!text.isNullOrBlank()) {
                Result.success("اتصال موفقیت‌آمیز بود! کلید API فعال و معتبر است.")
            } else {
                Result.failure(Exception("پاسخی از سرور دریافت نشد."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun translateTextOnline(
        apiKey: String,
        sourceText: String,
        targetLanguage: String
    ): Result<String> {
        return try {
            val systemPrompt = "You are a professional translator. Translate the given text accurately and naturally into $targetLanguage. Preserve the original formatting, paragraph breaks, tone, and technical terminology. Do NOT add notes, explanations, or quotes around the output; return only the translated text."
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = sourceText))
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.3f,
                    maxOutputTokens = 8192
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt))
                )
            )
            val response = service.generateContent(apiKey.trim(), request)
            val translated = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (response.error != null) {
                Result.failure(Exception(response.error.message ?: "خطا در برقراری ارتباط با سرویس ترجمه"))
            } else if (!translated.isNullOrBlank()) {
                Result.success(translated.trim())
            } else {
                Result.failure(Exception("متن ترجمه خالی دریافت شد."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseErrorMessage(e: Throwable): String {
        if (e is retrofit2.HttpException) {
            val code = e.code()
            val errorBody = try { e.response()?.errorBody()?.string() } catch (ignored: Exception) { null } ?: ""
            return when {
                code == 400 && (errorBody.contains("API_KEY_INVALID", true) || errorBody.contains("API key not valid", true)) ->
                    "کلید API هوش مصنوعی معتبر نیست. لطفاً از طریق آیکون کلید بالای صفحه، کلید Gemini معتبر خود را وارد نمایید."
                code == 400 && errorBody.contains("INVALID_ARGUMENT", true) ->
                    "خطا در فرمت یا داده‌های صوتی ارسالی به هوش مصنوعی. لطفاً فایل صوتی استانداردی مانند MP3 یا M4A انتخاب نمایید."
                code == 400 ->
                    "درخواست ارسالی توسط سرور هوش مصنوعی پذیرفته نشد (کد ۴۰۰). لطفاً فرمت و حجم فایل را بررسی کنید."
                code == 429 ->
                    "سقف مجاز درخواست‌های کلید هوش مصنوعی پر شده است (کد ۴۲۹). لطفاً لحظاتی دیگر تلاش فرمایید یا کلید دیگری وارد کنید."
                code == 403 ->
                    "دسترسی به سرویس هوش مصنوعی با این کلید محدود است (کد ۴۰۳)."
                code == 404 ->
                    "سرویس یا مدل هوش مصنوعی در دسترس نیست (کد ۴۰۴)."
                code == 413 ->
                    "حجم فایل ارسالی برای پردازش مستقیم آنلاین بیش از حد مجاز است."
                code in 500..599 ->
                    "سرور هوش مصنوعی موقتاً با اختلال مواجه است (کد $code). لطفاً دقایقی دیگر مجدداً امتحان فرمایید."
                else ->
                    "خطا در ارتباط با سرور هوش مصنوعی (کد $code)"
            }
        }
        if (e is java.net.SocketTimeoutException) {
            return "زمان پاسخ‌گویی هوش مصنوعی به پایان رسید (Timeout). لطفاً اتصال اینترنت خود را بررسی نمایید."
        }
        if (e is java.net.UnknownHostException) {
            return "عدم برقراری ارتباط با اینترنت. لطفاً اتصال شبکه را بررسی فرمایید."
        }
        return e.localizedMessage ?: "خطای ناشناخته در پردازش صوت یا هوش مصنوعی."
    }
}

