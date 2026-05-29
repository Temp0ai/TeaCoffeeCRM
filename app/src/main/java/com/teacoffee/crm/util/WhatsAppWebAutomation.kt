package com.teacoffee.crm.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.webkit.JavascriptInterface
import com.teacoffee.crm.data.local.entity.LeadEntity
import com.teacoffee.crm.data.local.entity.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhatsAppWebAutomation @Inject constructor() {

    private var isInitialized = false
    private var qrCodeUrl: String? = null
    private var authState: AuthState = AuthState.DISCONNECTED

    enum class AuthState {
        DISCONNECTED, SCANNING, CONNECTED, ERROR
    }

    fun getAuthState(): AuthState = authState

    fun getQrCodeUrl(): String? = qrCodeUrl

    fun initializeWebView(webView: WebView) {
        webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36"
            settings.cacheMode = WebSettings.LOAD_DEFAULT

            addJavascriptInterface(object {
                @JavascriptInterface
                fun onQrCode(url: String) {
                    qrCodeUrl = url
                    authState = AuthState.SCANNING
                }

                @JavascriptInterface
                fun onAuthenticated() {
                    authState = AuthState.CONNECTED
                    isInitialized = true
                }

                @JavascriptInterface
                fun onError(error: String) {
                    authState = AuthState.ERROR
                }
            }, "Android")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    if (url.contains("web.whatsapp.com")) {
                        injectScript(view)
                    }
                }
            }

            loadUrl("https://web.whatsapp.com")
        }
    }

    private fun injectScript(webView: WebView) {
        val script = """
            (function() {
                // Monitor QR code
                const observer = new MutationObserver(() => {
                    const qrCode = document.querySelector('canvas[aria-label]');
                    if (qrCode) {
                        Android.onQrCode(qrCode.toDataURL());
                    }
                    const chatList = document.querySelector('[data-testid="chat-list"]');
                    if (chatList) {
                        Android.onAuthenticated();
                        observer.disconnect();
                    }
                });
                observer.observe(document.body, { childList: true, subtree: true });
                
                // Error handling
                window.addEventListener('error', function(e) {
                    Android.onError(e.message);
                });
            })();
        """.trimIndent()

        webView.evaluateJavascript(script, null)
    }

    suspend fun sendMessage(
        lead: LeadEntity,
        messageContent: String,
        webView: WebView
    ): Boolean {
        return withContext(Dispatchers.Main) {
            try {
                val phoneNumber = lead.phone.replace(Regex("[^0-9]"), "")
                if (phoneNumber.length < 10) return@withContext false

                val waIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/$phoneNumber?text=${Uri.encode(messageContent)}")
                    `package` = "com.whatsapp"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                if (waIntent.resolveActivity(webView.context.packageManager) != null) {
                    webView.context.startActivity(waIntent)
                    true
                } else {
                    // Fallback to WhatsApp Web in browser
                    val webIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://web.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(messageContent)}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    webView.context.startActivity(webIntent)
                    true
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun sendBulkMessages(
        leads: List<LeadEntity>,
        messageContent: String,
        webView: WebView,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): BulkSendResult {
        var sent = 0
        val failed = mutableListOf<String>()

        for ((index, lead) in leads.withIndex()) {
            try {
                val success = sendMessage(lead, messageContent, webView)
                if (success) sent++ else failed.add(lead.phone)
            } catch (e: Exception) {
                failed.add(lead.phone)
            }
            onProgress(index + 1, leads.size)
        }

        return BulkSendResult(sent, failed.size, failed)
    }

    data class BulkSendResult(
        val sentCount: Int,
        val failedCount: Int,
        val failedNumbers: List<String>
    )

    companion object {
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val WHATSAPP_WEB_URL = "https://web.whatsapp.com"
    }
}
