package Contacts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import se.diimperio.guardians.DataStore
import se.diimperio.guardians.MainActivity
import se.diimperio.guardians.R

class ContactsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onStart() {
        super.onStart()

        val recyclerView: RecyclerView? = view?.findViewById(R.id.ice_contacts_recyclerview)
        val adapter = ICERecyclerAdapter(context!!, DataStore.guardians)

        //Toolbar setup
        setupToolbar()

        //RecyclerView Setup
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = adapter
    }
    fun setupToolbar(){
        (activity as MainActivity).toolbar.visibility = VISIBLE
        (activity as MainActivity).toolbar.setTitle("Guardians")
        (activity as MainActivity).toolbar.setNavigationIcon(R.drawable.ic_guardian)
    }
}
