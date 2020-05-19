package se.diimperio.guardians

import Alarm.AlarmFragment
import Contacts.ContactsFragment
import Login.LoginActivity
import Settings.PreferencesSettings
import Settings.SettingsFragment
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.KeyEvent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.beautycoder.pflockscreen.PFFLockScreenConfiguration
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment.OnPFLockScreenCodeCreateListener
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment.OnPFLockScreenLoginListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import models.Alarm
import models.DataStore
import models.User


class MainActivity() : AppCompatActivity() {

    lateinit var bottomNav: BottomNavigationView
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupCurrentUser()

        toolbar = findViewById(R.id.activity_toolbar)

        bottomNav = findViewById(R.id.bottom_navigation)

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
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

                    DataStore.currentUser.uid = userData.uid
                    DataStore.currentUser.email = userData.email
                    DataStore.currentUser.mobilNR = userData.mobilNR
                    DataStore.currentUser.name = userData.name
                    DataStore.currentUser.guardians = userData.guardians
                    DataStore.currentUser.location = userData.location
                }
            }

        }
        DataStore.syncChangesToFirebase()
    }
    fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container_layout, fragment).commit()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when(keyCode){
            KeyEvent.KEYCODE_HOME -> Log.d("KEYOVERRIDE", "Key home pressed")
            KeyEvent.KEYCODE_BACK -> Log.d("KEYOVERRIDE", "Key Back pressed")
            KeyEvent.KEYCODE_VOLUME_UP -> Log.d("KEYOVERRIDE", "Vol Up pressed")
            KeyEvent.KEYCODE_VOLUME_DOWN -> Log.d("KEYOVERRIDE", "Vol Down pressed")
            KeyEvent.KEYCODE_VOLUME_MUTE -> Log.d("KEYOVERRIDE", "Vol Mute pressed")
            KeyEvent.KEYCODE_APP_SWITCH -> Log.d("KEYOVERRIDE", "APP SWITCH KEY? pressed")
        }
        return true
    }
}
