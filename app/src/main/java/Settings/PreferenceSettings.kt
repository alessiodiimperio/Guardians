package Settings

import android.content.Context
/**
 * Created by aleksandr on 2018/02/09.
 */

object PreferencesSettings {

    private const val PREF_FILE = "settings_pref"

    fun saveToPref(context: Context, str: String?) {
        val sharedPref = context.getSharedPreferences(
            PREF_FILE,
            Context.MODE_PRIVATE
        )
        val editor = sharedPref.edit()
        editor.putString("code", str)
        editor.apply()
    }

    fun getCode(context: Context): String? {
        val sharedPref = context.getSharedPreferences(
            PREF_FILE,
            Context.MODE_PRIVATE
        )
        val defaultValue = ""
        return sharedPref.getString("code", defaultValue)
    }
}