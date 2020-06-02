package Contacts

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import models.UserManager
import se.diimperio.guardians.R
import se.diimperio.guardians.RequestCodes
import java.util.*
import kotlin.math.E

class EditGuardian : AppCompatActivity() {
    val EDIT_GUARDIAN = "EDIT_GUARDIAN"
    lateinit var guardianName: EditText
    lateinit var guardianNumber: EditText
    lateinit var guardianAvatar: ImageView
    lateinit var guardianRelation: Spinner
    lateinit var progressBar: ProgressBar

    lateinit var saveGuardian: Button
    lateinit var deleteGuardian:Button
    var avatarUri: Uri? = null
    var position = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_guardian)

        guardianName = findViewById(R.id.edit_guardian_name_edittext)
        guardianNumber = findViewById(R.id.edit_guardian_number_edittext)
        guardianAvatar = findViewById(R.id.edit_guardian_avatar)
        guardianRelation = findViewById<Spinner>(R.id.edit_guardian_relationship_spinner)
        saveGuardian = findViewById(R.id.edit_guardian_save_button)
        deleteGuardian = findViewById(R.id.edit_guardian_delete_button)
        progressBar = findViewById(R.id.edit_guardian_loading)

        position = intent.extras?.get("position") as Int

        if(position == null) finish()

        setupEditFields(position)

        guardianAvatar.setOnClickListener {
            choosePicture()
        }
        saveGuardian.setOnClickListener {
            saveChanges(position)
        }
        deleteGuardian.setOnClickListener {
            deleteGuardian(position)
        }
    }

    fun setupEditFields(position: Int){
        val currentGuardian = UserManager.currentUser.guardians[position]

        guardianName.setText(currentGuardian.displayName)
        guardianNumber.setText(currentGuardian.mobilNR)

        val relationValues = resources.getStringArray(R.array.relationship)
        val index = relationValues.indexOf(currentGuardian.relationship.toString())
        guardianRelation.setSelection(index)

        if(currentGuardian.avatar != null){
            try{
                Picasso.get().load(currentGuardian.avatar).into(guardianAvatar)
                Log.d(EDIT_GUARDIAN, "AVATAR:${currentGuardian.avatar}")

            } catch (error:Exception){
                Log.d(EDIT_GUARDIAN,"Error: $error")
                Log.d(EDIT_GUARDIAN, "AVATAR: ${currentGuardian.avatar}")
                guardianAvatar.setImageResource(R.drawable.ic_person_accent)
            }
        }

    }
    fun choosePicture(){

        if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            val mimeTypes =
                arrayOf("image/jpeg", "image/png")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            startActivityForResult(intent, RequestCodes.GALLERY_REQUEST_CODE);
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), RequestCodes.GALLERY_REQUEST_CODE)
        }
    }

    fun saveChanges(position:Int){

        val name = guardianName.text.toString()
        val number = guardianNumber.text.toString()
        val relation = guardianRelation.selectedItem.toString()

        UserManager.currentUser.guardians[position].displayName = name
        UserManager.currentUser.guardians[position].mobilNR = number
        UserManager.currentUser.guardians[position].relationship = relation

        if(avatarUri != null){
            UserManager.currentUser.guardians[position].avatar = avatarUri.toString()
            Log.d(EDIT_GUARDIAN, "avatarUri: $avatarUri")
        }
        UserManager.syncChangesToFirebase()
        finish()
    }
    fun deleteGuardian(position:Int){
        UserManager.removeGuardian(position)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                RequestCodes.GALLERY_REQUEST_CODE -> {
                    val selectedImage = data?.data

                    if(selectedImage != null){
                        uploadImageToFirebaseStorage(selectedImage){ uri->
                            Picasso.get().load(uri).into(guardianAvatar)
                            UserManager.currentUser.guardians[position].avatar = uri.toString()
                            UserManager.syncChangesToFirebase()
                        }
                    }
                }
            }
        }
    }
    private fun uploadImageToFirebaseStorage(photoUri:Uri, callback:(photoUri:Uri)->Unit) {
        if (photoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(photoUri)
            .addOnSuccessListener {
                Log.d("Main","Sucessfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {uri->
                    callback.invoke(uri)
                }
            }
            .addOnFailureListener {
                Log.d("Main", "Failed to save photo")
            }
    }
}
