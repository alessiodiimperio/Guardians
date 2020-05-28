package se.diimperio.guardians

import Alarm.AlarmFragment
import Alarm.AlertMapsFragment
import Contacts.ContactsFragment
import Login.LoginActivity
import Settings.SettingsFragment
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.Button
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import models.Alarm
import models.AlarmManager
import models.User
import models.UserManager

const val ALERT_NOTIFICATION_CHANNEL:String = "ALERT_CHANNEL"
class MainActivity() : AppCompatActivity() {

    lateinit var activeAlertBttn: Button
    lateinit var bottomNav: BottomNavigationView
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    lateinit var alarmFragment: AlarmFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        //Initiallize fragments for Main Activity
        val contactsFragment = ContactsFragment()
        alarmFragment = AlarmFragment()

        activeAlertBttn = findViewById(R.id.active_alert_button)
        toolbar = findViewById(R.id.activity_toolbar)
        bottomNav = findViewById(R.id.bottom_navigation)

        setupCurrentUser()
        createNotificationChannels()

        AlarmManager.setupAlarmListener(this, applicationContext)

        activeAlertBttn.setOnClickListener {
            val mapsFragment = AlertMapsFragment()
            loadFragment(mapsFragment)
            activeAlertBttn.visibility = GONE
            toolbar.visibility = GONE
        }

        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.alarm_menu_item -> {
                    loadFragment(alarmFragment)
                    if (AlarmManager.activeAlertsExists()) {
                        activeAlertBttn.visibility = VISIBLE
                    } else {
                        activeAlertBttn.visibility = GONE
                    }
                    true
                }
                R.id.contacts_menu_item -> {
                    if(UserManager.currentUser.guardians.size < 1){
                        loadFragment(contactsFragment)
                    } else if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
                        loadFragment(contactsFragment)
                    } else {
                        requestPermissions(
                            arrayOf(android.Manifest.permission.READ_CONTACTS), 12345)
                    }

                    if (AlarmManager.activeAlertsExists()) {
                        activeAlertBttn.visibility = VISIBLE
                    } else {
                        activeAlertBttn.visibility = GONE
                    }
                    true
                }
                R.id.settings_menu_item -> {
                    if (AlarmManager.activeAlertsExists()) {
                        activeAlertBttn.visibility = VISIBLE
                    } else {
                        activeAlertBttn.visibility = GONE
                    }
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
        //Select middle menu ("alarm") on start
        bottomNav.setSelectedItemId(bottomNav.getMenu().getItem(1).itemId
        )
    }

    private fun createNotificationChannels() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(
                ALERT_NOTIFICATION_CHANNEL,
                "Guardian ALERT Channel",
                NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = "A Guardian Alarm has been triggered nearby!"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
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
    fun checkForActiveAlerts() {
        if (AlarmManager.activeAlertsExists()) {

            activeAlertBttn.visibility = VISIBLE
        } else {
            activeAlertBttn.visibility = GONE
        }
    }

    /*
    ****************** Remapping keys.....
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> Log.d("KEYOVERRIDE", "Vol Up pressed") // Active
            KeyEvent.KEYCODE_VOLUME_DOWN -> Log.d("KEYOVERRIDE", "Vol Down pressed") // Active
            KeyEvent.KEYCODE_VOLUME_MUTE -> Log.d("KEYOVERRIDE", "Vol Mute pressed") // Active
            KeyEvent.KEYCODE_BACK -> Log.d("KEYOVERRIDE", "Back Pressed") //Active
        }
        return true
    }


    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if(alarmFragment.alarm.stateMachine.state == Alarm.State.Alarming) {
            (applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).moveTaskToFront(
                taskId,
                0
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if(alarmFragment.alarm.stateMachine.state == Alarm.State.Alarming) {
            (applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).moveTaskToFront(
                taskId,
                0
            )
        }
    }
}
