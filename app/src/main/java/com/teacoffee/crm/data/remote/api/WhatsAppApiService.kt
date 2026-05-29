package com.teacoffee.crm.data.remote.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface WhatsAppApiService {

    @POST("{phoneNumberId}/messages")
    suspend fun sendMessage(
        @Path("phoneNumberId") phoneNumberId: String,
        @Body message: SendMessageRequest
    ): SendMessageResponse

    @GET("{phoneNumberId}/message_templates")
    suspend fun getMessageTemplates(
        @Path("phoneNumberId") phoneNumberId: String
    ): MessageTemplateResponse

    @POST("{phoneNumberId}/messages")
    suspend fun sendTemplateMessage(
        @Path("phoneNumberId") phoneNumberId: String,
        @Body request: SendTemplateRequest
    ): SendMessageResponse

    data class SendMessageRequest(
        val messaging_product: String = "whatsapp",
        val to: String,
        val type: String = "text",
        val text: TextContent
    )

    data class TextContent(
        val preview_url: Boolean = false,
        val body: String
    )

    data class SendTemplateRequest(
        val messaging_product: String = "whatsapp",
        val to: String,
        val type: String = "template",
        val template: TemplateContent
    )

    data class TemplateContent(
        val name: String,
        val language: Language
    )

    data class Language(
        val code: String = "en"
    )

    data class SendMessageResponse(
        val messaging_product: String = "",
        val contacts: List<Contact>? = null,
        val messages: List<MessageStatus>? = null
    )

    data class Contact(
        val input: String = "",
        val wa_id: String = ""
    )

    data class MessageStatus(
        val id: String = ""
    )

    data class MessageTemplateResponse(
        val data: List<TemplateData>? = null
    )

    data class TemplateData(
        val name: String = "",
        val status: String = ""
    )

    companion object {
        fun create(accessToken: String): WhatsAppApiService {
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
                .baseUrl("https://graph.facebook.com/v18.0/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WhatsAppApiService::class.java)
        }
    }
}
