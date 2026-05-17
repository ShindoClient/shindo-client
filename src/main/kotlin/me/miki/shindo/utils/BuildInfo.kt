package me.miki.shindo.utils

data class BuildInfo(
    val build: Int,
    val semver: String,
    val buildId: String,
    val type: String
) {

    fun getDisplayString(): String {
        return "Shindo Client v$semver ($buildId $type)"
    }

    companion object {

        /**
         * Default stable build for 1.8.9
         */
        val DEFAULT = BuildInfo(
            build = 5111,
            semver = "5.1.11",
            buildId = "5111.3",
            type = "dev"
        )
    }
}