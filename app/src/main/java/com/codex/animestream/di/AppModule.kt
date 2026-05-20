package com.codex.animestream.di

import android.content.Context
import androidx.room.Room
import com.codex.animestream.BuildConfig
import com.codex.animestream.data.AppDatabase
import com.codex.animestream.data.LicensedAnimeProviderApi
import com.codex.animestream.data.LibraryDao
import com.codex.animestream.data.ProgressDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun json(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun okHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .apply {
                    if (BuildConfig.PROVIDER_API_KEY.isNotBlank()) {
                        if (BuildConfig.RAPIDAPI_HOST.isNotBlank()) {
                            header("X-RapidAPI-Key", BuildConfig.PROVIDER_API_KEY)
                            header("X-RapidAPI-Host", BuildConfig.RAPIDAPI_HOST)
                        } else {
                            header("Authorization", "Bearer ${BuildConfig.PROVIDER_API_KEY}")
                        }
                    }
                }
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    @Provides
    @Singleton
    fun providerApi(client: OkHttpClient, json: Json): LicensedAnimeProviderApi {
        if (BuildConfig.PROVIDER_BASE_URL.isBlank()) return ProviderConfigurationRequiredApi
        return Retrofit.Builder()
            .baseUrl(BuildConfig.PROVIDER_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(LicensedAnimeProviderApi::class.java)
    }

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "anime-stream.db",
    ).build()

    @Provides
    fun progressDao(db: AppDatabase): ProgressDao = db.progressDao()

    @Provides
    fun libraryDao(db: AppDatabase): LibraryDao = db.libraryDao()
}

private object ProviderConfigurationRequiredApi : LicensedAnimeProviderApi {
    private fun missing(): Nothing = error("Set PROVIDER_BASE_URL and, for RapidAPI, RAPIDAPI_HOST in gradle.properties.")
    override suspend fun home() = missing()
    override suspend fun details(id: String) = missing()
    override suspend fun search(query: String, genre: String?, year: Int?, status: String?, minimumRating: Double?, language: String?, season: String?) = missing()
    override suspend fun playback(episodeId: String) = missing()
}
