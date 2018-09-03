/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.engine

import android.content.Context
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.request.RequestInterceptor
import org.mozilla.focus.browser.BrowserFragment
import org.mozilla.focus.browser.LocalizedContent

/**
 * [RequestInterceptor] implementation to inject custom content for firefox:* pages.
 */
class CustomContentRequestInterceptor(
    private val context: Context
) : RequestInterceptor {
    override fun onLoadRequest(session: EngineSession, uri: String): RequestInterceptor.InterceptionResponse? {
        return when (uri) {
            BrowserFragment.APP_URL_HOME -> RequestInterceptor.InterceptionResponse("<html></html>")

            LocalizedContent.URL_ABOUT -> RequestInterceptor.InterceptionResponse(
                LocalizedContent.generateAboutPage(context))

            else -> null
        }
    }
}
