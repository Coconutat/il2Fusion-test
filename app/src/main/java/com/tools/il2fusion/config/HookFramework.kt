package com.tools.il2fusion.config

import androidx.annotation.StringRes
import com.tools.il2fusion.R

enum class HookFramework(
    val storageValue: String,
    @StringRes val displayNameRes: Int
) {
    And64InlineHook("and64_inline_hook", R.string.hook_framework_and64),
    Dobby("dobby", R.string.hook_framework_dobby);

    companion object {
        fun fromStorageValue(value: String?): HookFramework {
            return entries.firstOrNull { it.storageValue.equals(value, ignoreCase = true) }
                ?: And64InlineHook
        }
    }
}
