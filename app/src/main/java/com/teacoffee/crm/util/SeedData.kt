package com.teacoffee.crm.util

import com.teacoffee.crm.data.local.AppDatabase
import com.teacoffee.crm.data.local.entity.CategoryEntity
import com.teacoffee.crm.data.local.entity.ContentTemplateEntity
import com.teacoffee.crm.data.local.entity.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SeedData {

    companion object {
        suspend fun seedIfEmpty(database: AppDatabase) {
            withContext(Dispatchers.IO) {
                val categoryDao = database.categoryDao()
                val categories = categoryDao.getAllCategories()
                if (categories.value.isNullOrEmpty()) {
                    seedCategories(database)
                    seedProducts(database)
                    seedTemplates(database)
                }
            }
        }

        private suspend fun seedCategories(database: AppDatabase) {
            val dao = database.categoryDao()
            val categories = listOf(
                CategoryEntity(name = "Tea Premix", type = "PRODUCT", description = "Tea premix products", color = "#FF4CAF50"),
                CategoryEntity(name = "Coffee Premix", type = "PRODUCT", description = "Coffee premix products", color = "#FF795548"),
                CategoryEntity(name = "Nescafe Premix", type = "PRODUCT", description = "Nescafe premix products", color = "#FFFF5722"),
                CategoryEntity(name = "Tea Machine", type = "EQUIPMENT", description = "Tea vending machines", color = "#FF2196F3"),
                CategoryEntity(name = "Coffee Machine", type = "EQUIPMENT", description = "Coffee vending machines", color = "#FF9C27B0"),
                CategoryEntity(name = "Nescafe Machine", type = "EQUIPMENT", description = "Nescafe vending machines", color = "#FFFF9800"),
                CategoryEntity(name = "Society", type = "CLIENT_TYPE", description = "Housing societies", color = "#FF00BCD4"),
                CategoryEntity(name = "Cafe", type = "CLIENT_TYPE", description = "Cafes and coffee shops", color = "#FF8BC34A"),
                CategoryEntity(name = "Restaurant", type = "CLIENT_TYPE", description = "Restaurants and hotels", color = "#FFFFC107"),
                CategoryEntity(name = "Office", type = "CLIENT_TYPE", description = "Corporate offices", color = "#FF607D8B"),
                CategoryEntity(name = "Manufacturer", type = "CLIENT_TYPE", description = "Tea/coffee manufacturers", color = "#FFE91E63"),
                CategoryEntity(name = "Retailer", type = "CLIENT_TYPE", description = "Retail shops and distributors", color = "#FFCDDC39"),
            )
            dao.insertCategories(categories)
        }

        private suspend fun seedProducts(database: AppDatabase) {
            val dao = database.productDao()
            val products = listOf(
                ProductEntity(name = "Classic Tea Premix", description = "Premium classic tea premix with rich flavor", price = 450.0, category = "TEA_PREMIX"),
                ProductEntity(name = "Masala Tea Premix", description = "Spiced masala tea premix", price = 520.0, category = "TEA_PREMIX"),
                ProductEntity(name = "Green Tea Premix", description = "Healthy green tea premix", price = 580.0, category = "TEA_PREMIX"),
                ProductEntity(name = "Lemon Tea Premix", description = "Refreshing lemon tea premix", price = 490.0, category = "TEA_PREMIX"),
                ProductEntity(name = "South Filter Coffee Premix", description = "Authentic South Indian filter coffee premix", price = 650.0, category = "COFFEE_PREMIX"),
                ProductEntity(name = "Cold Coffee Premix", description = "Instant cold coffee premix", price = 550.0, category = "COFFEE_PREMIX"),
                ProductEntity(name = "Nescafe Premium Premix", description = "Premium Nescafe-based coffee premix", price = 700.0, category = "NESCAFE_PREMIX"),
                ProductEntity(name = "Tea Vending Machine", description = "Automatic tea vending machine (3 variants)", price = 25000.0, category = "MACHINE"),
                ProductEntity(name = "Coffee Vending Machine", description = "Automatic coffee vending machine", price = 35000.0, category = "MACHINE"),
                ProductEntity(name = "Combo Vending Machine", description = "Tea & Coffee combo vending machine", price = 45000.0, category = "MACHINE"),
            )
            dao.insertProducts(products)
        }

        private suspend fun seedTemplates(database: AppDatabase) {
            val dao = database.contentTemplateDao()
            val templates = listOf(
                ContentTemplateEntity(
                    title = "Initial Follow-Up",
                    body = "Dear {name},\n\nThank you for your inquiry regarding {product}. We specialize in premium tea and coffee solutions.\n\nWould you like to:\n1. Receive our latest product catalog\n2. Get a customized quotation\n3. Schedule a free product demo\n\nPlease let us know your preference!\n\nBest regards,\nTeaCoffee Team",
                    type = "FOLLOW_UP",
                    platform = "WHATSAPP",
                    category = "GENERAL"
                ),
                ContentTemplateEntity(
                    title = "Product Promotion - Tea",
                    body = "☕ Upgrade your tea experience with our premium {product}!\n\n✅ Rich authentic taste\n✅ Easy to prepare\n✅ Bulk discounts available\n✅ Free sample on first order\n\nOrder now and get 10% OFF on your first bulk purchase!\n\n📞 Contact us today!",
                    type = "PROMOTION",
                    platform = "WHATSAPP",
                    category = "TEA"
                ),
                ContentTemplateEntity(
                    title = "Product Promotion - Coffee",
                    body = "☕ Love great coffee? Try our {product}!\n\n✨ Barista-quality taste\n✨ Instant preparation\n✨ Perfect for offices & cafes\n✨ Competitive bulk pricing\n\nSpecial launch offer: 15% discount on first order!\n\n📞 Call/WhatsApp us now!",
                    type = "PROMOTION",
                    platform = "WHATSAPP",
                    category = "COFFEE"
                ),
                ContentTemplateEntity(
                    title = "Machine Demo Offer",
                    body = "🏭 Transform your beverage service with our {product}!\n\n✔ Automatic operation\n✔ Consistent quality every cup\n✔ Low maintenance\n✔ Perfect for high-volume locations\n\nBook a FREE demo today and see the difference!\n\n📞 Schedule your demo now!",
                    type = "PRODUCT_UPDATE",
                    platform = "WHATSAPP",
                    category = "MACHINE"
                ),
                ContentTemplateEntity(
                    title = "Festival Greeting",
                    body = "🎉 Wishing you and your team a wonderful {festival}! \n\nMay this festive season bring joy, prosperity, and success to your business.\n\nAs a special festive offer, enjoy {discount}% off on all our products!\n\nWarm regards,\nTeaCoffee Team",
                    type = "GREETING",
                    platform = "WHATSAPP",
                    category = "GENERAL"
                ),
                ContentTemplateEntity(
                    title = "Instagram - Product Showcase",
                    body = "☕ Elevate your coffee game with our premium premix!\n\nSwipe left to see our range ➡️\n\nFrom classic filter coffee to trendy cold coffee - we have it all!\n\nPerfect for: Cafes ☕ | Offices 🏢 | Events 🎉 | Home 🏠\n\n{hashtags}\n\n#TeaCoffee #PremiumBeverages #CoffeeLovers #TeaLovers #VendingMachine #BulkSupply",
                    type = "SOCIAL_MEDIA",
                    platform = "INSTAGRAM",
                    category = "COFFEE"
                ),
                ContentTemplateEntity(
                    title = "Re-engagement Follow-Up",
                    body = "Hi {name},\n\nWe haven't heard from you in a while! Just checking if you're still interested in {product}.\n\nWe now have:\n🆕 New flavors available\n💰 Special pricing for bulk orders\n🚚 Free delivery on orders above ₹5000\n\nReply to this message to get an updated quote!\n\nBest,\nTeaCoffee Team",
                    type = "FOLLOW_UP",
                    platform = "WHATSAPP",
                    category = "GENERAL"
                ),
            )
            dao.insertTemplates(templates)
        }
    }
}
