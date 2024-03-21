package com.example.duckduckgrid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.duckduckgrid.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.GridFragment,
                R.id.videoFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        val navHostFragment: Fragment? = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
        var currentFragment: Fragment?

        bottomNav = findViewById(R.id.bottomNav) as BottomNavigationView
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {

                    currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)

                    if (currentFragment !is GridFragment && currentFragment !is SingleImageFragment){
                        navController.navigate(
                            VideoFragmentDirections.actionVideoFragmentToGridFragment()
                        )
                    } else if (currentFragment is SingleImageFragment) {
                        navController.navigate(
                            SingleImageFragmentDirections.actionSecondFragmentToFirstFragment()
                        )
                    }

                    true
                }
                R.id.videos -> {
                    currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)

                    if (currentFragment is GridFragment){
                        navController.navigate(
                            GridFragmentDirections.actionGridFragmentToVideoFragment()
                        )
                    } else if (currentFragment is SingleImageFragment) {
                        navController.navigate(
                            SingleImageFragmentDirections.actionSingleImageFragmentToVideoFragment()
                        )
                    }
                    true
                }

                else -> true
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

}