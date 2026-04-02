package com.tools.il2fusion.core.i18n

import androidx.annotation.StringRes
import com.tools.il2fusion.R
import java.util.Locale

enum class AppLanguage(
    val languageTag: String,
    @StringRes val labelRes: Int
) {
    English("en", R.string.language_english),
    SimplifiedChinese("zh-CN", R.string.language_simplified_chinese);

    companion object {
        fun fromLanguageTag(value: String?): AppLanguage {
            return entries.firstOrNull { it.languageTag.equals(value, ignoreCase = true) }
                ?: English
        }

        fun fromLocale(locale: Locale): AppLanguage {
            return if (locale.language.equals("zh", ignoreCase = true)) {
                SimplifiedChinese
            } else {
                English
            }
        }
    }
}
