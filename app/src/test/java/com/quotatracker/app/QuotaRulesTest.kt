package com.quotatracker.app

import com.quotatracker.app.util.QuotaRules
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuotaRulesTest {

    @Test
    fun warningFollowsLatestThresholdAndSwitch() {
        assertTrue(QuotaRules.shouldWarn(80, 100, 80, warningEnabled = true))
        assertFalse(QuotaRules.shouldWarn(79, 100, 80, warningEnabled = true))
        assertFalse(QuotaRules.shouldWarn(100, 100, 80, warningEnabled = false))
    }

    @Test
    fun warningHandlesInvalidQuotaSafely() {
        assertFalse(QuotaRules.shouldWarn(100, 0, 80, warningEnabled = true))
    }
}
