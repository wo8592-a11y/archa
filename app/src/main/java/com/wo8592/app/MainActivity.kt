package com.wo8592.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.wo8592.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // 메인 웹사이트 주소
    private val targetUrl = "https://wo8592-a11y.github.io/wo8592/"
    private val targetHost = "wo8592-a11y.github.io"

    // 뒤로가기 종료 타이머 변수
    private var backPressedTime: Long = 0
    private val backWaitTime: Long = 2000

    // 파일/사진 첨부 콜백
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var fileChooserLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 세련된 전체화면 Edge-to-Edge 구성
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상태바 및 내비게이션바 패딩 안전영역 적용
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        initFileChooserLauncher()
        setupWebView()
        setupSwipeRefresh()
        setupErrorHandling()
        setupBackPressHandler()

        // 최초 웹 페이지 로드
        if (savedInstanceState == null) {
            loadWebsite(targetUrl)
        } else {
            binding.webView.restoreState(savedInstanceState)
        }
    }

    /**
     * 파일/사진 업로드 런처 초기화
     */
    private fun initFileChooserLauncher() {
        fileChooserLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (fileUploadCallback == null) return@registerForActivityResult

            val results: Array<Uri>? = if (result.resultCode == RESULT_OK) {
                val data = result.data
                when {
                    data?.clipData != null -> {
                        val count = data.clipData!!.itemCount
                        Array(count) { i -> data.clipData!!.getItemAt(i).uri }
                    }
                    data?.data != null -> {
                        arrayOf(data.data!!)
                    }
                    else -> null
                }
            } else {
                null
            }

            fileUploadCallback?.onReceiveValue(results)
            fileUploadCallback = null
        }
    }

    /**
     * WebView 환경 설정
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                allowFileAccess = true
                allowContentAccess = true
                cacheMode = WebSettings.LOAD_DEFAULT
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                
                // 앱 식별을 위한 커스텀 User-Agent 설정
                userAgentString = "$userAgentString Wo8592App/1.0"
            }

            // 웹뷰 클라이언트 설정
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false
                    val uri = Uri.parse(url)

                    // 외부 커스텀 스킴(tel, mailto, intent 등) 처리
                    val scheme = uri.scheme
                    if (scheme != null && scheme != "http" && scheme != "https") {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)
                            return true
                        } catch (e: Exception) {
                            return true
                        }
                    }

                    // 대상 도메인 외의 외부 웹사이트 링크는 외부 브라우저로 열기
                    val host = uri.host
                    if (host != null && !host.contains(targetHost)) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)
                            return true
                        } catch (e: ActivityNotFoundException) {
                            // 외부 브라우저가 없는 경우 웹뷰에서 로드
                        }
                    }

                    return false
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                    showWebView()
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    // 메인 프레임 에러일 때 에러 화면 표시
                    if (request?.isForMainFrame == true) {
                        showErrorLayout()
                    }
                }
            }

            // 웹 크롬 클라이언트 설정 (진행바, 팝업, 파일 업로드 지원)
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    binding.progressBar.progress = newProgress
                    if (newProgress >= 100) {
                        binding.progressBar.visibility = View.GONE
                    } else {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }

                // 게시판 사진/파일 첨부 대응
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    fileUploadCallback?.onReceiveValue(null)
                    fileUploadCallback = filePathCallback

                    val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                    }

                    try {
                        fileChooserLauncher.launch(intent)
                    } catch (e: ActivityNotFoundException) {
                        fileUploadCallback = null
                        Toast.makeText(this@MainActivity, "파일 선택기를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                        return false
                    }
                    return true
                }

                // JS alert 다이얼로그 커스텀
                override fun onJsAlert(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: android.webkit.JsResult?
                ): Boolean {
                    AlertDialog.Builder(this@MainActivity)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                        .setOnCancelListener { result?.cancel() }
                        .create()
                        .show()
                    return true
                }

                // JS confirm 다이얼로그 커스텀
                override fun onJsConfirm(
                    view: WebView?,
                    url: String?,
                    message: String?,
                    result: android.webkit.JsResult?
                ): Boolean {
                    AlertDialog.Builder(this@MainActivity)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> result?.cancel() }
                        .setOnCancelListener { result?.cancel() }
                        .create()
                        .show()
                    return true
                }
            }
        }
    }

    /**
     * Swipe to Refresh 설정
     */
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary,
            R.color.secondary,
            R.color.accent
        )

        // 최상단에서만 당겨서 새로고침 트리거
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.webView.reload()
        }

        binding.webView.viewTreeObserver.addOnScrollChangedListener {
            binding.swipeRefreshLayout.isEnabled = (binding.webView.scrollY == 0)
        }
    }

    /**
     * 네트워크 에러 재시도 버튼 설정
     */
    private fun setupErrorHandling() {
        binding.btnRetry.setOnClickListener {
            if (isNetworkAvailable()) {
                showWebView()
                binding.webView.reload()
            } else {
                Toast.makeText(this, R.string.network_error_desc, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 뒤로가기 버튼 처리: 웹 뒤로가기 지원 및 2번 누르면 종료
     */
    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - backPressedTime < backWaitTime) {
                        finish()
                    } else {
                        backPressedTime = currentTime
                        Toast.makeText(this@MainActivity, R.string.exit_confirm, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun loadWebsite(url: String) {
        if (isNetworkAvailable()) {
            showWebView()
            binding.webView.loadUrl(url)
        } else {
            showErrorLayout()
        }
    }

    private fun showWebView() {
        binding.layoutError.visibility = View.GONE
        binding.webView.visibility = View.VISIBLE
    }

    private fun showErrorLayout() {
        binding.webView.visibility = View.GONE
        binding.layoutError.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false
    }

    /**
     * 인터넷 연결 상태 체크
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }
}
