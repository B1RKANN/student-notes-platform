package com.birkanboz.knowledgehub.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.birkanboz.knowledgehub.R
import com.birkanboz.knowledgehub.databinding.ActivityBottomNavigateBinding

class BottomNavigateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBottomNavigateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBottomNavigateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        goHome()
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.add -> {
                    replaceFragment(AddFragment())
                    true
                }
                R.id.profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }



    fun goHome() {
        replaceFragment(HomeFragment())
    }



    private fun replaceFragment(fragment: Fragment) {
        if (!isFinishing && !isDestroyed) {
            val fragmentManager = supportFragmentManager
            if (!fragmentManager.isStateSaved) {
                fragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .commit()
            } else {
                fragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .commitAllowingStateLoss()
            }
        }
    }
}
