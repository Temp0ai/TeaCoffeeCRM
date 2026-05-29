package com.teacoffee.crm.data.remote.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface GmbApiService {

    @GET("v1/accounts/{accountId}/locations")
    suspend fun getLocations(
        @Path("accountId") accountId: String
    ): LocationsResponse

    @GET("v1/{locationId}/reviews")
    suspend fun getReviews(
        @Path("locationId") locationId: String
    ): ReviewsResponse

    @POST("v1/{locationId}/localPosts")
    suspend fun createPost(
        @Path("locationId") locationId: String,
        @Body post: LocalPost
    ): LocalPost

    data class LocationsResponse(
        val locations: List<Location>? = null
    )

    data class Location(
        val name: String = "",
        val locationName: String = "",
        val address: Address? = null,
        val phoneNumbers: PhoneNumbers? = null,
        val websiteUrl: String = "",
        val regularHours: RegularHours? = null
    )

    data class Address(
        val addressLines: List<String>? = null,
        val locality: String = "",
        val administrativeArea: String = "",
        val postalCode: String = "",
        val countryCode: String = ""
    )

    data class PhoneNumbers(
        val primaryPhone: String = ""
    )

    data class RegularHours(
        val periods: List<Period>? = null
    )

    data class Period(
        val openDay: String = "",
        val openTime: String = "",
        val closeDay: String = "",
        val closeTime: String = ""
    )

    data class ReviewsResponse(
        val reviews: List<Review>? = null
    )

    data class Review(
        val name: String = "",
        val starRating: String = "",
        val comment: String = "",
        val createTime: String = "",
        val reviewer: Reviewer? = null
    )

    data class Reviewer(
        val displayName: String = ""
    )

    data class LocalPost(
        val summary: String = "",
        val event: Event? = null,
        val offer: Offer? = null,
        val alertType: String = "",
        val callToAction: CallToAction? = null
    )

    data class Event(
        val title: String = "",
        val startDate: String = "",
        val endDate: String = ""
    )

    data class Offer(
        val couponCode: String = "",
        val redeemOnlineUrl: String = "",
        val termsConditions: String = ""
    )

    data class CallToAction(
        val actionType: String = "",
        val url: String = ""
    )

    companion object {
        fun create(accessToken: String): GmbApiService {
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
                .baseUrl("https://mybusiness.googleapis.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GmbApiService::class.java)
        }
    }
}
