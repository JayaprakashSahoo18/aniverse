package com.codex.animestream.data

import kotlinx.serialization.Serializable

@Serializable
data class AnimeDto(
    val id: String,
    val title: String,
    val synopsis: String = "",
    val posterUrl: String = "",
    val bannerUrl: String = "",
    val genres: List<String> = emptyList(),
    val rating: Double? = null,
    val studio: String? = null,
    val status: String? = null,
    val year: Int? = null,
)

@Serializable
data class EpisodeDto(
    val id: String,
    val animeId: String,
    val number: Double,
    val title: String,
    val thumbnailUrl: String? = null,
    val durationMs: Long? = null,
)

@Serializable
data class CharacterDto(val id: String, val name: String, val role: String, val imageUrl: String? = null)

@Serializable
data class AnimeDetailsDto(
    val anime: AnimeDto,
    val episodes: List<EpisodeDto> = emptyList(),
    val characters: List<CharacterDto> = emptyList(),
    val related: List<AnimeDto> = emptyList(),
)

@Serializable
data class StreamSourceDto(
    val url: String,
    val type: String,
    val quality: String,
    val headers: Map<String, String> = emptyMap(),
)

@Serializable
data class SubtitleTrackDto(val label: String, val language: String, val url: String, val mimeType: String = "application/x-subrip")

@Serializable
data class AudioTrackDto(val id: String, val label: String, val language: String)

@Serializable
data class SkipMarkerDto(val kind: String, val startMs: Long, val endMs: Long)

@Serializable
data class PlaybackDto(
    val episode: EpisodeDto,
    val sources: List<StreamSourceDto>,
    val subtitles: List<SubtitleTrackDto> = emptyList(),
    val audioTracks: List<AudioTrackDto> = emptyList(),
    val skipMarkers: List<SkipMarkerDto> = emptyList(),
    val nextEpisodeId: String? = null,
)

@Serializable
data class HomeDto(
    val trending: List<AnimeDto> = emptyList(),
    val popular: List<AnimeDto> = emptyList(),
    val latestEpisodes: List<AnimeDto> = emptyList(),
    val seasonal: List<AnimeDto> = emptyList(),
    val recommended: List<AnimeDto> = emptyList(),
)
