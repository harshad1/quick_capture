package com.claw.logger.data

enum class PhotoScale(
    val storageValue: String,
    val label: String,
    val sampleSize: Int,
) {
    FULL("1x", "1x", 1),
    HALF("0.5x", "0.5x", 2),
    QUARTER("0.25x", "0.25x", 4),
    ;

    companion object {
        fun fromStorageValue(value: String?): PhotoScale {
            return entries.firstOrNull { it.storageValue == value } ?: FULL
        }
    }
}
