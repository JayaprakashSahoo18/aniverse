package com.codex.animestream.data

import com.codex.animestream.domain.*

fun AnimeDto.toDomain() = Anime(id, title, synopsis, posterUrl, bannerUrl, genres, rating, studio, status, year)
fun EpisodeDto.toDomain() = Episode(id, animeId, number, title, thumbnailUrl, durationMs)
fun CharacterDto.toDomain() = Character(id, name, role, imageUrl)

fun AnimeDetailsDto.toDomain() = AnimeDetails(
    anime = anime.toDomain(),
    episodes = episodes.map { it.toDomain() },
    characters = characters.map { it.toDomain() },
    related = related.map { it.toDomain() },
)

fun StreamSourceDto.toDomain() = StreamSource(
    url = url,
    type = if (type.equals("hls", ignoreCase = true)) StreamType.Hls else StreamType.Mp4,
    quality = when (quality.lowercase()) {
        "360p" -> Quality.Q360
        "480p" -> Quality.Q480
        "720p" -> Quality.Q720
        "1080p" -> Quality.Q1080
        else -> Quality.Auto
    },
    headers = headers,
)

fun SubtitleTrackDto.toDomain() = SubtitleTrack(label, language, url, mimeType)
fun AudioTrackDto.toDomain() = AudioTrack(id, label, language)
fun SkipMarkerDto.toDomain() = SkipMarker(
    kind = if (kind.equals("outro", ignoreCase = true)) SkipKind.Outro else SkipKind.Intro,
    startMs = startMs,
    endMs = endMs,
)

fun PlaybackDto.toDomain() = PlaybackBundle(
    episode = episode.toDomain(),
    sources = sources.map { it.toDomain() },
    subtitles = subtitles.map { it.toDomain() },
    audioTracks = audioTracks.map { it.toDomain() },
    skipMarkers = skipMarkers.map { it.toDomain() },
    nextEpisodeId = nextEpisodeId,
)
