package Contacts

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import models.UserManager
import se.diimperio.guardians.R
import java.lang.Exception

const val CONTACTS_ADAPTER:String = "CONTACTS_ADAPTER"
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
        holder.number.text = guardian.mobilNR

        holder.editButton.setOnClickListener {
            UserManager.removeGuardian(position)
            notifyDataSetChanged()
        }

        var avatar: Bitmap? = null
        try {
            avatar = MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                Uri.parse(guardian.avatar)
            )
        } catch (error: Exception) {
            Log.d(CONTACTS_ADAPTER, "Error no image at: ${error.message}")

        }

        if (avatar != null) {
            holder.avatar.setImageURI(Uri.parse(guardian.avatar))
        } else {
            holder.avatar.setBackgroundResource(R.drawable.ic_person)
        }
    }
    inner class ViewHolder(guardianView: View) : RecyclerView.ViewHolder(guardianView) {
        val relationship = guardianView.findViewById<TextView>(R.id.ice_relationship_textview)
        val name = guardianView.findViewById<TextView>(R.id.ice_fullname_textview)
        val editButton = guardianView.findViewById<ImageButton>(R.id.ice_edit_button)
        val avatar = guardianView.findViewById<ImageView>(R.id.ice_avatar_imageview)
        val number = guardianView.findViewById<TextView>(R.id.ice_phone_number)

    }
}

