package se.diimperio.guardians

import Alarm.AlarmFragment
import Alarm.AlertMapsFragment
import Alarm.MAPS_FRAGMENT
import Contacts.ContactsFragment
import Login.LoginActivity
import Settings.SettingsFragment
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import Managers.AlarmManager
import models.User
import Managers.UserManager

const val ALERT_NOTIFICATION_CHANNEL: String = "ALERT_CHANNEL"
const val MAIN_ACTIVITY = "MAIN_ACTIVITY"
class MainActivity() : AppCompatActivity() {

    lateinit var activeAlertBttn: Button
    lateinit var bottomNav: BottomNavigationView
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var mapsFragment:AlertMapsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapsFragment = AlertMapsFragment()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        //Initialize UI components
        activeAlertBttn = findViewById(R.id.active_alert_button)
        toolbar = findViewById(R.id.activity_toolbar)
        bottomNav = findViewById(R.id.bottom_navigation)

        setupCurrentUser()
        createNotificationChannels()

        AlarmManager.setupAlarmListener(this, applicationContext)

        activeAlertBttn.setOnClickListener {
            loadFragment(mapsFragment, MAPS_FRAGMENT)

            activeAlertBttn.visibility = GONE
            toolbar.visibility = GONE
        }

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.alarm_menu_item -> {
                    loadFragment(AlarmFragment())
                    checkForActiveAlerts()
                    true
                }
                R.id.contacts_menu_item -> {
                    if (UserManager.currentUser.guardians.size < 1) {
                        loadFragment(ContactsFragment())
                    } else if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        loadFragment(ContactsFragment())
                    } else {
                        requestPermissions(
                            arrayOf(android.Manifest.permission.READ_CONTACTS), 12345
                        )
                    }
                    checkForActiveAlerts()
                    true
                }
                R.id.settings_menu_item -> {

                    loadFragment(SettingsFragment())
                    checkForActiveAlerts()

                    true
                }
                else -> false
            }
        }

        //Select middle menu ("alarm") on start if not sent from notification
            bottomNav.setSelectedItemId(
                bottomNav.getMenu().getItem(1).itemId
            )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                ALERT_NOTIFICATION_CHANNEL,
                "Guardian ALERT Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "Guardian Alerts"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun setupCurrentUser() {

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null || currentUserId.isEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } else {
            val profileRef = db.collection("users").document("$currentUserId")
            profileRef.addSnapshotListener { snapshot, firebaseFirestoreException ->
                if (snapshot != null) {
                    val user = snapshot?.toObject(User::class.java)
                    val userData = user ?: return@addSnapshotListener

                    UserManager.currentUser.uid = userData.uid
                    UserManager.currentUser.email = userData.email
                    UserManager.currentUser.number = userData.number
                    UserManager.currentUser.name = userData.name
                    UserManager.currentUser.guardians = userData.guardians
                    UserManager.currentUser.location = userData.location
                }
            }
        }
    }

    fun loadFragment(fragment: Fragment, tag: String? = null) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container_layout, fragment, tag).commit()
    }

    fun checkForActiveAlerts() {
        //Make active alert button visibleif alarms exist
        if (AlarmManager.activeAlertsExists()) {

            val fragment = supportFragmentManager.findFragmentById(R.id.container_layout)
            if(fragment != null && fragment.javaClass != AlertMapsFragment::class.java) {
                activeAlertBttn.visibility = VISIBLE
            }

        } else {
            activeAlertBttn.visibility = GONE
        }
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)

        //If an alarm object exists show alert button on all fragments except the maps fragment
        if(AlarmManager.activeAlertsExists()){
            if(fragment.javaClass != AlertMapsFragment::class.java){
                Log.d(MAIN_ACTIVITY, "Fragment type is: $fragment make visible")
                activeAlertBttn.visibility = VISIBLE
        } else {
                activeAlertBttn.visibility = GONE
                Log.d(MAIN_ACTIVITY, "Fragment type is: $fragment make invisible")
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if(intent != null){

        val isSentFromAlertNotification =
            intent.getBooleanExtra("load_maps_fragment", false).apply {
                if (this) {
                    loadFragment(mapsFragment, MAPS_FRAGMENT)

                    activeAlertBttn.visibility = GONE
                    toolbar.visibility = GONE

                    Log.d(MAIN_ACTIVITY, "Sent from notification show maps fragment")

                } else {
                    Log.d(MAIN_ACTIVITY, "Not sent from notification start as usual")
                }
            }
        }
    }
}
