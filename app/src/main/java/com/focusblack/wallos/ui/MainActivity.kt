package com.focusblack.wallos.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.focusblack.wallos.R
import androidx.fragment.app.Fragment
import com.focusblack.wallos.core.RotationScheduler

class MainActivity : AppCompatActivity() {

    private val todayFragment = TodayFragment()
    private val packsFragment = PacksFragment()
    private val widgetsFragment = WidgetsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        RotationScheduler.scheduleDailyRotation(this)

        val nav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_today -> replaceFragment(todayFragment)
                R.id.nav_packs -> replaceFragment(packsFragment)
                R.id.nav_widgets -> replaceFragment(widgetsFragment)
                else -> false
            }
        }

        // default
        if (savedInstanceState == null) {
            nav.selectedItemId = R.id.nav_today
        }
    }

    private fun replaceFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
        return true
    }
}
