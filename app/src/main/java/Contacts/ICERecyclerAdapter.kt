package Contacts

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import models.Guardian
import se.diimperio.guardians.DataStore
import se.diimperio.guardians.R

class ICERecyclerAdapter(private val context: Context, private val guardians: List<Guardian>) :
    RecyclerView.Adapter<ICERecyclerAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val guardianView = layoutInflater.inflate(R.layout.ice_card, parent, false)
        return ViewHolder(guardianView)
    }

    override fun getItemCount() = guardians.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val guardian = guardians[position]
        holder.name.text = guardian.firstname + " " + guardian.lastname
        holder.relationship.text = guardian.relationship.toString()
        holder.avatar.setBackgroundResource(R.drawable.ic_person)

    }

    fun removeGuardian(position:Int){
        DataStore.guardians.removeAt(position)
        notifyDataSetChanged()
    }

    inner class ViewHolder(guardianView: View) : RecyclerView.ViewHolder(guardianView) {

        val relationship = guardianView.findViewById<TextView>(R.id.ice_relationship_textview)
        val name = guardianView.findViewById<TextView>(R.id.ice_fullname_textview)
        val editButton = guardianView.findViewById<ImageButton>(R.id.ice_edit_button)
        val avatar = guardianView.findViewById<ImageView>(R.id.ice_avatar_imageview)


        init {
            editButton.setOnClickListener() {
                Log.d("!!!", "Pressed edit button")
/*
                val intent = Intent(context, AddEditStudentActivity::class.java)
                intent.putExtra(STUDENT_POSITION_KEY, studentPosition)
                context.startActivity(intent)
 */
            }

            /*
            deleteBttn.setOnClickListener(){ view ->
                val dialogBuilder = AlertDialog.Builder(context)

                dialogBuilder.setTitle("Remove Guardian?")
                    .setMessage("Are you sure you want to remove this contact from your Guardians List?")
                    .setPositiveButton("Yes", DialogInterface.OnClickListener{ dialog, id ->

                        Snackbar.make(view, "Guardian removed.", Snackbar.LENGTH_SHORT).show()
                        removeStudent(studentPosition)
                    })
                    .setNegativeButton("No", DialogInterface.OnClickListener{ dialog, id ->
                        dialog.cancel()
                    })

                val alert = dialogBuilder.create()

                alert.show()

             */
            }
        }
    }