package com.teacoffee.crm.data.remote.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface SeoApiService {

    @GET("api/v1/keywords")
    suspend fun getKeywordSuggestions(
        @Query("keyword") keyword: String,
        @Query("country") country: String = "IN",
        @Query("language") language: String = "en"
    ): KeywordResponse

    @GET("api/v1/competitors")
    suspend fun getCompetitorAnalysis(
        @Query("domain") domain: String
    ): CompetitorResponse

    data class KeywordResponse(
        val suggestions: List<KeywordSuggestion>? = null,
        val volume: Int = 0,
        val competition: String = "LOW",
        val difficulty: Int = 0
    )

    data class KeywordSuggestion(
        val keyword: String = "",
        val volume: Int = 0,
        val competition: String = "LOW",
        val difficulty: Int = 0
    )

    data class CompetitorResponse(
        val competitors: List<Competitor>? = null,
        val topKeywords: List<String>? = null
    )

    data class Competitor(
        val domain: String = "",
        val traffic: Int = 0,
        val topKeyword: String = ""
    )

    companion object {
        private const val BASE_URL = "https://api.ubersuggest.com/"

        fun create(apiKey: String): SeoApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("API-Key", apiKey)
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SeoApiService::class.java)
        }
    }
}
