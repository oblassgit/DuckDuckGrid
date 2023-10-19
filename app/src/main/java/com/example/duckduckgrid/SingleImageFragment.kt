package com.example.duckduckgrid

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
    private val sharedPref: SharedPreferences get() = requireActivity().getPreferences(Context.MODE_PRIVATE)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSingleImageBinding.inflate(inflater, container, false)

        val imgUrl = args.imgUrl
        val date = args.date


        var isStarred = sharedPref.getBoolean(imgUrl, false)
        if (isStarred) {
            binding.starBtn.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            binding.starBtn.setImageResource(android.R.drawable.btn_star_big_off)
        }


        Log.d("DateAndUrl", "$imgUrl $date")
        binding.dateTxt.text = date

        Glide.with(binding.imageView)
            .load(imgUrl)
            .into(binding.imageView)

        binding.starBtn.setOnClickListener {
            if (isStarred) {
                binding.starBtn.setImageResource(android.R.drawable.btn_star_big_off)
                isStarred = false
                saveStarred(isStarred, imgUrl)

            } else {
                view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                binding.starBtn.setImageResource(android.R.drawable.btn_star_big_on)
                isStarred = true
                saveStarred(isStarred, imgUrl)
            }
        }

        return binding.root

    }

    private fun saveStarred(isStarred: Boolean, imgUrl: String) {
        sharedPref
        with(sharedPref.edit()) {
            putBoolean(imgUrl, isStarred)
            commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}