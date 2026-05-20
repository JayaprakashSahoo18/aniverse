package com.codex.animestream.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codex.animestream.data.AnimeRepository
import com.codex.animestream.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Ready<T>(val value: T) : LoadState<T>
    data class Error(val message: String) : LoadState<Nothing>
}

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: AnimeRepository) : ViewModel() {
    private val _state = MutableStateFlow<LoadState<HomeSections>>(LoadState.Loading)
    val state: StateFlow<LoadState<HomeSections>> = _state.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _state.value = LoadState.Loading
        _state.value = runCatching { repository.home() }
            .fold({ LoadState.Ready(it) }, { LoadState.Error(it.message ?: "Unable to load provider catalog") })
    }
}

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: AnimeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val animeId = checkNotNull(savedStateHandle["animeId"]) as String
    private val _state = MutableStateFlow<LoadState<AnimeDetails>>(LoadState.Loading)
    val state: StateFlow<LoadState<AnimeDetails>> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = runCatching { repository.details(animeId) }
                .fold({ LoadState.Ready(it) }, { LoadState.Error(it.message ?: "Unable to load anime details") })
        }
    }
}

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: AnimeRepository) : ViewModel() {
    private val _results = MutableStateFlow<LoadState<List<Anime>>>(LoadState.Ready(emptyList()))
    val results: StateFlow<LoadState<List<Anime>>> = _results.asStateFlow()
    val filters = MutableStateFlow(SearchFilters())

    fun search(query: String) = viewModelScope.launch {
        _results.value = LoadState.Loading
        _results.value = runCatching { repository.search(query, filters.value) }
            .fold({ LoadState.Ready(it) }, { LoadState.Error(it.message ?: "Search failed") })
    }
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: AnimeRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val episodeId = checkNotNull(savedStateHandle["episodeId"]) as String
    private val _state = MutableStateFlow<LoadState<PlaybackBundle>>(LoadState.Loading)
    val state: StateFlow<LoadState<PlaybackBundle>> = _state.asStateFlow()
    val progress = repository.observeProgress(episodeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            _state.value = runCatching { repository.playback(episodeId) }
                .fold({ LoadState.Ready(it) }, { LoadState.Error(it.message ?: "No playable stream") })
        }
    }

    fun saveProgress(bundle: PlaybackBundle, positionMs: Long, durationMs: Long) {
        viewModelScope.launch {
            repository.saveProgress(bundle.episode.id, bundle.episode.animeId, positionMs, durationMs)
        }
    }
}
