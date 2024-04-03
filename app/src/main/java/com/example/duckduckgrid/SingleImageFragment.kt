package com.example.duckduckgrid

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

    @SuppressLint("ClickableViewAccessibility")
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
            binding.starBtn.setImageResource(android.R.drawable.btn_star_big_on)
            item.liked = true
        } else {
            binding.starBtn.setImageResource(android.R.drawable.btn_star_big_off)
            item.liked = false
        }


        Log.d("DateAndUrl", "$imgUrl $date")
        binding.dateTxt.text = date

        Glide.with(binding.imageView)
            .load(imgUrl)
            .into(binding.imageView)

        binding.starBtn.setOnClickListener {
            DuckRepository.toggleLiked(item, sharedPref)
            isStarred = item.liked
            binding.starBtn.setImageResource(when (item.liked) {
                true -> android.R.drawable.btn_star_big_on
                false -> android.R.drawable.btn_star_big_off
            })
            if (isStarred) {
                view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            }

        }
        var scaleFactor = 1F
        val initScaleX = binding.imageView.scaleX
        val initScaleY = binding.imageView.scaleY

        val scaleGestureDetector = ScaleGestureDetector(
            requireActivity(),
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    scaleFactor *= detector.scaleFactor
                    scaleFactor = scaleFactor.coerceIn(0.1f, 5.0f)

                    binding.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    binding.imageView.scaleX = scaleFactor
                    binding.imageView.scaleY = scaleFactor

                    return super.onScale(detector)
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    binding.imageView.scaleX = initScaleX
                    binding.imageView.scaleY = initScaleY
                }
            }
        )
        binding.imageView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}