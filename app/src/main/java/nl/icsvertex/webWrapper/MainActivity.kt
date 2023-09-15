package nl.icsvertex.webWrapper

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    // the website url to render
    private val myWebsiteURL = ""

    // the js function that handles the input
    private val myGlobalJsFunctionForHandlingBarcodeInput: String = "scanFunction"

    // if enabled uses intent for barcode input
    private val datawedgeEnabled: Boolean = true

    // settings for datawedge
    private val barcodeIntentAction: String = "com.symbol.datawedge.api.RESULT_ACTION"
    private val barcodeDataKey: String = "com.symbol.datawedge.data_string"

    // this is initialized from the onCreate
    private lateinit var webView: WebView

    // the barcode receiver object
    private val scanBroadcastReceiver = object : BroadcastReceiver() {
        private val TAG = "ScanBroadcastReceiver"

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(this.TAG, "onReceive called")

            if (intent?.action == barcodeIntentAction) {
                Log.d(this.TAG, "correct action")

                intent.extras?.getString(barcodeDataKey)?.let {
                    Log.d(this.TAG, "barcodeData = $it")

                    sendBarcodeDataToWebView(it)
                }
            }
        }
    }

    // the intent filter for getting the correct intents
    private val barcodeIntentFilter = IntentFilter().apply {
        addAction(barcodeIntentAction)
    }

    // function that calls the javascript function from the global scope
    private fun sendBarcodeDataToWebView(barcodeData: String) {
        webView.evaluateJavascript(
            "javascript: $myGlobalJsFunctionForHandlingBarcodeInput($barcodeData)",
            null
        )
    }

    // gets called on activity/application start
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // keep screen on as long as app on the foreground
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // sets the mainView in the application
        setContentView(R.layout.activity_main)

        // sets the WebView configuration
        webView = findViewById<WebView>(R.id.webView).apply {
            webViewClient = WebViewClient()

            WebView.setWebContentsDebuggingEnabled(true)

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            loadUrl(myWebsiteURL)
        }
    }

    // gets called after pausing the application and when application gets focus back
    override fun onResume() {
        super.onResume()
        if (datawedgeEnabled) {
            this.registerReceiver(scanBroadcastReceiver, barcodeIntentFilter)
        }
    }

    // gets called when application gets pushed back to the backstack
    override fun onPause() {
        super.onPause()
        if (datawedgeEnabled) {
            this.unregisterReceiver(scanBroadcastReceiver)
        }
    }

    // companion object is for static values
    companion object {
        // tag for filtering the logs in logcat
        const val TAG = "MainActivity"
    }
}