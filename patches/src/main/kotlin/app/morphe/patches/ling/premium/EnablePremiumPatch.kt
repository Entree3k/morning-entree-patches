package app.morphe.patches.ling.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.morphe.patches.ling.premium.LingConstants.COMPATIBILITY_LING

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Enables app features locked behind the subscription paywall.",
) {
    compatibleWith(COMPATIBILITY_LING)

    execute {
        GetAvailableItemsByTypeFingerprint.method.apply {
            addInstructionsWithLabels(
                0,
                """
                    const-string v0, "subs"
                    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :original

                    invoke-static {}, Lcom/facebook/react/bridge/Arguments;->createArray()Lcom/facebook/react/bridge/WritableArray;
                    move-result-object v0

                    invoke-static {}, Lcom/facebook/react/bridge/Arguments;->createMap()Lcom/facebook/react/bridge/WritableMap;
                    move-result-object v1

                    const-string v2, "productId"
                    const-string v3, "ling_premium_yearly"
                    invoke-interface {v1, v2, v3}, Lcom/facebook/react/bridge/WritableMap;->putString(Ljava/lang/String;Ljava/lang/String;)V

                    invoke-static {}, Lcom/facebook/react/bridge/Arguments;->createArray()Lcom/facebook/react/bridge/WritableArray;
                    move-result-object v2
                    const-string v3, "ling_premium_yearly"
                    invoke-interface {v2, v3}, Lcom/facebook/react/bridge/WritableArray;->pushString(Ljava/lang/String;)V
                    const-string v3, "productIds"
                    check-cast v2, Lcom/facebook/react/bridge/ReadableArray;
                    invoke-interface {v1, v3, v2}, Lcom/facebook/react/bridge/WritableMap;->putArray(Ljava/lang/String;Lcom/facebook/react/bridge/ReadableArray;)V

                    const-string v2, "transactionId"
                    const-string v3, "GPA.0000-0000-0000-00000"
                    invoke-interface {v1, v2, v3}, Lcom/facebook/react/bridge/WritableMap;->putString(Ljava/lang/String;Ljava/lang/String;)V

                    const-string v2, "transactionDate"
                    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
                    move-result-wide v3
                    long-to-double v3, v3
                    invoke-interface {v1, v2, v3, v4}, Lcom/facebook/react/bridge/WritableMap;->putDouble(Ljava/lang/String;D)V

                    const-string v2, "transactionReceipt"
                    const-string v3, "{\"orderId\":\"GPA.0000-0000-0000-00000\",\"packageName\":\"com.simyasolutions.ling.universal\",\"productId\":\"ling_premium_yearly\",\"purchaseTime\":0,\"purchaseState\":0,\"purchaseToken\":\"morphe-premium-token\",\"acknowledged\":true}"
                    invoke-interface {v1, v2, v3}, Lcom/facebook/react/bridge/WritableMap;->putString(Ljava/lang/String;Ljava/lang/String;)V

                    const-string v2, "orderId"
                    const-string v3, "GPA.0000-0000-0000-00000"
                    invoke-interface {v1, v2, v3}, Lcom/facebook/react/bridge/WritableMap;->putString(Ljava/lang/String;Ljava/lang/String;)V

                    const-string v2, "purchaseToken"
                    const-string v3, "morphe-premium-token"
                    invoke-interface {v1, v2, v3}, Lcom/facebook/react/bridge/WritableMap;->putString(Ljava/lang/String;Ljava/lang/String;)V

                    const-string v2, "purchaseStateAndroid"
                    const/4 v3, 0x1
                    invoke-interface {v1, v2, v3}, Lcom/facebook/react/bridge/WritableMap;->putInt(Ljava/lang/String;I)V

                    const-string v2, "isAcknowledgedAndroid"
                    const/4 v3, 0x1
                    invoke-interface {v1, v2, v3}, Lcom/facebook/react/bridge/WritableMap;->putBoolean(Ljava/lang/String;Z)V

                    const-string v2, "autoRenewingAndroid"
                    const/4 v3, 0x1
                    invoke-interface {v1, v2, v3}, Lcom/facebook/react/bridge/WritableMap;->putBoolean(Ljava/lang/String;Z)V

                    check-cast v1, Lcom/facebook/react/bridge/ReadableMap;
                    invoke-interface {v0, v1}, Lcom/facebook/react/bridge/WritableArray;->pushMap(Lcom/facebook/react/bridge/ReadableMap;)V

                    invoke-interface {p2, v0}, Lcom/facebook/react/bridge/Promise;->resolve(Ljava/lang/Object;)V
                    return-void
                """.trimIndent(),
                ExternalLabel("original", getInstruction(0)),
            )
        }
    }
}
