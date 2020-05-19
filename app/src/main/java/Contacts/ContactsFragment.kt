package Contacts

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import se.diimperio.guardians.MainActivity
import se.diimperio.guardians.R

class ContactsFragment : Fragment() {

    lateinit var addGuardian:FloatingActionButton
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.ice_contacts_recyclerview)
        adapter = ICERecyclerAdapter(context!!)

        addGuardian = view.findViewById(R.id.add_guardian_fab)

        //Toolbar setup
        setupToolbar()

        //RecyclerView Setup
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        //Add guardian
        addGuardian.setOnClickListener {
            val intent = Intent(view.context, AddGuardian::class.java)
            startActivity(intent)
        }

    }
    override fun onResume() {
        super.onResume()
        recyclerView.adapter?.notifyDataSetChanged()

    }
    fun setupToolbar(){
        (activity as MainActivity).toolbar.visibility = VISIBLE
        (activity as MainActivity).toolbar.setTitle("Guardians")
        (activity as MainActivity).toolbar.setNavigationIcon(R.drawable.ic_guardian)
    }
}
