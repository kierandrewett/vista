package dev.drewett.vista.ui.home

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.drewett.vista.data.apps.AppRepository
import dev.drewett.vista.data.channels.ChannelsRepository
import dev.drewett.vista.data.favourites.FavouritesRepository
import dev.drewett.vista.data.usage.LaunchHistoryRepository
import dev.drewett.vista.data.usage.UsageRepository
import dev.drewett.vista.domain.AppEntry
import dev.drewett.vista.domain.AppSection
import dev.drewett.vista.domain.ContentCard
import dev.drewett.vista.domain.ContentSection
import dev.drewett.vista.domain.HomeSection
import dev.drewett.vista.domain.RowType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class ContentData(
    val watchNext: List<ContentCard> = emptyList(),
    val preview: List<ContentCard> = emptyList(),
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository(application)
    private val usageRepository = UsageRepository(application)
    private val favouritesRepository = FavouritesRepository(application)
    private val launchHistoryRepository = LaunchHistoryRepository(application)
    private val channelsRepository = ChannelsRepository(application)

    private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())
    val apps = _apps.asStateFlow()

    private val _usageRecents = MutableStateFlow<List<String>>(emptyList())
    private val _content = MutableStateFlow(ContentData())

    val favourites = favouritesRepository.favourites
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val sections = combine(
        _apps,
        launchHistoryRepository.recent,
        _usageRecents,
        favourites,
        _content,
    ) { apps, launched, usage, favs, content ->
        buildSections(apps, launched.ifEmpty { usage }, favs, content)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allContent: StateFlow<List<ContentCard>> = _content
        .map { (it.watchNext + it.preview).distinctBy { c -> c.title.lowercase() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val movies = allContent.map { list -> list.filter { it.isMovie } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val shows = allContent.map { list -> list.filter { it.isShow } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val continueWatching = _content.map { it.watchNext }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            // Load everything first, then publish apps LAST. buildSections() returns empty until
            // apps is set, so the first non-empty section list already includes content — the
            // initial focus then lands on the true first row (Continue watching), not Jump back in.
            val content = ContentData(
                watchNext = channelsRepository.loadContinueWatching(),
                preview = channelsRepository.loadPreviewPrograms(),
            )
            val recents = usageRepository.recentPackages()
            val apps = appRepository.loadApps()
            _content.value = content
            _usageRecents.value = recents
            _apps.value = apps
        }
    }

    fun launch(entry: AppEntry) {
        viewModelScope.launch { launchHistoryRepository.record(entry.packageName) }
        appRepository.launch(entry)
    }

    fun launchContent(card: ContentCard) {
        card.packageName?.let { pkg -> viewModelScope.launch { launchHistoryRepository.record(pkg) } }
        if (!channelsRepository.launchContent(card)) {
            // Deep link failed — fall back to opening the owning app, if we know it.
            card.packageName
                ?.let { pkg -> _apps.value.firstOrNull { it.packageName == pkg } }
                ?.let(::launch)
        }
    }

    fun openAppInfo(packageName: String) = appRepository.openAppInfo(packageName)

    fun toggleFavourite(entry: AppEntry) {
        viewModelScope.launch { favouritesRepository.toggle(entry.packageName) }
    }

    private fun buildSections(
        apps: List<AppEntry>,
        recents: List<String>,
        favourites: Set<String>,
        content: ContentData,
    ): List<HomeSection> {
        if (apps.isEmpty()) return emptyList()
        val byPackage = apps.associateBy { it.packageName }
        val sections = mutableListOf<HomeSection>()

        if (content.watchNext.isNotEmpty()) {
            sections += ContentSection("continue", "Continue watching", content.watchNext)
        }

        recents.mapNotNull { byPackage[it] }.take(12)
            .takeIf { it.isNotEmpty() }
            ?.let { sections += AppSection(RowType.JUMP_BACK_IN, "Jump back in", it) }

        apps.filter { it.packageName in favourites }
            .takeIf { it.isNotEmpty() }
            ?.let { sections += AppSection(RowType.FAVOURITES, "Favourites", it) }

        // Per-app content rows from each app's published preview programs.
        content.preview
            .filter { it.packageName != null }
            .groupBy { it.packageName!! }
            .entries
            .sortedByDescending { it.value.size }
            .take(4)
            .forEach { (pkg, items) ->
                val app = byPackage[pkg] ?: return@forEach
                sections += ContentSection(
                    key = "preview:$pkg",
                    title = app.label,
                    items = items.take(20),
                    providerIcon = app.icon,
                )
            }

        apps.filter { it.category == ApplicationInfo.CATEGORY_VIDEO }
            .takeIf { it.isNotEmpty() }?.let { sections += AppSection(RowType.WATCH, "Watch", it) }
        apps.filter { it.category == ApplicationInfo.CATEGORY_AUDIO }
            .takeIf { it.isNotEmpty() }?.let { sections += AppSection(RowType.LISTEN, "Listen", it) }
        apps.filter { it.isGame }
            .takeIf { it.isNotEmpty() }?.let { sections += AppSection(RowType.PLAY, "Play", it) }

        val categorised = setOf(ApplicationInfo.CATEGORY_VIDEO, ApplicationInfo.CATEGORY_AUDIO)
        apps.filter { it.category !in categorised && !it.isGame }
            .takeIf { it.isNotEmpty() }
            ?.let { sections += AppSection(RowType.APPS, "Your apps", it) }

        return sections
    }
}
