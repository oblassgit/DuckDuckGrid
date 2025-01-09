package com.example.duckduckgrid

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.duckduckgrid.databinding.ActivityMainBinding
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var counter = 0
    private lateinit var showcaseView: ShowcaseView

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Setup the bottom navigation view with navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNavigationView.setupWithNavController(navController)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.grid, R.id.liked, R.id.add_image)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        val lps = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        lps.bottomMargin = ((resources.displayMetrics.density * 60) as Number).toInt()

        showcaseView = ShowcaseView.Builder(this)
            .setTarget(ViewTarget(findViewById(R.id.add_duck_btn)))
            .withMaterialShowcase()
            .blockAllTouches()
            .setOnClickListener(this)
            .setStyle(R.style.CustomShowcaseViewTheme)
            .setContentTitle("Add Ducks")
            .singleShot(42)
            .build()
        showcaseView.setButtonPosition(lps)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onClick(v: View?) {
        when (counter) {
            0 -> {
                showcaseView.setShowcase(ViewTarget(findViewById(R.id.starImgBtn)), true)
                showcaseView.setContentTitle("Like Ducks")
            }

            1 -> {
                showcaseView.setShowcase(ViewTarget(findViewById(R.id.liked)), true)
                showcaseView.setContentTitle("View Liked Ducks")
            }
            2 -> {
                showcaseView.setShowcase(ViewTarget(findViewById(R.id.imgView)), true)
                showcaseView.setContentTitle("Click on Ducks")
                showcaseView.setButtonText("Done")
            }
            3 -> {
                showcaseView.hide()
            }
        }
        counter++
    }

}