package app.morphe.patches.ling.premium

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object LingConstants {
    val COMPATIBILITY_LING =
        Compatibility(
            name = "Ling",
            packageName = "com.simyasolutions.ling.universal",
            appIconColor = 0xFFB800,
            targets =
                listOf(
                    AppTarget(version = "8.1.0"),
                ),
        )
}
