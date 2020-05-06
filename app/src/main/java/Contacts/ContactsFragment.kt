package Contacts

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import se.diimperio.guardians.DataStore
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

        val toolbar: androidx.appcompat.widget.Toolbar? =
            view?.findViewById(R.id.contacts_toolbar)
        val recyclerView: RecyclerView? = view?.findViewById(R.id.ice_contacts_recyclerview)
        val adapter = ICERecyclerAdapter(context!!, DataStore.guardians)

        //Toolbar setup
        toolbar?.setTitle("Guardians")

        //RecyclerView Setup
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = adapter
    }
}
