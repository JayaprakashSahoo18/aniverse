package com.codex.animestream.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.codex.animestream.domain.AnimeDetails

@Composable
fun DetailsScreen(
    playEpisode: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    when (val current = state) {
        LoadState.Loading -> LoadingBlock("Loading anime")
        is LoadState.Error -> ErrorBlock(current.message, onBack)
        is LoadState.Ready -> DetailsContent(current.value, playEpisode, onBack)
    }
}

@Composable
private fun DetailsContent(details: AnimeDetails, playEpisode: (String) -> Unit, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Box {
                AsyncImage(
                    model = details.anime.bannerUrl,
                    contentDescription = details.anime.title,
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    contentScale = ContentScale.Crop,
                )
                OutlinedButton(onClick = onBack, modifier = Modifier.padding(top = 42.dp, start = 16.dp), shape = RoundedCornerShape(8.dp)) { Text("Back") }
            }
        }
        item {
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AsyncImage(
                    model = details.anime.posterUrl,
                    contentDescription = details.anime.title,
                    modifier = Modifier.width(116.dp).height(170.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(details.anime.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(listOfNotNull(details.anime.studio, details.anime.year?.toString(), details.anime.status, details.anime.rating?.let { "$it" }).joinToString(" • "))
                    Text(details.anime.genres.joinToString(" / "), color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
        item { Text(details.anime.synopsis, modifier = Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.bodyLarge) }
        item { SectionTitle("Episodes") }
        items(details.episodes, key = { it.id }) { ep ->
            ListItem(
                headlineContent = { Text("Episode ${ep.number}: ${ep.title}") },
                supportingContent = { Text(ep.durationMs?.let { "${it / 60000} min" } ?: "") },
                trailingContent = { Button(onClick = { playEpisode(ep.id) }, shape = RoundedCornerShape(8.dp)) { Text("Play") } },
            )
        }
        item { SectionTitle("Characters") }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(details.characters, key = { it.id }) { character ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(92.dp)) {
                        AsyncImage(character.imageUrl, character.name, Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        Text(character.name, maxLines = 2, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item { SectionTitle("Related Anime") }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(details.related, key = { it.id }) { AnimePoster(it) { } }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, modifier = Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
}
