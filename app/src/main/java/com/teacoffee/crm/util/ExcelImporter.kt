package com.teacoffee.crm.util

import android.content.Context
import android.net.Uri
import com.teacoffee.crm.data.local.entity.LeadEntity
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Row
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelImporter @Inject constructor() {

    data class ImportResult(
        val leads: List<LeadEntity> = emptyList(),
        val errors: List<String> = emptyList(),
        val totalRows: Int = 0,
        val importedRows: Int = 0
    )

    suspend fun importFromUri(context: Context, uri: Uri): ImportResult {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return ImportResult(errors = listOf("Cannot open file"))

            inputStream.use { stream ->
                importFromStream(stream)
            }
        } catch (e: Exception) {
            ImportResult(errors = listOf("Error reading file: ${e.message}"))
        }
    }

    suspend fun importFromAsset(context: Context, assetPath: String = "IndiaMART_Leads_ALL.xlsx"): ImportResult {
        return try {
            val inputStream = context.assets.open(assetPath)
            inputStream.use { stream ->
                importFromStream(stream)
            }
        } catch (e: Exception) {
            ImportResult(errors = listOf("Error reading asset: ${e.message}"))
        }
    }

    fun importFromStream(stream: InputStream): ImportResult {
        val errors = mutableListOf<String>()
        val leads = mutableListOf<LeadEntity>()

        try {
            val workbook = WorkbookFactory.create(stream)
            val sheet = workbook.getSheetAt(0)
            val headerRow = sheet.getRow(0) ?: return ImportResult(errors = listOf("Empty spreadsheet"))

            val columnMap = mapColumns(headerRow)

            val totalRows = sheet.lastRowNum
            var importedRows = 0

            for (i in 1..totalRows) {
                val row = sheet.getRow(i) ?: continue
                try {
                    val lead = parseRow(row, columnMap)
                    if (lead.name.isNotBlank() || lead.email.isNotBlank() || lead.phone.isNotBlank()) {
                        leads.add(lead)
                        importedRows++
                    } else {
                        errors.add("Row $i: No identifiable lead data")
                    }
                } catch (e: Exception) {
                    errors.add("Row $i: ${e.message}")
                }
            }

            workbook.close()
            return ImportResult(leads, errors, totalRows, importedRows)
        } catch (e: Exception) {
            return ImportResult(errors = listOf("Failed to parse Excel: ${e.message}"))
        }
    }

    private fun mapColumns(headerRow: Row): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        val headerMap = mapOf(
            "name" to listOf("name", "lead name", "customer name", "client name", "full name", "contact person"),
            "phone" to listOf("phone", "mobile", "contact number", "phone number", "telephone", "mobile number", "whatsapp"),
            "email" to listOf("email", "e-mail", "email id", "email address", "mail"),
            "company" to listOf("company", "organization", "business", "firm", "company name", "brand"),
            "designation" to listOf("designation", "title", "role", "position", "job title"),
            "productRequirement" to listOf("product", "product requirement", "requirement", "product interest", "interested in", "item"),
            "orderDetails" to listOf("order", "order details", "order qty", "quantity", "order quantity"),
            "inquiryDetails" to listOf("inquiry", "inquiry details", "message", "notes", "remarks", "comments", "description"),
            "clientType" to listOf("client type", "customer type", "type", "category", "segment", "business type"),
            "status" to listOf("status", "lead status", "stage"),
            "source" to listOf("source", "lead source", "origin")
        )

        for (i in 0..headerRow.lastCellNum - 1) {
            val cell = headerRow.getCell(i) ?: continue
            val headerValue = cell.stringCellValue.trim().lowercase()

            for ((key, aliases) in headerMap) {
                if (headerValue in aliases && !map.containsKey(key)) {
                    map[key] = i
                    break
                }
            }
        }

        return map
    }

    private fun parseRow(row: Row, columnMap: Map<String, Int>): LeadEntity {
        return LeadEntity(
            name = getCellStringValue(row, columnMap["name"]) ?: "",
            phone = getCellStringValue(row, columnMap["phone"]) ?: "",
            email = getCellStringValue(row, columnMap["email"]) ?: "",
            company = getCellStringValue(row, columnMap["company"]) ?: "",
            designation = getCellStringValue(row, columnMap["designation"]) ?: "",
            productRequirement = getCellStringValue(row, columnMap["productRequirement"]) ?: "",
            orderDetails = getCellStringValue(row, columnMap["orderDetails"]) ?: "",
            inquiryDetails = getCellStringValue(row, columnMap["inquiryDetails"]) ?: "",
            clientType = normalizeClientType(getCellStringValue(row, columnMap["clientType"]) ?: ""),
            status = normalizeStatus(getCellStringValue(row, columnMap["status"]) ?: "NEW"),
            source = getCellStringValue(row, columnMap["source"]) ?: "EXCEL"
        )
    }

    private fun getCellStringValue(row: Row, colIndex: Int?): String? {
        if (colIndex == null) return null
        val cell = row.getCell(colIndex) ?: return null
        return when (cell.cellType) {
            org.apache.poi.ss.usermodel.CellType.STRING -> cell.stringCellValue.trim()
            org.apache.poi.ss.usermodel.CellType.NUMERIC -> {
                val value = cell.numericCellValue
                if (value == value.toLong().toDouble()) value.toLong().toString()
                else value.toString()
            }
            org.apache.poi.ss.usermodel.CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> null
        }
    }

    private fun normalizeClientType(type: String): String {
        return when (type.lowercase().trim()) {
            "society", "soc", "housing society" -> "SOCIETY"
            "cafe", "café", "coffee shop", "cafeteria" -> "CAFE"
            "restaurant", "restro", "hotel", "dining" -> "RESTAURANT"
            "office", "corporate", "company", "business" -> "OFFICE"
            "manufacturer", "manufacturing", "production" -> "MANUFACTURER"
            "retailer", "retail", "shop", "store", "distributor" -> "RETAILER"
            else -> type.uppercase().takeIf { it.isNotBlank() } ?: "OTHER"
        }
    }

    private fun normalizeStatus(status: String): String {
        return when (status.lowercase().trim()) {
            "new", "fresh" -> "NEW"
            "contacted", "called", "reached" -> "CONTACTED"
            "follow up", "follow-up", "followup" -> "FOLLOW_UP"
            "converted", "won", "closed won", "customer" -> "CONVERTED"
            "closed", "lost", "closed lost", "not interested" -> "CLOSED"
            else -> "NEW"
        }
    }
}
