package com.example.duckduckgrid

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.duckduckgrid.databinding.FragmentSingleImageBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SingleImageFragment : Fragment() {

    private var _binding: FragmentSingleImageBinding? = null
    private val args: SingleImageFragmentArgs by navArgs<SingleImageFragmentArgs>()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSingleImageBinding.inflate(inflater, container, false)
        val imgUrl = args.imgUrl
        val date = args.date

        Log.d("DateAndUrl", "$imgUrl $date")
        binding.dateTxt.text = date

        Glide.with(binding.imageView)
            .load(imgUrl)
            .into(binding.imageView)

        return binding.root

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}