package se.diimperio.guardians

import Alarm.AlarmFragment
import Contacts.ContactsFragment
import Settings.SettingsFragment
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView




class MainActivity() : AppCompatActivity() {

    lateinit var bottomNav:BottomNavigationView

    lateinit var toolbar:androidx.appcompat.widget.Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.activity_toolbar)

        bottomNav = findViewById(R.id.bottom_navigation)

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.alarm_menu_item -> {
                    loadFragment(AlarmFragment())
                    true
                }
                R.id.contacts_menu_item -> {
                    loadFragment(ContactsFragment())
                    true
                }
                R.id.settings_menu_item -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
        bottomNav.setSelectedItemId(bottomNav.getMenu().getItem(1).getItemId());

    }
    fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container_layout, fragment).commit()
    }
    fun showBottomNav() {
        bottomNav.visibility = VISIBLE
    }
    fun hideBottomNav() {
        bottomNav.visibility = GONE
    }

/*


 */
}
