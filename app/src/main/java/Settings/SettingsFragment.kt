package Settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import se.diimperio.guardians.MainActivity
import se.diimperio.guardians.R


class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setupToolbar()
    }
    fun setupToolbar(){
        (activity as MainActivity).toolbar.visibility = VISIBLE
        (activity as MainActivity).toolbar.setTitle("Settings")
        (activity as MainActivity).toolbar.setNavigationIcon(R.drawable.ic_settings)
    }

}
