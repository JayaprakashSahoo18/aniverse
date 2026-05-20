package com.codex.animestream.data

import com.codex.animestream.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnimeRepository @Inject constructor(
    private val api: LicensedAnimeProviderApi,
    private val progressDao: ProgressDao,
    private val libraryDao: LibraryDao,
) {
    suspend fun home(): HomeSections {
        val dto = api.home()
        return HomeSections(
            trending = dto.trending.map { it.toDomain() },
            popular = dto.popular.map { it.toDomain() },
            latestEpisodes = dto.latestEpisodes.map { it.toDomain() },
            continueWatching = emptyList(),
            seasonal = dto.seasonal.map { it.toDomain() },
            recommended = dto.recommended.map { it.toDomain() },
        )
    }

    suspend fun details(id: String): AnimeDetails = api.details(id).toDomain()

    suspend fun search(query: String, filters: SearchFilters): List<Anime> = api.search(
        query = query,
        genre = filters.genre,
        year = filters.year,
        status = filters.status,
        minimumRating = filters.minimumRating,
        language = filters.language,
        season = filters.season,
    ).map { it.toDomain() }

    suspend fun playback(episodeId: String): PlaybackBundle {
        val bundle = api.playback(episodeId).toDomain()
        require(bundle.sources.any { it.url.startsWith("https://") || it.url.startsWith("http://") }) {
            "Provider returned no playable HLS/MP4 stream."
        }
        return bundle
    }

    fun observeProgress(episodeId: String): Flow<ProgressEntity?> = progressDao.observe(episodeId)

    suspend fun saveProgress(episodeId: String, animeId: String, positionMs: Long, durationMs: Long) {
        progressDao.upsert(ProgressEntity(episodeId, animeId, positionMs, durationMs, System.currentTimeMillis()))
    }

    fun observeLibrary(kind: String): Flow<List<LibraryEntity>> = libraryDao.observeKind(kind)

    suspend fun toggleLibrary(anime: Anime, kind: String, enabled: Boolean) {
        if (enabled) {
            libraryDao.upsert(LibraryEntity(anime.id, anime.title, anime.posterUrl, kind, System.currentTimeMillis()))
        } else {
            libraryDao.remove(anime.id, kind)
        }
    }
}
