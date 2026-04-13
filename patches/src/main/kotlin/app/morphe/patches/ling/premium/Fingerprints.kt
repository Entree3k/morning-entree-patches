package app.morphe.patches.ling.premium

import app.morphe.patcher.Fingerprint

object GetAvailableItemsByTypeFingerprint : Fingerprint(
    definingClass = "Lcom/dooboolab/rniap/RNIapModule;",
    name = "getAvailableItemsByType",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Lcom/facebook/react/bridge/Promise;"),
)
