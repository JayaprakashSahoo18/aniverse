package com.codex.animestream.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.codex.animestream.domain.PlaybackBundle
import com.codex.animestream.domain.Quality
import com.codex.animestream.domain.SkipKind
import com.codex.animestream.player.PlayerFactory
import com.codex.animestream.player.PlayerFactory.setPlaybackSpeed
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    onEnterPictureInPicture: () -> Unit,
    playNext: (String) -> Unit,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    when (val current = state) {
        LoadState.Loading -> LoadingBlock("Opening stream")
        is LoadState.Error -> ErrorBlock(current.message, onBack)
        is LoadState.Ready -> PlayerContent(current.value, viewModel, onBack, onEnterPictureInPicture, playNext)
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun PlayerContent(
    bundle: PlaybackBundle,
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
    onEnterPictureInPicture: () -> Unit,
    playNext: (String) -> Unit,
) {
    val context = LocalContext.current
    val progress by viewModel.progress.collectAsState()
    var quality by remember { mutableStateOf(Quality.Auto) }
    var speed by remember { mutableFloatStateOf(1f) }
    val player = remember(bundle.episode.id, quality) { PlayerFactory.create(context, bundle, quality) }
    var restored by remember(bundle.episode.id, quality) { mutableStateOf(false) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED && bundle.nextEpisodeId != null) {
                    playNext(bundle.nextEpisodeId)
                }
            }
        }
        player.addListener(listener)
        onDispose {
            viewModel.saveProgress(bundle, player.currentPosition, player.duration.coerceAtLeast(0))
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(player, bundle) {
        while (true) {
            delay(5_000)
            viewModel.saveProgress(bundle, player.currentPosition, player.duration.coerceAtLeast(0))
        }
    }

    LaunchedEffect(player, progress) {
        val saved = progress
        if (!restored && saved != null && saved.positionMs > 5_000) {
            restored = true
            player.seekTo(saved.positionMs)
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    this.player = player
                    useController = true
                    setShowSubtitleButton(true)
                    setShowFastForwardButton(true)
                    setShowRewindButton(true)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            modifier = Modifier.align(Alignment.TopStart).padding(top = 38.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onBack, shape = RoundedCornerShape(8.dp)) { Text("Back") }
            OutlinedButton(onClick = onEnterPictureInPicture, shape = RoundedCornerShape(8.dp)) { Text("PiP") }
            QualityMenu(quality) { quality = it }
            SpeedMenu(speed) {
                speed = it
                player.setPlaybackSpeed(it)
            }
        }
        SkipControls(
            bundle = bundle,
            position = player.currentPosition,
            seekTo = { player.seekTo(it) },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 92.dp),
        )
    }
}

@Composable
private fun QualityMenu(selected: Quality, onSelected: (Quality) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }, shape = RoundedCornerShape(8.dp)) { Text(selected.label) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Quality.entries.forEach {
                DropdownMenuItem(text = { Text(it.label) }, onClick = { expanded = false; onSelected(it) })
            }
        }
    }
}

@Composable
private fun SpeedMenu(selected: Float, onSelected: (Float) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }, shape = RoundedCornerShape(8.dp)) { Text("${selected}x") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach {
                DropdownMenuItem(text = { Text("${it}x") }, onClick = { expanded = false; onSelected(it) })
            }
        }
    }
}

@Composable
private fun SkipControls(bundle: PlaybackBundle, position: Long, seekTo: (Long) -> Unit, modifier: Modifier = Modifier) {
    val marker = bundle.skipMarkers.firstOrNull { position in it.startMs..it.endMs }
    if (marker != null) {
        Button(onClick = { seekTo(marker.endMs) }, modifier = modifier, shape = RoundedCornerShape(8.dp)) {
            Text(if (marker.kind == SkipKind.Intro) "Skip intro" else "Skip outro")
        }
    }
}

private val Quality.label: String
    get() = when (this) {
        Quality.Auto -> "Auto"
        Quality.Q360 -> "360p"
        Quality.Q480 -> "480p"
        Quality.Q720 -> "720p"
        Quality.Q1080 -> "1080p"
    }
