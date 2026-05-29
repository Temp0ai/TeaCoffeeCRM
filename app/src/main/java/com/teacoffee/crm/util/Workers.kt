package com.teacoffee.crm.util

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.teacoffee.crm.data.local.AppDatabase
import com.teacoffee.crm.data.local.entity.LeadEntity
import com.teacoffee.crm.data.remote.api.GmailApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class GmailSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val accessToken = inputData.getString(KEY_ACCESS_TOKEN)
                ?: return@withContext Result.failure()

            val db = AppDatabase.getDatabase(applicationContext)
            val gmailDao = db.gmailMessageDao()
            val leadDao = db.leadDao()
            val gmailParser = GmailParser()
            val aiEngine = AiEngine()

            val api = GmailApiService.create(accessToken)

            val query = inputData.getString(KEY_QUERY) ?: "subject:(inquiry OR quote OR order OR price OR product OR tea OR coffee)"
            val maxResults = inputData.getInt(KEY_MAX_RESULTS, 50)

            val listResponse = api.listMessages(query = "in:inbox $query", maxResults = maxResults)

            listResponse.messages?.forEach { msgRef ->
                try {
                    val existingMsg = gmailDao.getMessageById(msgRef.id)
                    if (existingMsg != null) return@forEach

                    val detail = api.getMessage(msgRef.id)
                    val parsed = gmailParser.parseMessageDetail(detail)

                    gmailDao.insertMessage(
                        com.teacoffee.crm.data.local.entity.GmailMessageEntity(
                            id = parsed.id,
                            threadId = parsed.threadId,
                            fromAddress = parsed.from,
                            fromName = parsed.fromName,
                            subject = parsed.subject,
                            body = parsed.body,
                            receivedAt = parsed.receivedAt
                        )
                    )

                    val extracted = aiEngine.extractLeadFromEmail(
                        parsed.subject, parsed.body, parsed.fromName, parsed.from
                    )

                    val existingLead = leadDao.getLeadByEmail(parsed.from)
                    val leadId = if (existingLead != null) {
                        leadDao.updateLead(
                            existingLead.copy(
                                productRequirement = extracted.productRequirement.ifEmpty { existingLead.productRequirement },
                                orderDetails = extracted.orderDetails.ifEmpty { existingLead.orderDetails },
                                inquiryDetails = extracted.inquiryDetails.ifEmpty { existingLead.inquiryDetails },
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                        existingLead.id
                    } else {
                        leadDao.insertLead(
                            LeadEntity(
                                name = extracted.name.ifEmpty { parsed.fromName.ifEmpty { parsed.from.substringBefore("@") } },
                                phone = extracted.phone,
                                email = parsed.from,
                                company = extracted.company,
                                productRequirement = extracted.productRequirement,
                                orderDetails = extracted.orderDetails,
                                inquiryDetails = extracted.inquiryDetails,
                                clientType = extracted.clientType,
                                source = "GMAIL"
                            )
                        )
                    }

                    gmailDao.markAsExtracted(parsed.id, leadId)

                } catch (e: Exception) {
                    Log.e("GmailSyncWorker", "Error processing message ${msgRef.id}: ${e.message}")
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("GmailSyncWorker", "Sync failed: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_QUERY = "query"
        const val KEY_MAX_RESULTS = "max_results"

        fun createRequest(
            accessToken: String,
            query: String = "subject:(inquiry OR quote OR order OR price OR product OR tea OR coffee)",
            maxResults: Int = 50
        ): OneTimeWorkRequest {
            val inputData = workDataOf(
                KEY_ACCESS_TOKEN to accessToken,
                KEY_QUERY to query,
                KEY_MAX_RESULTS to maxResults
            )

            return OneTimeWorkRequestBuilder<GmailSyncWorker>()
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30, TimeUnit.SECONDS
                )
                .build()
        }
    }
}

@HiltWorker
class WhatsAppSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val accessToken = inputData.getString(KEY_ACCESS_TOKEN) ?: return@withContext Result.failure()
            val phoneNumberId = inputData.getString(KEY_PHONE_NUMBER_ID) ?: return@withContext Result.failure()

            val api = WhatsAppApiService.create(accessToken)
            val templatesResponse = api.getMessageTemplates(phoneNumberId)

            Result.success()
        } catch (e: Exception) {
            Log.e("WhatsAppSyncWorker", "Sync failed: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_PHONE_NUMBER_ID = "phone_number_id"

        fun createRequest(accessToken: String, phoneNumberId: String): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<WhatsAppSyncWorker>()
                .setInputData(
                    workDataOf(
                        KEY_ACCESS_TOKEN to accessToken,
                        KEY_PHONE_NUMBER_ID to phoneNumberId
                    )
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        }
    }
}
