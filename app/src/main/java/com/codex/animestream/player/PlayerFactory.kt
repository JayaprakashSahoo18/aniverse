package com.codex.animestream.player

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.codex.animestream.domain.*

@UnstableApi
object PlayerFactory {
    fun create(context: Context, bundle: PlaybackBundle, quality: Quality): ExoPlayer {
        val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(buildUponParameters().setMaxVideoSizeForQuality(quality).build())
        }
        val player = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build()
        val source = bundle.sources.firstOrNull { it.quality == quality } ?: bundle.sources.first()
        val mediaItem = MediaItem.Builder()
            .setUri(source.url)
            .setMimeType(if (source.type == StreamType.Hls) MimeTypes.APPLICATION_M3U8 else MimeTypes.VIDEO_MP4)
            .setSubtitleConfigurations(
                bundle.subtitles.map {
                    MediaItem.SubtitleConfiguration.Builder(android.net.Uri.parse(it.url))
                        .setMimeType(it.mimeType)
                        .setLanguage(it.language)
                        .setLabel(it.label)
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .build()
                }
            )
            .build()
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(source.headers)
            .setAllowCrossProtocolRedirects(true)
        val mediaSource = if (source.type == StreamType.Hls) {
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        } else {
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        }
        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true
        return player
    }

    fun ExoPlayer.setPlaybackSpeed(speed: Float) {
        playbackParameters = PlaybackParameters(speed)
    }

    private fun DefaultTrackSelector.Parameters.Builder.setMaxVideoSizeForQuality(quality: Quality): DefaultTrackSelector.Parameters.Builder {
        return when (quality) {
            Quality.Q360 -> setMaxVideoSize(640, 360)
            Quality.Q480 -> setMaxVideoSize(854, 480)
            Quality.Q720 -> setMaxVideoSize(1280, 720)
            Quality.Q1080 -> setMaxVideoSize(1920, 1080)
            Quality.Auto -> clearVideoSizeConstraints()
        }
    }
}
