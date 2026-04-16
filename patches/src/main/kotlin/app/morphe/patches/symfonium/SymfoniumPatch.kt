package app.morphe.patches.symfonium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.symfonium.Constants.COMPATIBILITY_SYMFONIUM
import app.morphe.patches.symfonium.Fingerprints.FirebaseManagerFingerprint
import app.morphe.patches.symfonium.Fingerprints.IsPremiumMethodFingerprint

// Unlocks Symfonium v14.0.0 without relying on the native library or signature spoofing.
//
// The app's license manager (ab3) relies on libsymfonik.so to verify the APK signature
// and write 42L into MutableStateFlow G via ab3.p(J)V. After re-signing the APK the native
// lib never fires, so G stays at Long(-1), m()Z returns false, and every licensed code
// path is blocked. Signature spoofing approaches (PackageManager proxy in Application.onCreate())
// are unreliable — the native lib can check signatures before onCreate() runs, or via direct
// JNI bypassing the Java PackageManager entirely.
//
// This patch instead makes ab3 self-sufficient: it reports "licensed" from construction,
// with every trial-related helper neutralised so no UI path can produce an expired state.
@Suppress("unused")
val symfoniumUnlockPatch =
    bytecodePatch(
        name = "Unlock Symfonium",
        description = "Unlocks the trial version of Symfonium.",
        default = true,
    ) {
        compatibleWith(COMPATIBILITY_SYMFONIUM)

        execute {
            val firebaseManagerClass = classDefBy(FirebaseManagerFingerprint.definingClass!!)
            val mutableFirebaseManager =
                FirebaseManagerFingerprint.match(firebaseManagerClass).classDef

            val isPremiumMethod =
                IsPremiumMethodFingerprint.match(mutableFirebaseManager).method

            // 1. ab3.m()Z — main license gate.
            //    Also sets the separate premium-state flag s65.y (reachable via ab3.x),
            //    then returns true. Setting s65.y ensures every check that reads the
            //    PremiumState object directly (not through m()Z) also sees "premium".
            isPremiumMethod.apply {
                addInstructions(
                    0,
                    """
                const/4 v0, 0x1
                iget-object v1, p0, Lab3;->x:Ls65;
                iput-boolean v0, v1, Ls65;->y:Z
                return v0
            """.trimIndent(),
                )
            }

            // 2. ab3.e()I — beta expiry gate.
            //    Returns 0 unconditionally so nd5 (playback gate), gy5, o9, sj9, vw0
            //    and ab3.l() all see "not expired". Hides the "beta version expired" screen.
            mutableFirebaseManager.methods
                .firstOrNull { it.name == "e" && it.returnType == "I" }
                ?.apply { addInstructions(0, "const/4 v0, 0x0\nreturn v0") }

            // 3. ab3.p(J)V — license token setter.
            //    Normally the native lib calls this with 42L after signature verification.
            //    Overwrite the body so it ALWAYS writes 42L regardless of the argument —
            //    this guarantees G becomes 42L whenever any code path invokes p().
            mutableFirebaseManager.methods
                .firstOrNull {
                    it.name == "p" &&
                        it.parameterTypes.size == 1 &&
                        it.parameterTypes[0] == "J"
                }
                ?.apply {
                    addInstructions(
                        0,
                        """
                const-wide/16 p1, 0x2a
                invoke-static {p1, p2}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
                move-result-object p1
                iget-object p0, p0, Lab3;->G:Lgk9;
                const/4 p2, 0x0
                invoke-virtual {p0, p2, p1}, Lgk9;->n(Ljava/lang/Object;Ljava/lang/Object;)Z
                return-void
            """.trimIndent(),
                    )
                }

            // 4. ab3.r(Lpi1;)Ljava/io/Serializable; — produces trial expiration date text
            //    for the home banner. Returning null suppresses the banner's expiry string.
            mutableFirebaseManager.methods
                .firstOrNull {
                    it.name == "r" &&
                        it.parameterTypes.size == 1 &&
                        it.parameterTypes[0] == "Lpi1;"
                }
                ?.apply {
                    addInstructions(
                        0,
                        """
                const/4 v0, 0x0
                return-object v0
            """.trimIndent(),
                    )
                }

            // 5. ab3.j(Lpi1;) and ab3.k(Lpi1;) — boolean helpers that feed trial UI cards.
            //    Return Boolean.FALSE so the trial cards never render.
            mutableFirebaseManager.methods
                .filter {
                    (it.name == "j" || it.name == "k") &&
                        it.parameterTypes.size == 1 &&
                        it.parameterTypes[0] == "Lpi1;"
                }
                .forEach { method ->
                    method.addInstructions(
                        0,
                        """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
                    )
                }

            // 6. ab3.<init> — constructor.
            //    Replace the "const-wide/16 p1, -0x1" that seeds G with Long(-1) so it
            //    instead seeds G with 42L. This makes m()Z return true from construction
            //    even before the safety net above runs, and avoids any race between
            //    initialization and the first m()Z caller.
            mutableFirebaseManager.methods.firstOrNull { it.name == "<init>" }?.apply {
                implementation?.apply {
                    instructions.forEachIndexed { index, instruction ->
                        if (instruction.opcode.name.contains("const-wide") &&
                            instruction.toString().contains("-1")
                        ) {
                            removeInstructions(index, 1)
                            addInstructions(index, "const-wide/16 p1, 0x2a")
                        }
                    }
                }
            }
        }
    }
