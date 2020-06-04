package Settings

import Alarm.CREATE_PIN_FRAGMENT
import Login.LoginActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.beautycoder.pflockscreen.PFFLockScreenConfiguration
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment
import com.google.firebase.auth.FirebaseAuth
import se.diimperio.guardians.MainActivity
import se.diimperio.guardians.R

class SettingsFragment : PreferenceFragmentCompat() {
    val SETTINGS:String = "SETTINGS"

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setupToolbar()

        //Handle set new PIN button press
        val setPIN = findPreference("setpin")
        setPIN.setOnPreferenceClickListener(
            object : Preference.OnPreferenceClickListener {
                override fun onPreferenceClick(arg0: Preference?): Boolean {
                    Log.d(SETTINGS, "in clicklistener")
                    createPINFragment()
                    return true
                }
            })

        //Handle signout button press
        val signOut = findPreference("signout")
        signOut.setOnPreferenceClickListener(
            object : Preference.OnPreferenceClickListener {
                override fun onPreferenceClick(arg0: Preference?): Boolean {
                    Log.d(SETTINGS, "in clicklistener")
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(view?.context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    return true
                }
            })
    }
    val mCodeCreateListener: PFLockScreenFragment.OnPFLockScreenCodeCreateListener =
        object : PFLockScreenFragment.OnPFLockScreenCodeCreateListener {
            override fun onCodeCreated(encodedCode: String) {
                Toast.makeText(context, "PIN created successfully", Toast.LENGTH_SHORT)
                    .show()
                PreferencesSettings.saveToPref(context!!, encodedCode)
                removeFragmentByTag(CREATE_PIN_FRAGMENT)
            }
            override fun onNewCodeValidationFailed() {
                Toast.makeText(context, "Code validation error", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    fun setupToolbar(){
        (activity as MainActivity).toolbar.visibility = VISIBLE
        (activity as MainActivity).toolbar.setTitle("Settings")
        (activity as MainActivity).toolbar.setNavigationIcon(R.drawable.ic_settings)
    }
    fun createPINFragment() {
        (activity as MainActivity).toolbar.visibility = GONE

        val builder =
            PFFLockScreenConfiguration.Builder(context)
                .setTitle("Create PIN")
                .setCodeLength(4)
                .setLeftButton("Cancel")
                .setNewCodeValidation(true)
                .setNewCodeValidationTitle("Please verify PIN")
                .setUseFingerprint(false)

        val fragment = PFLockScreenFragment()
        fragment.setOnLeftButtonClickListener {
            Toast.makeText(context, "Left button pressed", Toast.LENGTH_LONG)
                .show()
        }
        builder.setMode(PFFLockScreenConfiguration.MODE_CREATE)
        fragment.setConfiguration(builder.build())
        fragment.setCodeCreateListener(mCodeCreateListener)
        parentFragmentManager.beginTransaction()
            .add(R.id.container_layout, fragment, CREATE_PIN_FRAGMENT).commit()
    }
    fun removeFragmentByTag(tag:String){
        val fragment = parentFragmentManager.findFragmentByTag(tag)
        if(fragment != null) {
            when(tag){
                CREATE_PIN_FRAGMENT ->{
                    parentFragmentManager.beginTransaction().remove(fragment).commit()
                    (activity as MainActivity).toolbar.visibility = VISIBLE
                }
            }
        }
    }
}
