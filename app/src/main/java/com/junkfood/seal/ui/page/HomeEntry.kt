package com.junkfood.seal.ui.page

import android.webkit.CookieManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.android.billing.findActivity
import com.junkfood.seal.Downloader
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.common.animatedComposable
import com.junkfood.seal.ui.common.animatedComposableVariant
import com.junkfood.seal.ui.common.arg
import com.junkfood.seal.ui.common.id
import com.junkfood.seal.ui.common.intState
import com.junkfood.seal.ui.common.slideInVerticallyComposable
import com.junkfood.seal.ui.component.PlusAndAdsDialog
import com.junkfood.seal.ui.page.billing.BillingPage
import com.junkfood.seal.ui.page.command.TaskListPage
import com.junkfood.seal.ui.page.command.TaskLogPage
import com.junkfood.seal.ui.page.download.DownloadPage
import com.junkfood.seal.ui.page.download.DownloadViewModel
import com.junkfood.seal.ui.page.download.FormatPage
import com.junkfood.seal.ui.page.download.PlaylistSelectionPage
import com.junkfood.seal.ui.page.download.SupportedSite
import com.junkfood.seal.ui.page.settings.SettingsPage
import com.junkfood.seal.ui.page.settings.about.AboutPage
import com.junkfood.seal.ui.page.settings.about.CreditsPage
import com.junkfood.seal.ui.page.settings.about.UpdatePage
import com.junkfood.seal.ui.page.settings.appearance.AppearancePreferences
import com.junkfood.seal.ui.page.settings.appearance.DarkThemePreferences
import com.junkfood.seal.ui.page.settings.appearance.LanguagePage
import com.junkfood.seal.ui.page.settings.command.TemplateEditPage
import com.junkfood.seal.ui.page.settings.command.TemplateListPage
import com.junkfood.seal.ui.page.settings.directory.DownloadDirectoryPreferences
import com.junkfood.seal.ui.page.settings.format.DownloadFormatPreferences
import com.junkfood.seal.ui.page.settings.format.SubtitlePreference
import com.junkfood.seal.ui.page.settings.general.GeneralDownloadPreferences
import com.junkfood.seal.ui.page.settings.interaction.InteractionPreferencePage
import com.junkfood.seal.ui.page.settings.network.CookieProfilePage
import com.junkfood.seal.ui.page.settings.network.CookiesViewModel
import com.junkfood.seal.ui.page.settings.network.NetworkPreferences
import com.junkfood.seal.ui.page.settings.network.WebViewPage
import com.junkfood.seal.ui.page.videolist.VideoListPage
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.getLong
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.PreferenceUtil.updateInt
import com.junkfood.seal.util.SHOW_REVIEW
import com.junkfood.seal.util.UpdateUtil
import com.junkfood.seal.util.YT_DLP_AUTO_UPDATE
import com.junkfood.seal.util.YT_DLP_UPDATE_INTERVAL
import com.junkfood.seal.util.YT_DLP_UPDATE_TIME
import com.junkfood.seal.util.YT_DLP_VERSION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.warting.inappupdate.compose.review.rememberInAppReviewManager


@Composable
fun HomeEntry(
    downloadViewModel: DownloadViewModel,
    cookiesViewModel: CookiesViewModel,
    isUrlShared: Boolean,
    onViewAds: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val showReview by SHOW_REVIEW.intState
    val makeUp by downloadViewModel.makeUpStateFlow.collectAsStateWithLifecycle()
    var showGetPointDialog by remember { mutableStateOf(false) }
    val inAppReviewManager = rememberInAppReviewManager()
    if (makeUp.isNotBlank()) {
        showGetPointDialog = true
    }
    if (showGetPointDialog) {
        PlusAndAdsDialog(
            onDismissRequest = {
                showGetPointDialog = false
                downloadViewModel.makeUp("")
            },
            onMakePlus = {
                navController.navigate(Route.DONATE)
            },
            onViewAds = onViewAds
        )
    }

    val onNavigateBack: () -> Unit = {
        with(navController) {
            if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                popBackStack()
            }
        }
    }

    if (isUrlShared) {
        if (navController.currentDestination?.route != Route.HOME) {
            navController.popBackStack(route = Route.HOME, inclusive = false, saveState = true)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        NavHost(
            modifier = Modifier
                .fillMaxWidth(
                    when (LocalWindowWidthState.current) {
                        WindowWidthSizeClass.Compact -> 1f
                        WindowWidthSizeClass.Expanded -> 0.5f
                        else -> 0.8f
                    }
                )
                .align(Alignment.Center),
            navController = navController,
            startDestination = Route.HOME
        ) {
            animatedComposable(Route.HOME) {
                DownloadPage(
                    navigateToDownloads = { navController.navigate(Route.DOWNLOADS) },
                    navigateToSettings = {
                        navController.navigate(Route.SETTINGS) {
                            launchSingleTop = true
                        }
                    },
                    navigateToPlaylistPage = { navController.navigate(Route.PLAYLIST) },
                    navigateToFormatPage = { navController.navigate(Route.FORMAT_SELECTION) },
                    onNavigateToTaskList = { navController.navigate(Route.TASK_LIST) },
                    onNavigateToCookieGeneratorPage = {
                        cookiesViewModel.updateUrl(it)
                        navController.navigate(Route.COOKIE_GENERATOR_WEBVIEW)
                    },
                    downloadViewModel = downloadViewModel,
                    onNavigateToSupportedSite = { navController.navigate(Route.SUPPORTED_SITE_ROUTER) },
                    onViewAds = onViewAds,
                    onMakePlus = { navController.navigate(Route.DONATE) }
                )
            }
            animatedComposable(Route.DOWNLOADS) { VideoListPage { onNavigateBack() } }
            animatedComposableVariant(Route.TASK_LIST) {
                TaskListPage(
                    onNavigateBack = onNavigateBack,
                    onNavigateToDetail = { navController.navigate(Route.TASK_LOG id it) }
                )
            }
            slideInVerticallyComposable(
                Route.TASK_LOG arg Route.TASK_HASHCODE,
                arguments = listOf(navArgument(Route.TASK_HASHCODE) { type = NavType.IntType })
            ) {
                TaskLogPage(
                    onNavigateBack = onNavigateBack,
                    taskHashCode = it.arguments?.getInt(Route.TASK_HASHCODE) ?: -1
                )
            }

//            animatedComposable(Route.DOWNLOAD_QUEUE) { DownloadQueuePage { onNavigateBack() } }
            slideInVerticallyComposable(Route.PLAYLIST) { PlaylistSelectionPage { onNavigateBack() } }
            slideInVerticallyComposable(Route.FORMAT_SELECTION) { FormatPage(downloadViewModel) { onNavigateBack() } }
            slideInVerticallyComposable(Route.SUPPORTED_SITE_ROUTER) { SupportedSite { onNavigateBack() } }
            settingsGraph(
                cookiesViewModel = cookiesViewModel,
                onNavigateBack = onNavigateBack,
                onNavigateTo = { route ->
                    navController.navigate(route = route) {
                        launchSingleTop = true
                    }
                }
            )

        }

        WelcomeDialog {
            navController.navigate(Route.SETTINGS)
        }

        val downloaderState by Downloader.downloaderState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            if (downloaderState !is Downloader.State.Idle) return@LaunchedEffect

            if (!YT_DLP_AUTO_UPDATE.getBoolean() && YT_DLP_VERSION.getString()
                    .isNotEmpty()
            ) return@LaunchedEffect

            if (!PreferenceUtil.isNetworkAvailableForDownload()) {
                return@LaunchedEffect
            }

            val lastUpdateTime = YT_DLP_UPDATE_TIME.getLong()
            val currentTime = System.currentTimeMillis()

            if (currentTime < lastUpdateTime + YT_DLP_UPDATE_INTERVAL.getLong()) {
                return@LaunchedEffect
            }

            runCatching {
                Downloader.updateState(state = Downloader.State.Updating)
                withContext(Dispatchers.IO) {
                    UpdateUtil.updateYtDlp()
                }
            }.onFailure {
                it.printStackTrace()
            }
            Downloader.updateState(state = Downloader.State.Idle)
        }
        if (showReview == 10) {
            inAppReviewManager.launchReviewFlow(activity = context.findActivity(), onReviewRequestSuccess = {
            }, onReviewRequestFail = {
                SHOW_REVIEW.updateInt(0)
            })
        }
    }
}

fun NavGraphBuilder.settingsGraph(
    cookiesViewModel: CookiesViewModel,
    onNavigateBack: () -> Unit,
    onNavigateTo: (route: String) -> Unit
) {
    navigation(startDestination = Route.SETTINGS_PAGE, route = Route.SETTINGS) {
        animatedComposable(Route.DOWNLOAD_DIRECTORY) {
            DownloadDirectoryPreferences(onNavigateBack)
        }
        animatedComposable(Route.SETTINGS_PAGE) {
            SettingsPage(
                onNavigateBack = onNavigateBack,
                onNavigateTo = onNavigateTo
            )
        }
        animatedComposable(Route.GENERAL_DOWNLOAD_PREFERENCES) {
            GeneralDownloadPreferences(
                onNavigateBack = { onNavigateBack() },
            ) { onNavigateTo(Route.TEMPLATE) }
        }
        animatedComposable(Route.DOWNLOAD_FORMAT) {
            DownloadFormatPreferences(onNavigateBack = onNavigateBack) {
                onNavigateTo(Route.SUBTITLE_PREFERENCES)
            }
        }
        animatedComposable(Route.SUBTITLE_PREFERENCES) { SubtitlePreference { onNavigateBack() } }
        animatedComposable(Route.ABOUT) {
            AboutPage(
                onNavigateBack = onNavigateBack,
                onNavigateToCreditsPage = { onNavigateTo(Route.CREDITS) },
                onNavigateToUpdatePage = { onNavigateTo(Route.AUTO_UPDATE) },
                onNavigateToDonatePage = { onNavigateTo(Route.DONATE) })
        }
        animatedComposable(Route.DONATE) { BillingPage(onNavigateBack) }
        animatedComposable(Route.CREDITS) { CreditsPage(onNavigateBack) }
        animatedComposable(Route.AUTO_UPDATE) { UpdatePage(onNavigateBack) }
        animatedComposable(Route.APPEARANCE) {
            AppearancePreferences(
                onNavigateBack = onNavigateBack,
                onNavigateTo = onNavigateTo
            )
        }
        animatedComposable(Route.INTERACTION) { InteractionPreferencePage(onBack = onNavigateBack) }
        animatedComposable(Route.LANGUAGES) { LanguagePage { onNavigateBack() } }
        animatedComposable(Route.DOWNLOAD_DIRECTORY) {
            DownloadDirectoryPreferences { onNavigateBack() }
        }
        animatedComposable(Route.TEMPLATE) {
            TemplateListPage(onNavigateBack = onNavigateBack) {
                onNavigateTo(Route.TEMPLATE_EDIT id it)
            }
        }
        animatedComposable(
            Route.TEMPLATE_EDIT arg Route.TEMPLATE_ID,
            arguments = listOf(navArgument(Route.TEMPLATE_ID) { type = NavType.IntType })
        ) {
            TemplateEditPage(onNavigateBack, it.arguments?.getInt(Route.TEMPLATE_ID) ?: -1)
        }
        animatedComposable(Route.DARK_THEME) { DarkThemePreferences { onNavigateBack() } }
        animatedComposable(Route.NETWORK_PREFERENCES) {
            NetworkPreferences(navigateToCookieProfilePage = {
                onNavigateTo(Route.COOKIE_PROFILE)
            }) { onNavigateBack() }
        }
        animatedComposable(Route.COOKIE_PROFILE) {
            CookieProfilePage(
                cookiesViewModel = cookiesViewModel,
                navigateToCookieGeneratorPage = { onNavigateTo(Route.COOKIE_GENERATOR_WEBVIEW) },
            ) { onNavigateBack() }
        }
        animatedComposable(
            Route.COOKIE_GENERATOR_WEBVIEW
        ) {
            WebViewPage(cookiesViewModel) {
                onNavigateBack()
                CookieManager.getInstance().flush()
            }
        }
    }
}

