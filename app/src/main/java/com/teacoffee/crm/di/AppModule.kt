package com.teacoffee.crm.di

import android.content.Context
import com.teacoffee.crm.data.local.AppDatabase
import com.teacoffee.crm.data.local.dao.*
import com.teacoffee.crm.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideLeadDao(database: AppDatabase): LeadDao = database.leadDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()

    @Provides
    fun provideCampaignDao(database: AppDatabase): CampaignDao = database.campaignDao()

    @Provides
    fun provideGmailMessageDao(database: AppDatabase): GmailMessageDao = database.gmailMessageDao()

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    @Provides
    fun provideSeoKeywordDao(database: AppDatabase): SeoKeywordDao = database.seoKeywordDao()

    @Provides
    fun provideContentTemplateDao(database: AppDatabase): ContentTemplateDao = database.contentTemplateDao()

    @Provides
    @Singleton
    fun provideLeadRepository(leadDao: LeadDao): LeadRepository = LeadRepository(leadDao)

    @Provides
    @Singleton
    fun provideCategoryRepository(categoryDao: CategoryDao): CategoryRepository = CategoryRepository(categoryDao)

    @Provides
    @Singleton
    fun provideMessageRepository(messageDao: MessageDao): MessageRepository = MessageRepository(messageDao)

    @Provides
    @Singleton
    fun provideProductRepository(productDao: ProductDao): ProductRepository = ProductRepository(productDao)

    @Provides
    @Singleton
    fun provideCampaignRepository(campaignDao: CampaignDao): CampaignRepository = CampaignRepository(campaignDao)

    @Provides
    @Singleton
    fun provideContentTemplateRepository(contentTemplateDao: ContentTemplateDao): ContentTemplateRepository = ContentTemplateRepository(contentTemplateDao)

    @Provides
    @Singleton
    fun provideSeoKeywordRepository(seoKeywordDao: SeoKeywordDao): SeoKeywordRepository = SeoKeywordRepository(seoKeywordDao)
}
