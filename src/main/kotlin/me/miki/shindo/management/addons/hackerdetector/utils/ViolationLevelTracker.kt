package me.miki.shindo.management.addons.hackerdetector.utils

class ViolationLevelTracker(
    private val failedCheckWeight: Int = 0,
    private val successfulCheckWeight: Int = 0,
    private val flagLevel: Int
) {

    private var violationLevel = 0

    constructor(flagLevel: Int) : this(0, 0, flagLevel)

    fun isFlagging(failedCheck: Boolean): Boolean {
        return if (failedCheck) {
            onCheckFail()
        } else {
            onCheckSuccess()
            false
        }
    }

    private fun onCheckSuccess(): Boolean {
        subtract(successfulCheckWeight)
        if (violationLevel < 0) {
            violationLevel = 0
        }
        return false
    }

    private fun onCheckFail(): Boolean {
        add(failedCheckWeight)
        if (violationLevel >= flagLevel) {
            violationLevel = 0
            return true
        }
        return false
    }

    fun add(amount: Int) {
        violationLevel += amount
    }

    fun subtract(amount: Int) {
        violationLevel -= amount
    }

    fun getViolationLevel(): Int = violationLevel
}
