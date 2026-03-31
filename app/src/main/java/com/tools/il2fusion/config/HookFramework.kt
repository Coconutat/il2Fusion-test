package com.tools.il2fusion.config

enum class HookFramework(
    val storageValue: String,
    val displayName: String
) {
    And64InlineHook("and64_inline_hook", "And64InlineHook"),
    Dobby("dobby", "Dobby Hook");

    companion object {
        fun fromStorageValue(value: String?): HookFramework {
            return entries.firstOrNull { it.storageValue.equals(value, ignoreCase = true) }
                ?: And64InlineHook
        }
    }
}
