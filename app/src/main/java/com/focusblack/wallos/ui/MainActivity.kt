package com.focusblack.wallos.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.focusblack.wallos.R
import com.focusblack.wallos.core.RotationScheduler
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val todayFragment = TodayFragment()
    private val packsFragment = PacksFragment()
    private val widgetsFragment = WidgetsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Schedule daily rotation
        RotationScheduler.scheduleDailyRotation(this)

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Setup bottom navigation
        val nav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_today -> replaceFragment(todayFragment)
                R.id.nav_packs -> replaceFragment(packsFragment)
                R.id.nav_widgets -> replaceFragment(widgetsFragment)
                else -> false
            }
        }

        // Default to Today tab
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
