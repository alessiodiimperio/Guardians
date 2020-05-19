package Contacts

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import models.DataStore
import se.diimperio.guardians.R

class ICERecyclerAdapter(private val context: Context):RecyclerView.Adapter<ICERecyclerAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val guardianView = layoutInflater.inflate(R.layout.ice_card, parent, false)
        return ViewHolder(guardianView)
    }

    override fun getItemCount() = DataStore.currentUser.guardians.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val guardian = DataStore.currentUser.guardians[position]
        holder.name.text = guardian.displayName
        holder.relationship.text = guardian.relationship.toString()
        holder.number.text = guardian.mobilNR

        holder.editButton.setOnClickListener {
            DataStore.removeGuardian(position)
            notifyDataSetChanged()
        }

        if (guardian.avatar != null) {
            var image: Bitmap? = null
            val storageRef = FirebaseStorage.getInstance()
            storageRef.getReferenceFromUrl(guardian.avatar.toString()).getBytes(1024 * 1024)
                .addOnSuccessListener { data ->
                    if (data != null) {
                        image = BitmapFactory.decodeByteArray(data, 0, data.size)
                    }
                }
            if (image != null) {
                holder.avatar.setImageBitmap(image)
            } else {
                holder.avatar.setBackgroundResource(R.drawable.ic_person)
            }
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

