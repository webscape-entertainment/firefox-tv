/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.tv.firefox.webrender

import android.content.Context
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.browser.session.SessionManager
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.support.utils.SafeIntent
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import mozilla.components.browser.engine.gecko.fetch.GeckoViewFetchClient
import mozilla.components.browser.engine.gecko.glean.GeckoAdapter
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.engine.EngineMiddleware
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.session.middleware.LastAccessMiddleware
import org.mozilla.tv.firefox.R
import org.mozilla.tv.firefox.utils.BuildConstants
import org.mozilla.tv.firefox.utils.Settings

/**
 * Helper class for lazily instantiating and keeping references to components needed by the
 * application.
 */
class WebRenderComponents(applicationContext: Context, systemUserAgent: String) {
    // The first intent the App was launched with.  Used to pass configuration through to Gecko.
    private var launchSafeIntent: SafeIntent? = null

    fun notifyLaunchWithSafeIntent(safeIntent: SafeIntent): Boolean {
        // We can't access the property reference outside of our own lexical scope,
        // so this helper must be in this class.
        if (launchSafeIntent == null) {
            launchSafeIntent = safeIntent
            return true
        }
        return false
    }

    private val runtime by lazy {
        // Allow for exfiltrating Gecko metrics through the Glean SDK.
        val builder = GeckoRuntimeSettings.Builder()
        builder.telemetryDelegate(GeckoAdapter())
        if (BuildConstants.isDevBuild) {
            // In debug builds, allow to invoke via an Intent that has extras customizing Gecko.
            // In particular, this allows to add command line arguments for custom profiles, etc.
            val extras = launchSafeIntent?.extras
            if (extras != null) {
                builder.extras(extras)
            }
        }

        GeckoRuntime.create(applicationContext, builder.build())
    }

    val engine: Engine by lazy {
        fun getUserAgent(): String = UserAgent.buildUserAgentString(
                applicationContext,
                systemUserAgent = systemUserAgent,
                appName = applicationContext.resources.getString(R.string.useragent_appname))

        GeckoEngine(applicationContext, DefaultSettings(
                trackingProtectionPolicy = Settings.getInstance(applicationContext).trackingProtectionPolicy,
                requestInterceptor = CustomContentRequestInterceptor(applicationContext),
                userAgentString = getUserAgent(),

                displayZoomControls = false,
                loadWithOverviewMode = true, // To respect the html viewport

                // We don't have a reason for users to access local files; assets can still
                // be loaded via file:///android_asset/
                allowFileAccess = false,
                allowContentAccess = false,

                remoteDebuggingEnabled = BuildConstants.isDevBuild,

                mediaPlaybackRequiresUserGesture = false // Allows auto-play (which improves YouTube experience).
        ), runtime)
    }

    val sessionManager by lazy { SessionManager(engine) }

    val client by lazy { GeckoViewFetchClient(applicationContext, runtime) }

    //val icons by lazy { BrowserIcons(applicationContext, client) }

    val store by lazy {
        BrowserStore(middleware = listOf(
            LastAccessMiddleware()
        ) + EngineMiddleware.create(engine, ::findSessionById))
    }

    private fun findSessionById(tabId: String): Session? {
        return sessionManager.findSessionById(tabId)
    }
}
