package S.N.R.I.tracking.ui.corona

import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import S.N.R.I.tracking.R
import S.N.R.I.tracking.databinding.FragmentCoronaLiveBinding
import android.view.*

class BrowserFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var binding: FragmentCoronaLiveBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
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
        binding.web.loadUrl("https://corona-live.com")

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

    }
}