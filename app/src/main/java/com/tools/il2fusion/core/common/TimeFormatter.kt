package com.tools.il2fusion.core.common

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeFormatter {
    fun formatIsoDateTime(value: String): String {
        return runCatching {
            val instant = Instant.parse(value)
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(instant)
        }.getOrElse { value }
    }
}
