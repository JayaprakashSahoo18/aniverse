package com.codex.animestream.ui

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale

@Composable
fun SearchScreen(
    openAnime: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.results.collectAsState()
    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spoken.isNullOrBlank()) {
            query = spoken
            viewModel.search(spoken)
        }
    }

    Column(Modifier.fillMaxSize().padding(top = 42.dp, start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack, shape = RoundedCornerShape(8.dp)) { Text("Back") }
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    if (it.length > 2) viewModel.search(it)
                },
                modifier = Modifier.weight(1f),
                label = { Text("Global search") },
                singleLine = true,
            )
            Button(
                onClick = {
                    voiceLauncher.launch(
                        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    )
                },
                shape = RoundedCornerShape(8.dp),
            ) { Text("Voice") }
        }
        FilterBar(viewModel)
        when (val state = results) {
            LoadState.Loading -> LoadingBlock("Searching")
            is LoadState.Error -> ErrorBlock(state.message) { viewModel.search(query) }
            is LoadState.Ready -> LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(state.value, key = { it.id }) { AnimeListItem(it, openAnime) }
            }
        }
    }
}

@Composable
private fun FilterBar(viewModel: SearchViewModel) {
    val filters by viewModel.filters.collectAsState()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = filters.genre == "Action", onClick = { viewModel.filters.value = filters.copy(genre = if (filters.genre == "Action") null else "Action") }, label = { Text("Genre") })
        FilterChip(selected = filters.status == "Ongoing", onClick = { viewModel.filters.value = filters.copy(status = if (filters.status == "Ongoing") null else "Ongoing") }, label = { Text("Status") })
        FilterChip(selected = filters.language == "ja", onClick = { viewModel.filters.value = filters.copy(language = if (filters.language == "ja") null else "ja") }, label = { Text("Language") })
        FilterChip(selected = filters.minimumRating == 8.0, onClick = { viewModel.filters.value = filters.copy(minimumRating = if (filters.minimumRating == 8.0) null else 8.0) }, label = { Text("Rating") })
    }
}

@Composable
fun AnimeListItem(anime: com.codex.animestream.domain.Anime, openAnime: (String) -> Unit) {
    ElevatedCard(onClick = { openAnime(anime.id) }, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(anime.title, fontWeight = FontWeight.SemiBold)
            Text(listOfNotNull(anime.year?.toString(), anime.status, anime.studio).joinToString(" • "), style = MaterialTheme.typography.bodySmall)
            Text(anime.genres.joinToString(" / "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
