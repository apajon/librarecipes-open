package com.librarecipes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.webkit.WebResourceRequest
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var loadingLayout: LinearLayout
    private lateinit var errorLayout: LinearLayout
    private lateinit var errorMessage: TextView
    private lateinit var retryButton: Button
    private lateinit var loadingText: TextView

    private val executor = Executors.newSingleThreadExecutor()
    private val PERMISSION_REQUEST_CODE = 100
    private val TAG = "LibraRecipes"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()

        Log.d(TAG, "MainActivity créée, vérification des permissions...")

        // Vérifier et demander les permissions
        if (checkPermissions()) {
            initializeApp()
        } else {
            requestPermissions()
        }
    }

    private fun initializeViews() {
        webView = findViewById(R.id.webview)
        loadingLayout = findViewById(R.id.loading_layout)
        errorLayout = findViewById(R.id.error_layout)
        errorMessage = findViewById(R.id.error_message)
        retryButton = findViewById(R.id.retry_button)
        loadingText = findViewById(R.id.loading_text)

        // Configuration du bouton retry
        retryButton.setOnClickListener {
            showLoading()
            initializeApp()
        }
    }

    private fun showLoading() {
        loadingLayout.visibility = View.VISIBLE
        webView.visibility = View.GONE
        errorLayout.visibility = View.GONE
    }

    private fun showWebView() {
        loadingLayout.visibility = View.GONE
        webView.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
    }

    private fun showError(message: String) {
        loadingLayout.visibility = View.GONE
        webView.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        errorMessage.text = message
    }

    private fun updateLoadingText(message: String) {
        runOnUiThread {
            loadingText.text = message
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
        Log.d(TAG, "Demande de permissions...")
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
                Log.d(TAG, "Permissions accordées")
                initializeApp()
            } else {
                Log.e(TAG, "Permissions refusées")
                showError(getString(R.string.permission_denied_message))
            }
        }
    }

    private fun initializeApp() {
        Log.d(TAG, "Initialisation de l'application...")
        showLoading()
        updateLoadingText("Configuration de l'interface...")
        setupWebView()
        updateLoadingText("Démarrage du serveur Python...")
        startPythonServer()
    }

    private fun setupWebView() {
        Log.d(TAG, "Configuration de la WebView...")

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

            // Optimisations pour mobile
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true

            // Désactiver le cache pour éviter les problèmes de développement
            setAppCacheEnabled(false)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false // Laisse la WebView gérer toutes les URLs
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "Page chargée: $url")
                showWebView()
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e(TAG, "Erreur WebView: $description")
                showError("Erreur de chargement: $description")
            }
        }
    }

    private fun startPythonServer() {
        executor.execute {
            try {
                Log.d(TAG, "Démarrage du serveur Python...")

                runOnUiThread {
                    updateLoadingText("Initialisation de Python...")
                }

                // Initialiser Python si pas déjà fait
                if (!Python.isStarted()) {
                    Log.d(TAG, "Initialisation de Python...")
                    Python.start(AndroidPlatform(this))
                }

                val python = Python.getInstance()

                runOnUiThread {
                    updateLoadingText("Configuration du stockage...")
                }

                // Configurer le stockage Android pour Python
                val androidStorage = filesDir.absolutePath
                System.setProperty("ANDROID_STORAGE", androidStorage)
                Log.d(TAG, "Stockage Android configuré: $androidStorage")

                runOnUiThread {
                    updateLoadingText("Démarrage de Streamlit...")
                }

                // Démarrer le serveur Streamlit via le bridge Python
                val bridge = python.getModule("android_bridge")
                val serverUrl = bridge.callAttr("start_streamlit_server").toString()

                Log.d(TAG, "Serveur démarré sur: $serverUrl")

                // Charger l'application Streamlit dans la WebView
                runOnUiThread {
                    updateLoadingText("Chargement de l'application...")
                    webView.loadUrl(serverUrl)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors du démarrage du serveur Python", e)

                // En cas d'erreur, afficher une page d'erreur détaillée
                runOnUiThread {
                    val errorMsg = when {
                        e.message?.contains("android_bridge") == true ->
                            "Module android_bridge non trouvé. Vérifiez l'installation."
                        e.message?.contains("Python") == true ->
                            "Erreur d'initialisation Python: ${e.message}"
                        else ->
                            "Erreur de démarrage: ${e.message}"
                    }
                    showError(errorMsg)
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

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }
}
