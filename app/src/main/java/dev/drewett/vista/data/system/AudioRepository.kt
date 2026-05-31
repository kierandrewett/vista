package dev.drewett.vista.data.system

import android.content.Context
import android.media.MediaRouter

/** One selectable system audio output (TV speaker, HDMI, Bluetooth…). */
data class AudioOutput(
    val name: String,
    val selected: Boolean,
    val route: MediaRouter.RouteInfo,
)

/**
 * Lists and switches the live-audio output via MediaRouter. On Google TV this can move audio to a
 * connected Bluetooth/HDMI route; if the system only exposes the default route, only that shows.
 */
class AudioRepository(context: Context) {

    private val router = context.getSystemService(Context.MEDIA_ROUTER_SERVICE) as MediaRouter

    fun outputs(): List<AudioOutput> {
        val selected = router.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO)
        return (0 until router.routeCount)
            .map { router.getRouteAt(it) }
            .filter { (it.supportedTypes and MediaRouter.ROUTE_TYPE_LIVE_AUDIO) != 0 }
            .map { route ->
                AudioOutput(
                    name = route.name?.toString() ?: "Output",
                    selected = route === selected,
                    route = route,
                )
            }
    }

    fun select(route: MediaRouter.RouteInfo) {
        runCatching { router.selectRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO, route) }
    }
}
