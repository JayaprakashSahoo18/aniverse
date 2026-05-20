package com.codex.animestream.ui

import androidx.compose.foundation.clickable
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
import com.codex.animestream.domain.Anime
import com.codex.animestream.domain.HomeSections

@Composable
fun HomeScreen(
    openAnime: (String) -> Unit,
    openSearch: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 44.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("AnimeStream", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Button(onClick = openSearch, shape = RoundedCornerShape(8.dp)) { Text("Search") }
            }
        }
        when (val current = state) {
            LoadState.Loading -> item { LoadingBlock("Loading catalog") }
            is LoadState.Error -> item { ErrorBlock(current.message, viewModel::refresh) }
            is LoadState.Ready -> homeSections(current.value, openAnime)
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.homeSections(sections: HomeSections, openAnime: (String) -> Unit) {
    animeRow("Trending Anime", sections.trending, openAnime)
    animeRow("Popular", sections.popular, openAnime)
    animeRow("Latest Episodes", sections.latestEpisodes, openAnime)
    animeRow("Continue Watching", sections.continueWatching, openAnime)
    animeRow("Seasonal Anime", sections.seasonal, openAnime)
    animeRow("Recommended For You", sections.recommended, openAnime)
}

private fun androidx.compose.foundation.lazy.LazyListScope.animeRow(title: String, items: List<Anime>, openAnime: (String) -> Unit) {
    if (items.isEmpty()) return
    item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, modifier = Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(items, key = { it.id }) { anime -> AnimePoster(anime, openAnime) }
            }
        }
    }
}

@Composable
fun AnimePoster(anime: Anime, openAnime: (String) -> Unit) {
    Column(
        modifier = Modifier.width(132.dp).clickable { openAnime(anime.id) },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AsyncImage(
            model = anime.posterUrl,
            contentDescription = anime.title,
            modifier = Modifier.height(190.dp).fillMaxWidth().clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
        Text(anime.title, maxLines = 2, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun LoadingBlock(label: String) {
    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator()
            Text(label)
        }
    }
}

@Composable
fun ErrorBlock(message: String, retry: () -> Unit) {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(message, color = MaterialTheme.colorScheme.error)
        OutlinedButton(onClick = retry, shape = RoundedCornerShape(8.dp)) { Text("Retry") }
    }
}
