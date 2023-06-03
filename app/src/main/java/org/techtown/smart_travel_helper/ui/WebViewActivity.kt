package org.techtown.smart_travel_helper.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import org.techtown.smart_travel_helper.R

class WebViewActivity : AppCompatActivity() {
    lateinit var webView: WebView
    lateinit var button: ExtendedFloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_web_view)

        webView = findViewById(R.id.webView)
        button = findViewById(R.id.btn)


        with(webView){
            // 웹뷰 설정
            settings.run {
                javaScriptEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
                domStorageEnabled = true
                useWideViewPort =true
                loadWithOverviewMode =true
                if(Build.VERSION.SDK_INT >= 21) {
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;
                }
                setSupportMultipleWindows(true)
            }
            scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            loadUrl("https://somi2978.github.io/SmartTravelerHelper/")
        }


        button.setOnClickListener { v->
            startActivity(Intent(applicationContext, DrowsinessActicity::class.java))
        }


    }

    override fun onBackPressed() { // 뒤로가기 기능 구현

        webView = findViewById(R.id.webView)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}