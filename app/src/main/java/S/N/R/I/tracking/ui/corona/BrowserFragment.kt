package S.N.R.I.tracking.ui.corona

import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import S.N.R.I.tracking.R
import S.N.R.I.tracking.databinding.FragmentCoronaLiveBinding
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

class BrowserFragment : Fragment() {

    private val URL_CORONA_LIVE     = "https://corona-live.com"
    private val URL_NAVER_LG_WOO    = "https://m.stock.naver.com/item/main.nhn#/stocks/003555/total"
    private val URL_NAVER_LG        = "https://m.stock.naver.com/item/main.nhn#/stocks/003550/total"

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

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true);

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        var url = when(item.itemId) {
            R.id.action_corona_live -> URL_CORONA_LIVE
            R.id.action_naver_lg_woo -> URL_NAVER_LG_WOO
            R.id.action_naver_lg -> URL_NAVER_LG
            else -> URL_CORONA_LIVE
        }

        binding.web.loadUrl(url)
        val act = activity
        if (act is AppCompatActivity) {
            val uri = Uri.parse(url);
            act.supportActionBar!!.title = uri.host

        }
        return super.onOptionsItemSelected(item)
    }
}