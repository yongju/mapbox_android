package dev.snri.tracking.ui.browse

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import S.N.R.I.tracking.R
import S.N.R.I.tracking.databinding.FragmentCoronaLiveBinding
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.*
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class BrowseFragment : Fragment() {

    private val URL_CORONA_LIVE     = "https://corona-live.com"

    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var binding: FragmentCoronaLiveBinding

    private lateinit var backCallback: OnBackPressedCallback

    private val client = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)

        galleryViewModel =
                ViewModelProvider(this).get(GalleryViewModel::class.java)
        binding = FragmentCoronaLiveBinding.inflate(inflater, container, false)

//        val textView: TextView = root.findViewById(R.id.text_gallery)
//        galleryViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

        binding.web.settings.javaScriptEnabled = true
        binding.web.settings.databaseEnabled = true
        binding.web.settings.domStorageEnabled = true
        binding.web.webViewClient = client

        binding.web.loadUrl("https://corona-live.com")

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if ( binding.web.canGoBack()) {
                    binding.web.goBack()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, backCallback)

    }

    override fun onDetach() {
        super.onDetach()
        backCallback.remove()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.browser, menu)
    }

    @SuppressLint("StringFormatInvalid")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val url = when(item.itemId) {
            R.id.action_corona_live -> URL_CORONA_LIVE
            R.id.action_naver_lg -> getStockUrl(0)
            R.id.action_naver_lg_woo -> getStockUrl(1)
            R.id.action_naver_samsung -> getStockUrl(2)
            R.id.action_naver_samsung_woo -> getStockUrl(3)
            R.id.action_naver_samsung_fire_insurance ->getStockUrl(4)
            R.id.action_naver_samsung_fire_insurance_woo ->getStockUrl(5)
            R.id.action_naver_sk_tel ->getStockUrl(6)
            R.id.action_naver_lg_es ->getStockUrl(7)
            else -> URL_CORONA_LIVE
        }

        binding.web.loadUrl(url)
        val act = activity
        if (act is AppCompatActivity) {
            val uri = Uri.parse(url)
            act.supportActionBar!!.title = uri.host

        }
        return super.onOptionsItemSelected(item)
    }

    private fun getStockUrl(index: Int): String {
        val code = resources.getStringArray(R.array.stock_numbers)[index]
        return getString(R.string.url_stock, code)
    }
}