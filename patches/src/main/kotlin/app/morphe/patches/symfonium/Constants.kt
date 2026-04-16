package app.morphe.patches.symfonium

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_SYMFONIUM =
        Compatibility(
            name = "Symfonium",
            packageName = "app.symfonik.music.player",
            appIconColor = 0x1DB9D4,
            targets =
                listOf(
                    AppTarget(version = "14.0.0"),
                ),
        )
}
