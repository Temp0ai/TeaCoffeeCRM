package com.teacoffee.crm.util

import com.teacoffee.crm.data.local.entity.LeadEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiEngine @Inject constructor() {

    suspend fun generateFollowUpMessage(lead: LeadEntity, context: String = ""): String {
        return withContext(Dispatchers.IO) {
            val prompt = buildFollowUpPrompt(lead, context)
            callAiApi(prompt)
        }
    }

    suspend fun generateBulkMessage(
        leads: List<LeadEntity>,
        category: String,
        campaignGoal: String
    ): String {
        return withContext(Dispatchers.IO) {
            val prompt = buildBulkMessagePrompt(leads, category, campaignGoal)
            callAiApi(prompt)
        }
    }

    suspend fun extractLeadFromEmail(
        subject: String,
        body: String,
        fromName: String,
        fromEmail: String
    ): ExtractedLeadData {
        return withContext(Dispatchers.IO) {
            val prompt = """
                Extract lead information from this email:
                From: $fromName <$fromEmail>
                Subject: $subject
                Body: $body
                
                Return JSON with: name, phone, email, company, productRequirement, orderDetails, inquiryDetails, clientType
                Client type should be one of: SOCIETY, CAFE, RESTAURANT, OFFICE, MANUFACTURER, RETAILER, OTHER
            """.trimIndent()

            val raw = callAiApi(prompt)
            parseExtractedLead(raw, fromName, fromEmail)
        }
    }

    suspend fun generateSeoKeywords(
        businessType: String,
        products: List<String>,
        targetMarkets: List<String>
    ): List<KeywordSuggestion> {
        return withContext(Dispatchers.IO) {
            val prompt = """
                Generate SEO keywords for a $businessType business.
                Products: ${products.joinToString(", ")}
                Target Markets: ${targetMarkets.joinToString(", ")}
                Return 20 keywords as a numbered list with search volume estimates.
            """.trimIndent()

            val raw = callAiApi(prompt)
            parseKeywordSuggestions(raw)
        }
    }

    suspend fun generateSocialMediaContent(
        productName: String,
        platform: String,
        targetAudience: String,
        contentType: String
    ): SocialMediaContent {
        return withContext(Dispatchers.IO) {
            val prompt = """
                Create $platform content for $productName.
                Target Audience: $targetAudience
                Content Type: $contentType
                Include: caption, hashtags, and image description.
            """.trimIndent()

            val raw = callAiApi(prompt)
            parseSocialMediaContent(raw)
        }
    }

    suspend fun generateCompetitorAnalysis(
        businessName: String,
        market: String
    ): CompetitorInsights {
        return withContext(Dispatchers.IO) {
            val prompt = """
                Analyze competitors for $businessName in $market market.
                Provide: top 5 competitors, their strategies, gaps in market, recommendations.
            """.trimIndent()

            val raw = callAiApi(prompt)
            parseCompetitorInsights(raw)
        }
    }

    private suspend fun callAiApi(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = URL("https://api.openai.com/v1/chat/completions").openConnection().apply {
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer ${getApiKey()}")
                    doOutput = true
                    outputStream.write(
                        """
                        {
                            "model": "gpt-3.5-turbo",
                            "messages": [{"role": "user", "content": ${jsonEncode(prompt)}}],
                            "temperature": 0.7,
                            "max_tokens": 1000
                        }
                        """.trimIndent().toByteArray()
                    )
                }
                val result = response.inputStream.bufferedReader().readText()
                parseApiResponse(result)
            } catch (e: Exception) {
                fallbackResponse(prompt)
            }
        }
    }

    private fun parseApiResponse(json: String): String {
        val contentMarker = "\"content\":\""
        val start = json.indexOf(contentMarker)
        if (start == -1) return json
        val contentStart = start + contentMarker.length
        val contentEnd = json.indexOf("\"", contentStart)
        return if (contentEnd == -1) json.substring(contentStart)
        else json.substring(contentStart, contentEnd)
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\t", "\t")
            .replace("\\/", "/")
    }

    private fun fallbackResponse(prompt: String): String {
        return when {
            prompt.contains("follow-up", ignoreCase = true) ||
            prompt.contains("follow up", ignoreCase = true) ->
                generateFallbackFollowUp(prompt)
            prompt.contains("Extract lead", ignoreCase = true) ->
                """{"name":"","phone":"","email":"","company":"","productRequirement":"","orderDetails":"","inquiryDetails":"","clientType":"OTHER"}"""
            prompt.contains("SEO", ignoreCase = true) ->
                "1. tea premix supplier\n2. coffee premix wholesale\n3. nescafe machine dealer\n4. tea vending machine\n5. coffee vending machine"
            else -> "Thank you for your inquiry. We specialize in premium tea and coffee products. Please share your requirements for a customized quote."
        }
    }

    private fun generateFallbackFollowUp(prompt: String): String {
        val name = extractNameFromPrompt(prompt)
        return """
            Dear $name,
            
            Hope this message finds you well! We noticed your interest in our premium tea and coffee products.
            
            At TeaCoffee CRM, we offer:
            ✅ Premium Tea & Coffee Premixes
            ✅ State-of-the-art Vending Machines
            ✅ Bulk Supply with Quality Assurance
            ✅ Competitive Pricing & Timely Delivery
            
            Would you like to explore our latest product catalog or discuss bulk pricing?
            
            Looking forward to serving you!
            
            Best regards,
            TeaCoffee Team
        """.trimIndent()
    }

    private fun extractNameFromPrompt(prompt: String): String {
        val nameRegex = Regex("""[Nn]ame[:\s]+(\w+(?:\s+\w+)?)""")
        return nameRegex.find(prompt)?.groupValues?.getOrNull(1) ?: "Customer"
    }

    private fun jsonEncode(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun getApiKey(): String {
        return System.getenv("OPENAI_API_KEY") ?: ""
    }

    data class ExtractedLeadData(
        val name: String,
        val phone: String,
        val email: String,
        val company: String,
        val productRequirement: String,
        val orderDetails: String,
        val inquiryDetails: String,
        val clientType: String
    )

    data class KeywordSuggestion(
        val keyword: String,
        val volume: Int,
        val competition: String,
        val difficulty: Int
    )

    data class SocialMediaContent(
        val caption: String,
        val hashtags: List<String>,
        val imageDescription: String
    )

    data class CompetitorInsights(
        val competitors: List<String>,
        val strategies: List<String>,
        val gaps: List<String>,
        val recommendations: List<String>
    )

    private fun parseExtractedLead(raw: String, fallbackName: String, fallbackEmail: String): ExtractedLeadData {
        return try {
            val name = extractJsonValue(raw, "name").ifEmpty { fallbackName }
            ExtractedLeadData(
                name = name,
                phone = extractJsonValue(raw, "phone"),
                email = extractJsonValue(raw, "email").ifEmpty { fallbackEmail },
                company = extractJsonValue(raw, "company"),
                productRequirement = extractJsonValue(raw, "productRequirement"),
                orderDetails = extractJsonValue(raw, "orderDetails"),
                inquiryDetails = extractJsonValue(raw, "inquiryDetails"),
                clientType = extractJsonValue(raw, "clientType")
            )
        } catch (e: Exception) {
            ExtractedLeadData(fallbackName, "", fallbackEmail, "", "", "", "", "OTHER")
        }
    }

    private fun extractJsonValue(json: String, key: String): String {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]*)\""
        return Regex(pattern).find(json)?.groupValues?.getOrNull(1) ?: ""
    }

    private fun parseKeywordSuggestions(raw: String): List<KeywordSuggestion> {
        return raw.lines().filter { it.matches(Regex("""^\d+\..*""")) }
            .mapIndexed { index, line ->
                KeywordSuggestion(
                    keyword = line.replace(Regex("""^\d+\.\s*"""), ""),
                    volume = (1000 - index * 50).coerceAtLeast(50),
                    competition = if (index < 5) "HIGH" else if (index < 10) "MEDIUM" else "LOW",
                    difficulty = (100 - index * 5).coerceAtLeast(10)
                )
            }
    }

    private fun parseSocialMediaContent(raw: String): SocialMediaContent {
        return SocialMediaContent(
            caption = raw,
            hashtags = extractHashtags(raw),
            imageDescription = "Product showcase image"
        )
    }

    private fun extractHashtags(text: String): List<String> {
        return Regex("#\\w+").findAll(text).map { it.value }.toList()
    }

    private fun parseCompetitorInsights(raw: String): CompetitorInsights {
        return CompetitorInsights(
            competitors = raw.lines().filter { it.contains("competitor", ignoreCase = true) },
            strategies = raw.lines().filter { it.contains("strateg", ignoreCase = true) },
            gaps = raw.lines().filter { it.contains("gap", ignoreCase = true) },
            recommendations = raw.lines().filter { it.contains("recommend", ignoreCase = true) }
        )
    }
}
