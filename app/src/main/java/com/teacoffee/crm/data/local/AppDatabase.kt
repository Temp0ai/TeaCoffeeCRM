package com.teacoffee.crm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.teacoffee.crm.data.local.dao.*
import com.teacoffee.crm.data.local.entity.*

@Database(
    entities = [
        LeadEntity::class,
        CategoryEntity::class,
        MessageEntity::class,
        CampaignEntity::class,
        GmailMessageEntity::class,
        ProductEntity::class,
        SeoKeywordEntity::class,
        ContentTemplateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun leadDao(): LeadDao
    abstract fun categoryDao(): CategoryDao
    abstract fun messageDao(): MessageDao
    abstract fun campaignDao(): CampaignDao
    abstract fun gmailMessageDao(): GmailMessageDao
    abstract fun productDao(): ProductDao
    abstract fun seoKeywordDao(): SeoKeywordDao
    abstract fun contentTemplateDao(): ContentTemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tea_coffee_crm_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
