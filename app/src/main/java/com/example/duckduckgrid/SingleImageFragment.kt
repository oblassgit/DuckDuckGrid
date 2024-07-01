package com.example.duckduckgrid

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.duckduckgrid.databinding.FragmentSingleImageBinding
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget


class SingleImageFragment : Fragment(), OnClickListener {

    private lateinit var showcaseView: ShowcaseView
    private var counter = 0

    private var _binding: FragmentSingleImageBinding? = null
    private val args: SingleImageFragmentArgs by navArgs()
    private val sharedPref: SharedPreferences get() = requireActivity().getSharedPreferences("duckduck", Context.MODE_PRIVATE)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("ClickableViewAccessibility", "ResourceType")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSingleImageBinding.inflate(inflater, container, false)

        val imgUrl = args.imgUrl
        val date = args.date
        val item = args.item


        var isStarred = sharedPref.getBoolean(imgUrl, false)
        if (isStarred) {
            binding.starBtnOn.visibility = View.VISIBLE
            binding.starBtnOff.visibility = View.GONE
            item.liked = true
        } else {
            binding.starBtnOn.visibility = View.GONE
            binding.starBtnOff.visibility = View.VISIBLE
            item.liked = false
        }


        Log.d("DateAndUrl", "$imgUrl $date")
        binding.dateTxt.text = date
        binding.urlTxt.text = imgUrl

        Glide.with(binding.photoview)
            .load(imgUrl)
            .into(binding.photoview)

        binding.starBtnOff.setOnClickListener {
            binding.infoPopup.visibility = View.GONE
            binding.starBtnOff.visibility = View.GONE
            binding.starBtnOn.visibility = View.VISIBLE
            DuckRepository.toggleLiked(item, sharedPref)
            isStarred = item.liked
            view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }
        binding.starBtnOn.setOnClickListener {
            binding.infoPopup.visibility = View.GONE
            binding.starBtnOn.visibility = View.GONE
            binding.starBtnOff.visibility = View.VISIBLE
            DuckRepository.toggleLiked(item, sharedPref)
            isStarred = item.liked
        }
        binding.infoBtn.setOnClickListener {
            if (binding.infoPopup.visibility == View.GONE) {
                binding.infoPopup.visibility = View.VISIBLE
            } else {
                binding.infoPopup
            }
        }
        binding.root.setOnClickListener {
            binding.infoPopup.visibility = View.GONE
        }
        binding.photoview.setOnClickListener {
            binding.infoPopup.visibility = View.GONE
        }

        val lps = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        lps.addRule(RelativeLayout.CENTER_HORIZONTAL)
        lps.bottomMargin = ((resources.displayMetrics.density * 60) as Number).toInt()

        showcaseView = ShowcaseView.Builder(requireActivity())
            .setTarget(ViewTarget(binding.photoview))
            .withMaterialShowcase()
            .blockAllTouches()
            .setOnClickListener(this)
            .singleShot(41)
            .setStyle(R.style.CustomShowcaseViewTheme)
            .setContentTitle("Pinch to zoom")
            .build()
        showcaseView.setButtonPosition(lps)


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        showcaseView.hide()
    }

    override fun onClick(v: View?) {
        when (counter) {
            0 -> {
                showcaseView.setShowcase(ViewTarget(binding.starBtnOff), true)
                showcaseView.setContentTitle("Like the duck")
            }

            1 -> {
                showcaseView.setShowcase(ViewTarget(binding.infoBtn), true)
                showcaseView.setContentTitle("Get info about the duck")
                showcaseView.setButtonText("Done")
            }
            2 -> {
                showcaseView.hide()
            }
        }
        counter++
    }

}