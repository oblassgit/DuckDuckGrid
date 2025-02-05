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
                activity?.let { it1 -> CameraPreviewViewModel(it1.application) }?.let { it2 ->
                    CameraPreviewScreen(
                        Modifier, it2,
                        context = it
                    )
                }
            }
        }
        return _binding.root.rootView
    }
}