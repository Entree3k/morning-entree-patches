package app.morphe.patches.symfonium

import app.morphe.patcher.Fingerprint

object Fingerprints {
    // ab3 — the license manager class (called "FirebaseManager" in earlier notes,
    // though it has no relation to Firebase; it owns the license token MutableStateFlow G,
    // the premium-state reference x:Ls65, the beta expiry int A, and all license methods).
    val FirebaseManagerFingerprint = Fingerprint(definingClass = "Lab3;")

    // ab3.m()Z — the main license gate. Returns true when G == 42L. Called from 40+ sites.
    val IsPremiumMethodFingerprint =
        Fingerprint(definingClass = "Lab3;", name = "m", returnType = "Z")

    // s65 — the premium-state holder referenced by ab3.x. Field s65.y:Z is the premium flag.
    val PremiumStateClassFingerprint = Fingerprint(definingClass = "Ls65;")
}
