package Contacts

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import se.diimperio.guardians.MainActivity
import se.diimperio.guardians.R
import se.diimperio.guardians.RequestCodes

class ContactsFragment : Fragment() {

    lateinit var addGuardianBttn:FloatingActionButton
    lateinit var recyclerView:RecyclerView
    lateinit var adapter:ICERecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.ice_contacts_recyclerview)
        adapter = ICERecyclerAdapter(context!!)

        addGuardianBttn = view.findViewById(R.id.add_guardian_fab)

        //Toolbar setup
        setupToolbar()

        //RecyclerView Setup
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        //Add guardian
        addGuardianBttn.setOnClickListener {
            if(isFirstTime()){
                //Show alert dialog first time button is pressed and save to sharedPrefs to not show again
                val alertBuilder = AlertDialog.Builder(context!!)
                alertBuilder.create()
                alertBuilder.setTitle(getString(R.string.allow_sms_notifications))
                alertBuilder.setMessage(getString(R.string.prompt_send_sms_select_remember_allow))
                alertBuilder.setNeutralButton("OK", DialogInterface.OnClickListener { dialog, _ ->
                    dialog.dismiss()
                })
                    alertBuilder.show()

            } else {
                val intent = Intent(view.context, AddGuardian::class.java)
                startActivityForResult(intent, RequestCodes.UPDATE_DATASET)
            }
        }

    }
    override fun onResume() {
        super.onResume()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun isFirstTime():Boolean{
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val isFirstPress = sharedPreferences.getBoolean("first_click", true)
        sharedPreferences.edit().putBoolean("first_click", false).apply()
        return isFirstPress
    }

    fun setupToolbar(){
        (activity as MainActivity).toolbar.visibility = VISIBLE
        (activity as MainActivity).toolbar.title = getString(R.string.guardians)
        (activity as MainActivity).toolbar.setNavigationIcon(R.drawable.ic_guardian)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                RequestCodes.UPDATE_DATASET -> {
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
}
