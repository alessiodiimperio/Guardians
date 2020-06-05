package Contacts

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import Managers.UserManager
import android.net.Uri
import se.diimperio.guardians.R

const val CONTACTS_ADAPTER: String = "CONTACTS_ADAPTER"

class ICERecyclerAdapter(private val context: Context) :
    RecyclerView.Adapter<ICERecyclerAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val guardianView = layoutInflater.inflate(R.layout.ice_card, parent, false)
        return ViewHolder(guardianView)
    }

    override fun getItemCount() = UserManager.currentUser.guardians.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val guardian = UserManager.currentUser.guardians[position]

        holder.name.text = guardian.displayName
        holder.relationship.text = guardian.relationship.toString()
        holder.number.text = guardian.phoneNumber
        holder.email.text = guardian.email

        holder.editButton.setOnClickListener {

            //Edit button within cardview to edit guardian att given position
            val intent = Intent(context, EditGuardian::class.java)
            intent.putExtra("position", position)
            context.startActivity(intent)
        }

        if (guardian.avatar != null) {
            Picasso.get().load(Uri.parse(guardian.avatar)).into(holder.avatar)
        } else {
            holder.avatar.setBackgroundResource(R.drawable.ic_person_accent)
        }
    }

    inner class ViewHolder(guardianView: View) : RecyclerView.ViewHolder(guardianView) {
        val relationship = guardianView.findViewById<TextView>(R.id.ice_relationship_textview)
        val name = guardianView.findViewById<TextView>(R.id.ice_fullname_textview)
        val editButton = guardianView.findViewById<ImageButton>(R.id.ice_edit_button)
        val avatar = guardianView.findViewById<ImageView>(R.id.ice_avatar_imageview)
        val number = guardianView.findViewById<TextView>(R.id.ice_phone_number)
        val email = guardianView.findViewById<TextView>(R.id.ice_email)
    }
}

