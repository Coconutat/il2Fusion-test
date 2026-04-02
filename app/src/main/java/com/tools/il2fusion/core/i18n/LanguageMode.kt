package com.tools.il2fusion.core.i18n

enum class LanguageMode(
    val storageValue: String
) {
    Auto("auto"),
    Manual("manual");

    companion object {
        fun fromStorageValue(value: String?): LanguageMode {
            return entries.firstOrNull { it.storageValue.equals(value, ignoreCase = true) }
                ?: Auto
        }
    }
}
