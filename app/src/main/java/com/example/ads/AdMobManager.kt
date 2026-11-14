package com.example.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.Date

/**
 * Google AdMob Publisher Configuration.
 * বর্তমানে Google-এর অফিসিয়াল টেস্ট Ad Unit ID ব্যবহার করা হয়েছে।
 * Play Store-এ পাবলিশ করার আগে আপনার নিজের AdMob অ্যাকাউন্ট থেকে তৈরি
 * আসল Ad Unit ID এবং strings.xml-এ admob_app_id পরিবর্তন করে দিন।
 */
object AdMobConfig {
    const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
}

object AdMobManager : Application.ActivityLifecycleCallbacks {

    private const val APP_OPEN_EXPIRY_HOURS = 4L
    private const val FULLSCREEN_AD_COOLDOWN_MS = 40_000L
    private const val COLD_START_WAIT_WINDOW_MS = 6_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var isInitialized = false

    private var appOpenAd: AppOpenAd? = null
    private var appOpenLoadTimeMs: Long = 0L
    private var isLoadingAppOpen = false

    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false

    @Volatile
    var isShowingFullScreenAd: Boolean = false
        private set

    private var lastFullScreenAdShownMs: Long = 0L
    private var appStartTimeMs: Long = 0L
    private var hasShownColdStartAd: Boolean = false
    private var startedActivityCount: Int = 0
    private var currentActivityRef: WeakReference<Activity>? = null

    fun registerAndInitialize(application: Application) {
        if (isInitialized) return
        isInitialized = true
        appStartTimeMs = System.currentTimeMillis()
        application.registerActivityLifecycleCallbacks(this)

        scope.launch {
            runCatching {
                MobileAds.initialize(application)
            }
            launch(Dispatchers.Main) {
                loadAppOpenAd(application)
                loadInterstitialAd(application)
            }
        }
    }

    fun loadAppOpenAd(context: Context) {
        if (isLoadingAppOpen || isAppOpenAdAvailable()) return
        isLoadingAppOpen = true
        val request = AdRequest.Builder().build()
        runCatching {
            AppOpenAd.load(
                context.applicationContext,
                AdMobConfig.APP_OPEN_AD_UNIT_ID,
                request,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        appOpenLoadTimeMs = Date().time
                        isLoadingAppOpen = false

                        // If this loaded right after cold start, show immediately on the active screen
                        val withinColdStartWindow =
                            (System.currentTimeMillis() - appStartTimeMs) <= COLD_START_WAIT_WINDOW_MS
                        val activity = currentActivityRef?.get()
                        if (!hasShownColdStartAd && withinColdStartWindow && activity != null && !activity.isFinishing) {
                            showAppOpenAdIfAvailable(activity)
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoadingAppOpen = false
                    }
                }
            )
        }.onFailure {
            isLoadingAppOpen = false
        }
    }

    fun loadInterstitialAd(context: Context) {
        if (isLoadingInterstitial || interstitialAd != null) return
        isLoadingInterstitial = true
        val request = AdRequest.Builder().build()
        runCatching {
            InterstitialAd.load(
                context.applicationContext,
                AdMobConfig.INTERSTITIAL_AD_UNIT_ID,
                request,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoadingInterstitial = false
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isLoadingInterstitial = false
                    }
                }
            )
        }.onFailure {
            isLoadingInterstitial = false
        }
    }

    private fun isAppOpenAdAvailable(): Boolean {
        val ad = appOpenAd ?: return false
        val elapsedHours = (Date().time - appOpenLoadTimeMs) / (1000L * 60L * 60L)
        if (elapsedHours >= APP_OPEN_EXPIRY_HOURS) {
            appOpenAd = null
            return false
        }
        return ad != null
    }

    private fun isCooldownPassed(): Boolean {
        if (lastFullScreenAdShownMs == 0L) return true
        return (System.currentTimeMillis() - lastFullScreenAdShownMs) >= FULLSCREEN_AD_COOLDOWN_MS
    }

    fun showAppOpenAdIfAvailable(activity: Activity, onComplete: () -> Unit = {}) {
        if (isShowingFullScreenAd || activity.isFinishing || activity.isDestroyed) {
            onComplete()
            return
        }
        if (!isAppOpenAdAvailable()) {
            loadAppOpenAd(activity)
            onComplete()
            return
        }
        if (hasShownColdStartAd && !isCooldownPassed()) {
            onComplete()
            return
        }

        val ad = appOpenAd ?: run {
            onComplete()
            return
        }
        hasShownColdStartAd = true
        isShowingFullScreenAd = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingFullScreenAd = false
                lastFullScreenAdShownMs = System.currentTimeMillis()
                loadAppOpenAd(activity)
                onComplete()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingFullScreenAd = false
                loadAppOpenAd(activity)
                onComplete()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingFullScreenAd = true
            }
        }
        ad.show(activity)
    }

    /**
     * গেম স্ক্রিনে প্রবেশ বা নতুন কুইজ শুরু করার সময় ইন্টাস্টিশিয়াল এড দেখায়।
     * Google Play নীতিমালা মেনে অপ্রয়োজনীয় বিরক্তি এড়াতে কুলডাউন টাইমার অনুসরণ করে।
     */
    fun showInterstitialOnGameEntry(
        activity: Activity?,
        forceIgnoreShortCooldown: Boolean = false,
        onComplete: () -> Unit = {}
    ) {
        if (activity == null || activity.isFinishing || activity.isDestroyed || isShowingFullScreenAd) {
            onComplete()
            return
        }
        val allowNow = if (forceIgnoreShortCooldown) {
            (System.currentTimeMillis() - lastFullScreenAdShownMs) >= 15_000L
        } else {
            isCooldownPassed()
        }

        val ad = interstitialAd
        if (ad == null || !allowNow) {
            loadInterstitialAd(activity)
            onComplete()
            return
        }

        isShowingFullScreenAd = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                isShowingFullScreenAd = false
                lastFullScreenAdShownMs = System.currentTimeMillis()
                loadInterstitialAd(activity)
                onComplete()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                isShowingFullScreenAd = false
                loadInterstitialAd(activity)
                onComplete()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingFullScreenAd = true
            }
        }
        ad.show(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        if (!isShowingFullScreenAd) {
            currentActivityRef = WeakReference(activity)
        }
        val wasInBackground = startedActivityCount == 0
        startedActivityCount++
        if (wasInBackground && !isShowingFullScreenAd) {
            showAppOpenAdIfAvailable(activity)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (!isShowingFullScreenAd) {
            currentActivityRef = WeakReference(activity)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivityRef?.get() == activity) {
            currentActivityRef = null
        }
    }
}

@Composable
fun AdMobBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        val widthDp = maxWidth.value.toInt().coerceAtLeast(320)
        val adView = remember(context, widthDp) {
            runCatching {
                AdView(context).apply {
                    adUnitId = AdMobConfig.BANNER_AD_UNIT_ID
                    val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
                    setAdSize(adaptiveSize)
                    loadAd(AdRequest.Builder().build())
                }
            }.getOrNull()
        }

        if (adView != null) {
            DisposableEffect(lifecycleOwner, adView) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> runCatching { adView.resume() }
                        Lifecycle.Event.ON_PAUSE -> runCatching { adView.pause() }
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    runCatching { adView.destroy() }
                }
            }

            AndroidView(
                factory = { adView },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
