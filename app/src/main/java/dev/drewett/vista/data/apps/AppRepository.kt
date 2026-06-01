package dev.drewett.vista.data.apps

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import dev.drewett.vista.domain.AppEntry

/**
 * Enumerates launchable apps directly from the OS. TV (LEANBACK_LAUNCHER) entries win over
 * plain phone (LAUNCHER) entries for the same package, so sideloaded TV apps show their banner
 * while still surfacing phone apps that only register a standard launcher activity.
 */
class AppRepository(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    fun loadApps(): List<AppEntry> {
        val byPackage = LinkedHashMap<String, AppEntry>()
        queryFor(Intent.CATEGORY_LEANBACK_LAUNCHER).forEach { ri -> addIfAbsent(byPackage, ri, leanback = true) }
        queryFor(Intent.CATEGORY_LAUNCHER).forEach { ri -> addIfAbsent(byPackage, ri, leanback = false) }
        return byPackage.values.sortedBy { it.label.lowercase() }
    }

    private fun queryFor(category: String): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
        return pm.queryIntentActivities(intent, 0)
    }

    private fun addIfAbsent(into: LinkedHashMap<String, AppEntry>, ri: ResolveInfo, leanback: Boolean) {
        val activity = ri.activityInfo ?: return
        val pkg = activity.packageName
        if (pkg == context.packageName) return        // never list ourselves
        if (into.containsKey(pkg)) return              // first (leanback) entry wins

        val appInfo: ApplicationInfo = activity.applicationInfo
        val componentName = ComponentName(pkg, activity.name)
        val banner = if (leanback) {
            runCatching { pm.getActivityBanner(componentName) }.getOrNull()
                ?: runCatching { pm.getApplicationBanner(pkg) }.getOrNull()
        } else {
            null
        }
        into[pkg] = AppEntry(
            packageName = pkg,
            componentName = componentName,
            label = ri.loadLabel(pm).toString(),
            banner = banner,
            icon = hiResIcon(ri, appInfo),
            category = appInfo.category,
            isLeanback = leanback,
        )
    }

    /** Pull the xxxhdpi icon variant so it stays crisp at large (circular) sizes. */
    private fun hiResIcon(ri: ResolveInfo, appInfo: ApplicationInfo): android.graphics.drawable.Drawable {
        val iconRes = ri.iconResource.takeIf { it != 0 } ?: appInfo.icon
        if (iconRes != 0) {
            runCatching {
                pm.getResourcesForApplication(appInfo)
                    .getDrawableForDensity(iconRes, android.util.DisplayMetrics.DENSITY_XXXHIGH, null)
            }.getOrNull()?.let { return it }
        }
        return ri.loadIcon(pm)
    }

    /** Launch an app via its resolved component. */
    fun launch(entry: AppEntry) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(if (entry.isLeanback) Intent.CATEGORY_LEANBACK_LAUNCHER else Intent.CATEGORY_LAUNCHER)
            component = entry.componentName
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        runCatching { context.startActivity(intent) }
    }

    /** Open the system App Info screen for an app. */
    fun openAppInfo(packageName: String) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }

    /** Launch the system uninstall confirmation for an app. */
    fun uninstall(packageName: String) {
        @Suppress("DEPRECATION")
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
            putExtra(Intent.EXTRA_RETURN_RESULT, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { context.startActivity(intent) }
    }
}
