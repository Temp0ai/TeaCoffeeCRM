package com.teacoffee.crm.util

import android.util.Base64
import com.teacoffee.crm.data.remote.api.GmailApiService
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GmailParser @Inject constructor() {

    data class ParsedMessage(
        val id: String,
        val threadId: String,
        val from: String,
        val fromName: String,
        val subject: String,
        val body: String,
        val receivedAt: Long
    )

    fun parseMessageDetail(detail: GmailApiService.GmailMessageDetailResponse): ParsedMessage {
        val headers = detail.payload?.headers ?: emptyList()
        val headerMap = headers.associate { it.name?.lowercase() to (it.value ?: "") }

        val from = headerMap["from"] ?: ""
        val fromName = extractName(from)
        val fromEmail = extractEmail(from)
        val subject = headerMap["subject"] ?: ""
        val receivedAt = detail.internalDate?.toLongOrNull() ?: System.currentTimeMillis()

        val body = extractBody(detail.payload)

        return ParsedMessage(
            id = detail.id,
            threadId = detail.threadId,
            from = fromEmail,
            fromName = fromName,
            subject = subject,
            body = body,
            receivedAt = receivedAt
        )
    }

    private fun extractBody(payload: GmailApiService.MessagePayload?): String {
        if (payload == null) return ""

        return when {
            payload.body?.data != null && payload.body.data.isNotEmpty() -> {
                decodeBase64(payload.body.data)
            }
            payload.parts != null -> {
                val textPart = findTextPart(payload.parts)
                textPart ?: findHtmlPart(payload.parts)?.let { htmlToText(it) } ?: ""
            }
            else -> ""
        }
    }

    private fun findTextPart(parts: List<GmailApiService.MessagePayload>): String? {
        for (part in parts) {
            when {
                part.mimeType == "text/plain" && part.body?.data != null ->
                    return decodeBase64(part.body.data)
                part.mimeType == "text/plain" && part.parts != null ->
                    return findTextPart(part.parts)
                part.parts != null -> {
                    findTextPart(part.parts)?.let { return it }
                }
            }
        }
        return null
    }

    private fun findHtmlPart(parts: List<GmailApiService.MessagePayload>): String? {
        for (part in parts) {
            when {
                part.mimeType == "text/html" && part.body?.data != null ->
                    return decodeBase64(part.body.data)
                part.mimeType == "multipart/alternative" && part.parts != null -> {
                    val htmlPart = part.parts.find { it.mimeType == "text/html" }
                    if (htmlPart?.body?.data != null) return decodeBase64(htmlPart.body.data)
                    return findHtmlPart(part.parts)
                }
                part.parts != null -> {
                    findHtmlPart(part.parts)?.let { return it }
                }
            }
        }
        return null
    }

    private fun decodeBase64(data: String): String {
        return try {
            val decoded = Base64.decode(data, Base64.URL_SAFE)
            String(decoded, Charsets.UTF_8)
        } catch (e: Exception) {
            try {
                val decoded = Base64.decode(data, Base64.DEFAULT)
                String(decoded, Charsets.UTF_8)
            } catch (e: Exception) {
                data
            }
        }
    }

    private fun htmlToText(html: String): String {
        return try {
            Jsoup.parse(html).text()
        } catch (e: Exception) {
            html.replace(Regex("<[^>]*>"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }
    }

    private fun extractName(from: String): String {
        val nameRegex = Regex("""^"?([^"<]+)"?\s*<""")
        return nameRegex.find(from)?.groupValues?.getOrNull(1)?.trim() ?: from.substringBefore("@").replace(".", " ")
    }

    private fun extractEmail(from: String): String {
        val emailRegex = Regex("""<([^>]+)>""")
        return emailRegex.find(from)?.groupValues?.getOrNull(1)
            ?: from.trim().takeIf { it.contains("@") }
            ?: ""
    }
}
