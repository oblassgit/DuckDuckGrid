package com.example.duckduckgrid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import com.example.duckduckgrid.databinding.FragmentAddImageBinding

class AddImageFragment: Fragment() {
    private lateinit var _binding: FragmentAddImageBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddImageBinding.inflate(inflater, container, false)


        binding.addPhotoComposeView.setContent {
            context?.let {
                CameraPreviewScreen(
                    Modifier, CameraPreviewViewModel(),
                    context = it
                )
            }
        }
        return _binding.root.rootView
    }
}