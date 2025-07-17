package com.librarecipes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val executor = Executors.newSingleThreadExecutor()
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vérifier et demander les permissions
        if (checkPermissions()) {
            initializeApp()
        } else {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeApp()
            } else {
                // Gérer le cas où les permissions sont refusées
                finish()
            }
        }
    }

    private fun initializeApp() {
        setupWebView()
        startPythonServer()
    }

    private fun setupWebView() {
        webView = findViewById(R.id.webview)

        // Configuration WebView pour Streamlit
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = WebViewClient()
    }

    private fun startPythonServer() {
        executor.execute {
            try {
                // Initialiser Python si pas déjà fait
                if (!Python.isStarted()) {
                    Python.start(AndroidPlatform(this))
                }

                val python = Python.getInstance()

                // Configurer le stockage Android pour Python
                val androidStorage = filesDir.absolutePath
                System.setProperty("ANDROID_STORAGE", androidStorage)

                // Démarrer le serveur Streamlit via le bridge Python
                val bridge = python.getModule("android_bridge")
                val serverUrl = bridge.callAttr("start_streamlit_server").toString()

                // Charger l'application Streamlit dans la WebView
                runOnUiThread {
                    webView.loadUrl(serverUrl)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // En cas d'erreur, charger une page d'erreur
                runOnUiThread {
                    webView.loadData(
                        "<html><body><h1>Erreur de démarrage</h1><p>${e.message}</p></body></html>",
                        "text/html",
                        "UTF-8"
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
