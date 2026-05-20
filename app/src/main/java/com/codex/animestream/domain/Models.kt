package com.codex.animestream.domain

data class Anime(
    val id: String,
    val title: String,
    val synopsis: String,
    val posterUrl: String,
    val bannerUrl: String,
    val genres: List<String>,
    val rating: Double?,
    val studio: String?,
    val status: String?,
    val year: Int?,
)

data class Episode(
    val id: String,
    val animeId: String,
    val number: Double,
    val title: String,
    val thumbnailUrl: String?,
    val durationMs: Long?,
)

data class Character(
    val id: String,
    val name: String,
    val role: String,
    val imageUrl: String?,
)

data class AnimeDetails(
    val anime: Anime,
    val episodes: List<Episode>,
    val characters: List<Character>,
    val related: List<Anime>,
)

data class StreamSource(
    val url: String,
    val type: StreamType,
    val quality: Quality,
    val headers: Map<String, String> = emptyMap(),
)

data class SubtitleTrack(
    val label: String,
    val language: String,
    val url: String,
    val mimeType: String = "application/x-subrip",
)

data class AudioTrack(
    val id: String,
    val label: String,
    val language: String,
)

data class SkipMarker(
    val kind: SkipKind,
    val startMs: Long,
    val endMs: Long,
)

data class PlaybackBundle(
    val episode: Episode,
    val sources: List<StreamSource>,
    val subtitles: List<SubtitleTrack>,
    val audioTracks: List<AudioTrack>,
    val skipMarkers: List<SkipMarker>,
    val nextEpisodeId: String?,
)

enum class StreamType { Hls, Mp4 }
enum class Quality { Auto, Q360, Q480, Q720, Q1080 }
enum class SkipKind { Intro, Outro }

data class SearchFilters(
    val genre: String? = null,
    val year: Int? = null,
    val status: String? = null,
    val minimumRating: Double? = null,
    val language: String? = null,
    val season: String? = null,
)

data class HomeSections(
    val trending: List<Anime>,
    val popular: List<Anime>,
    val latestEpisodes: List<Anime>,
    val continueWatching: List<Anime>,
    val seasonal: List<Anime>,
    val recommended: List<Anime>,
)
