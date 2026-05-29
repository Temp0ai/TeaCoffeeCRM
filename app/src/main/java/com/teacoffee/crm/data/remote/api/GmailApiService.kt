package com.teacoffee.crm.data.remote.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface GmailApiService {

    @GET("gmail/v1/users/me/messages")
    suspend fun listMessages(
        @Query("q") query: String? = null,
        @Query("maxResults") maxResults: Int = 50,
        @Query("pageToken") pageToken: String? = null
    ): GmailMessageListResponse

    @GET("gmail/v1/users/me/messages/{id}")
    suspend fun getMessage(
        @Path("id") messageId: String,
        @Query("format") format: String = "full"
    ): GmailMessageDetailResponse

    @GET("gmail/v1/users/me/threads/{threadId}")
    suspend fun getThread(
        @Path("threadId") threadId: String
    ): GmailThreadResponse

    data class GmailMessageListResponse(
        val messages: List<MessageRef>? = emptyList(),
        val nextPageToken: String? = null,
        val resultSizeEstimate: Int = 0
    )

    data class MessageRef(
        val id: String,
        val threadId: String
    )

    data class GmailMessageDetailResponse(
        val id: String,
        val threadId: String,
        val labelIds: List<String>? = null,
        val snippet: String? = null,
        val payload: MessagePayload? = null,
        val internalDate: String? = null
    )

    data class MessagePayload(
        val mimeType: String? = null,
        val headers: List<MessageHeader>? = null,
        val body: MessageBody? = null,
        val parts: List<MessagePayload>? = null
    )

    data class MessageHeader(
        val name: String? = null,
        val value: String? = null
    )

    data class MessageBody(
        val size: Int = 0,
        val data: String? = null
    )

    data class GmailThreadResponse(
        val id: String,
        val messages: List<GmailMessageDetailResponse>? = null
    )

    companion object {
        fun create(accessToken: String): GmailApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $accessToken")
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GmailApiService::class.java)
        }
    }
}
