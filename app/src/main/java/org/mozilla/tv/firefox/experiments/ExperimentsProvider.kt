/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.tv.firefox.experiments

import android.content.Context
import org.mozilla.tv.firefox.R

/**
 * [ExperimentsProvider] checks for experiment branch from [Fretboard] to provide its respective content.
 * See [getAAExitButtonExperiment] for example
 *
 * Note: Consider implementing fallback options (log in Sentry using [ExperimentIllegalStateException]
 * since fretboard doesn't necessarily load the latest changes from Kinto backend. See
 * [FretboardProvider.updateExperiments] and [FretboardProvider.loadExperiments] for more details
 */
class ExperimentsProvider(private val context: Context) {

    fun getAAExitButtonExperiment(): String {
        return context.resources.getString(R.string.exit_firefox_a11y,
            context.resources.getString(R.string.firefox_tv_brand_name_short))
    }

    fun shouldShowHintBar(): Boolean {
        return true
    }

    fun shouldShowTvGuideChannels(): Boolean {
        return true
    }

    fun shouldShowSendTab(): Boolean {
        return true
    }

    /** This is not an experiment: see [ExperimentConfig.MP4_VIDEO_WORKAROUND] for details. */
    fun shouldUseMp4VideoWorkaround(): Boolean {
        /* val expDescriptor = checkBranchVariants(ExperimentConfig.MP4_VIDEO_WORKAROUND)
        return when {
            expDescriptor == null -> false // Experiment unknown, or overridden to be false.
            expDescriptor.name.endsWith(ExperimentSuffix.A.value) -> false
            expDescriptor.name.endsWith(ExperimentSuffix.B.value) -> true
            else -> {
                Sentry.capture(ExperimentIllegalStateException("MP4 Video Workaround Illegal Branch Name"))
                false
            }
        } */
        return true
    }

    private fun shouldUseTurboRebrand(): Boolean {
        return true
    }

    data class TurboModeToolbarContent(
        val imageId: Int,
        val enabledTextId: Int,
        val disabledTextId: Int
    )

    fun getTurboModeToolbar() = when (shouldUseTurboRebrand()) {
        true -> TurboModeToolbarContent(
            imageId = R.drawable.etp_selector,
            enabledTextId = R.string.toolbar_etp_on,
            disabledTextId = R.string.toolbar_etp_off
        )
        false -> TurboModeToolbarContent(
            imageId = R.drawable.turbo_selector,
            enabledTextId = R.string.turbo_mode,
            disabledTextId = R.string.turbo_mode
        )
    }

    data class TurboModeOnboardingContent(
        val titleId: Int,
        val descriptionId: Int,
        val enableButtonTextId: Int,
        val disableButtonTextId: Int,
        val imageId: Int,
        val imageContentDescriptionId: Int
    )

    fun getTurboModeOnboarding() = when (shouldUseTurboRebrand()) {
        true -> TurboModeOnboardingContent(
            titleId = R.string.onboarding_etp_title,
            descriptionId = R.string.onboarding_etp_description,
            enableButtonTextId = R.string.onboarding_etp_enable,
            disableButtonTextId = R.string.onboarding_etp_disable2,
            imageId = R.drawable.etp_onboarding,
            imageContentDescriptionId = R.string.onboarding_etp_image_a11y
        )
        false -> TurboModeOnboardingContent(
            titleId = R.string.onboarding_turbo_mode_title,
            descriptionId = R.string.onboarding_turbo_mode_body2,
            enableButtonTextId = R.string.button_turbo_mode_keep_enabled2,
            disableButtonTextId = R.string.button_turbo_mode_turn_off2,
            imageId = R.drawable.turbo_mode_onboarding,
            imageContentDescriptionId = R.string.turbo_mode_image_a11y
        )
    }
}
