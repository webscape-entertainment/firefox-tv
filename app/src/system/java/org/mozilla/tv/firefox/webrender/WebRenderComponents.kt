/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.tv.firefox.webrender

import android.content.Context
import mozilla.components.browser.engine.system.SystemEngine
import mozilla.components.browser.session.SessionManager
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.browser.session.Session
import mozilla.components.lib.fetch.httpurlconnection.HttpURLConnectionClient
import mozilla.components.browser.session.engine.EngineMiddleware
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.session.middleware.LastAccessMiddleware
import mozilla.components.support.utils.SafeIntent
import org.mozilla.tv.firefox.R
import org.mozilla.tv.firefox.utils.BuildConstants
import org.mozilla.tv.firefox.utils.Settings

/**
 * Helper class for lazily instantiating and keeping references to components needed by the
 * application.
 */
class WebRenderComponents(applicationContext: Context, systemUserAgent: String) {
    fun notifyLaunchWithSafeIntent(@Suppress("UNUSED_PARAMETER") safeIntent: SafeIntent): Boolean {
        // For the system WebView, we don't need the initial launch intent right now.  In the
        // future, we might configure a proxy server using this intent for automation.
        return false
    }

    val engine: Engine by lazy {
        fun getUserAgent(): String = UserAgent.buildUserAgentString(
                applicationContext,
                systemUserAgent = systemUserAgent,
                appName = applicationContext.resources.getString(R.string.useragent_appname))

        SystemEngine(applicationContext, DefaultSettings(
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
        ))
    }

    val sessionManager by lazy { SessionManager(engine) }

    val client: Client by lazy { HttpURLConnectionClient() }

    val store by lazy {
        BrowserStore(middleware = listOf(
            LastAccessMiddleware()
        ) + EngineMiddleware.create(engine, ::findSessionById))
    }

    private fun findSessionById(tabId: String): Session? {
        return sessionManager.findSessionById(tabId)
    }
}
