package se.diimperio.guardians

import Alarm.AlarmFragment
import Alarm.AlertMapsFragment
import Contacts.ContactsFragment
import Login.LoginActivity
import Settings.SettingsFragment
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import models.AlarmManager
import models.UserManager
import models.User


class MainActivity() : AppCompatActivity() {

    lateinit var activeAlertBttn: Button
    lateinit var bottomNav: BottomNavigationView
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        activeAlertBttn = findViewById(R.id.active_alert_button)
        toolbar = findViewById(R.id.activity_toolbar)
        bottomNav = findViewById(R.id.bottom_navigation)

        setupCurrentUser()
        AlarmManager.setupAlarmListener(this)

        activeAlertBttn.setOnClickListener {
            val mapsFragment = AlertMapsFragment()
            loadFragment(mapsFragment)
            activeAlertBttn.visibility = GONE
            toolbar.visibility = GONE
        }

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.alarm_menu_item -> {
                    loadFragment(AlarmFragment())
                    if (AlarmManager.activeAlertsExists()) {
                        activeAlertBttn.visibility = VISIBLE
                    } else {
                        activeAlertBttn.visibility = GONE
                    }
                    true
                }
                R.id.contacts_menu_item -> {
                    loadFragment(ContactsFragment())
                    if (AlarmManager.activeAlertsExists()) {
                        activeAlertBttn.visibility = VISIBLE
                    } else {
                        activeAlertBttn.visibility = GONE
                    }
                    true
                }
                R.id.settings_menu_item -> {
                    loadFragment(SettingsFragment())
                    if (AlarmManager.activeAlertsExists()) {
                        activeAlertBttn.visibility = VISIBLE
                    } else {
                        activeAlertBttn.visibility = GONE
                    }
                    true
                }
                else -> false
            }
        }
        bottomNav.setSelectedItemId(
            bottomNav.getMenu().getItem(1).getItemId()
        ) //Select middle menu ("alarm") on start
    }


    private fun setupCurrentUser() {
        val currentUserId = auth.uid

        if (currentUserId == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            val profileRef = db.collection("users").document("$currentUserId")
            profileRef.addSnapshotListener { snapshot, firebaseFirestoreException ->
                if (snapshot != null) {
                    val user = snapshot.toObject(User::class.java)
                    val userData = user ?: return@addSnapshotListener

                    UserManager.currentUser.uid = userData.uid
                    UserManager.currentUser.email = userData.email
                    UserManager.currentUser.mobilNR = userData.mobilNR
                    UserManager.currentUser.name = userData.name
                    UserManager.currentUser.guardians = userData.guardians
                    UserManager.currentUser.location = userData.location
                }
            }
        }
    }

    fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container_layout, fragment).commit()
    }

    /*
    ****************** Remapping user input keys not working.....
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> Log.d("KEYOVERRIDE", "Vol Up pressed") // Active
            KeyEvent.KEYCODE_VOLUME_DOWN -> Log.d("KEYOVERRIDE", "Vol Down pressed") // Active
            KeyEvent.KEYCODE_VOLUME_MUTE -> Log.d("KEYOVERRIDE", "Vol Mute pressed") // Active
            KeyEvent.KEYCODE_BACK -> Log.d("KEYOVERRIDE", "Back Pressed") //Active
            KeyEvent.KEYCODE_HOME -> Log.d("KEYOVERRIDE","Home pressed")
        }
        return true
    }

    fun checkForActiveAlerts() {
        if (AlarmManager.activeAlertsExists()) {
            activeAlertBttn.visibility = VISIBLE
        } else {
            activeAlertBttn.visibility = GONE
        }
    }
}
