package com.teacoffee.crm.util

import android.content.Context
import com.teacoffee.crm.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class DatabaseInitializer {

    companion object {
        private const val PREFS_NAME = "db_init"
        private const val KEY_INITIALIZED = "excel_seeded"

        suspend fun initialize(context: Context, database: AppDatabase) {
            withContext(Dispatchers.IO) {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                if (prefs.getBoolean(KEY_INITIALIZED, false)) return@withContext

                SeedData.seedIfEmpty(database)

                val leadDao = database.leadDao()
                val existingCount = leadDao.getLeadCount().first()
                if (existingCount == 0) {
                    try {
                        val inputStream = context.assets.open("IndiaMART_Leads_ALL.xlsx")
                        inputStream.use { stream ->
                            val importer = ExcelImporter()
                            val result = importer.importFromStream(stream)
                            if (result.leads.isNotEmpty()) {
                                leadDao.insertLeads(result.leads)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DB_INIT", "Failed to import Excel: ${e.message}")
                    }
                }

                prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
            }
        }
    }
}
