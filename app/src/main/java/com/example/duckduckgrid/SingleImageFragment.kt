package com.example.duckduckgrid

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.duckduckgrid.databinding.FragmentSingleImageBinding


class SingleImageFragment : Fragment() {

    private var _binding: FragmentSingleImageBinding? = null
    private val args: SingleImageFragmentArgs by navArgs()
    private val sharedPref: SharedPreferences get() = requireActivity().getSharedPreferences("duckduck", Context.MODE_PRIVATE)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("ClickableViewAccessibility", "ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSingleImageBinding.inflate(inflater, container, false)

        val imgUrl = args.imgUrl
        val date = args.date
        val item = args.item


        var favouriteState = if (item.liked) {
            R.attr.state_liked
        } else {
            -R.attr.state_liked
        }
        binding.starBtn.setImageState(
            intArrayOf(favouriteState),
            true
        )



        Log.d("DateAndUrl", "$imgUrl $date")
        binding.dateTxt.text = date
        binding.urlTxt.text = imgUrl

        Glide.with(binding.photoview)
            .load(imgUrl)
            .into(binding.photoview)

        binding.starBtn.setOnClickListener {
            binding.infoPopup.visibility = View.GONE
            favouriteState = if (item.liked) {
                -R.attr.state_liked
            } else {
                R.attr.state_liked
            }
            binding.starBtn.setImageState(
                intArrayOf(favouriteState),
                true
            )
            if (!item.liked) {view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)}

            DuckRepository.setLikedState(item, sharedPref, !item.liked)
        }
        binding.infoBtn.setOnClickListener {
            if (binding.infoPopup.visibility == View.GONE) {
                binding.infoPopup.visibility = View.VISIBLE
            } else {
                binding.infoPopup.visibility = View.GONE
            }
        }
        binding.root.setOnClickListener {
            binding.infoPopup.visibility = View.GONE
        }
        binding.photoview.setOnClickListener {
            binding.infoPopup.visibility = View.GONE
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}