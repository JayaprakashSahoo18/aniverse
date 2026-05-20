# AnimeStream

Native Android anime streaming app inspired by Dantotsu-style navigation and media UX, implemented with Kotlin, Jetpack Compose, MVVM, Hilt, Retrofit, Room, Coroutines/Flow, Coil, and Media3/ExoPlayer.

This project intentionally does not include unauthorized scrapers, pirate sources, demo videos, YouTube embeds, sample MP4s, or fake stream responses. To run with real playback, configure a licensed provider backend:

```properties
PROVIDER_BASE_URL=https://your-licensed-provider.example/
PROVIDER_API_KEY=optional_api_key
RAPIDAPI_HOST=
```

For RapidAPI-backed providers, set `PROVIDER_BASE_URL` to the API base URL and `RAPIDAPI_HOST` to the exact host shown in RapidAPI. The app will send `X-RapidAPI-Key` and `X-RapidAPI-Host` headers. For non-RapidAPI providers, leave `RAPIDAPI_HOST` blank and the app will send a bearer token.

The provider must return real playable HLS or MP4 URLs from `GET /v1/episodes/{episodeId}/playback`. The app supports HLS, MP4, remote `.srt` subtitle tracks, embedded subtitles surfaced by Media3, playback speed, quality selection, skip intro/outro markers, PiP, resume persistence, and auto next episode.

## Provider Contract

`GET /v1/home`

Returns sections: `trending`, `popular`, `latestEpisodes`, `seasonal`, `recommended`.

`GET /v1/anime/{id}`

Returns anime metadata, episodes, characters, and related anime.

`GET /v1/search?q=&genre=&year=&status=&rating=&language=&season=`

Returns matching anime for global search and filters.

`GET /v1/episodes/{episodeId}/playback`

Returns:

```json
{
  "episode": {
    "id": "episode-id",
    "animeId": "anime-id",
    "number": 1,
    "title": "Episode title",
    "durationMs": 1440000
  },
  "sources": [
    {
      "url": "https://cdn.example/anime/ep1/master.m3u8",
      "type": "hls",
      "quality": "auto",
      "headers": {}
    }
  ],
  "subtitles": [
    {
      "label": "English",
      "language": "en",
      "url": "https://cdn.example/subtitles/ep1.en.srt",
      "mimeType": "application/x-subrip"
    }
  ],
  "audioTracks": [
    {
      "id": "ja",
      "label": "Japanese",
      "language": "ja"
    }
  ],
  "skipMarkers": [
    {
      "kind": "intro",
      "startMs": 90000,
      "endMs": 180000
    }
  ],
  "nextEpisodeId": "episode-id-2"
}
```

## Included Architecture

- Clean-ish package split: `domain`, `data`, `di`, `player`, `ui`
- Retrofit provider API with bearer token support
- Room tables for progress/history and library entries
- Hilt injection from app startup through ViewModels
- Compose home, search, details, and player screens
- Media3 player setup for adaptive HLS/MP4 playback
- Lazy image loading via Coil

## Production Work Remaining

- Add your licensed auth/login backend endpoints.
- Add a remote progress-sync endpoint and call it from `AnimeRepository.saveProgress`.
- Add WorkManager jobs for offline downloads and new-episode notifications.
- Add DRM session handling if your licensed provider requires Widevine.
- Add instrumentation tests once a provider sandbox is available.
