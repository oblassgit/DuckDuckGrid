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
import android.view.ViewGroup
import androidx.annotation.RequiresApi
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


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}