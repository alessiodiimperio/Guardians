package Contacts

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import Managers.UserManager
import se.diimperio.guardians.R
import se.diimperio.guardians.RequestCodes
import java.util.*

class EditGuardian : AppCompatActivity() {
    val EDIT_GUARDIAN = "EDIT_GUARDIAN"
    lateinit var guardianNameEditText: EditText
    lateinit var guardianNumberEditText: EditText
    lateinit var guardianEmailEditText:EditText
    lateinit var guardianAvatarImageView: ImageView
    lateinit var guardianRelationSpinner: Spinner
    lateinit var progressBar: ProgressBar
    lateinit var saveGuardianBttn: Button
    lateinit var deleteGuardianBttn:Button
    var position = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_guardian)

        //UI Components
        guardianNameEditText = findViewById(R.id.edit_guardian_name_edittext)
        guardianNumberEditText = findViewById(R.id.edit_guardian_number_edittext)
        guardianEmailEditText = findViewById(R.id.edit_guardian_email_edittext)
        guardianAvatarImageView = findViewById(R.id.edit_guardian_avatar)
        guardianRelationSpinner = findViewById<Spinner>(R.id.edit_guardian_relationship_spinner)
        saveGuardianBttn = findViewById(R.id.edit_guardian_save_button)
        deleteGuardianBttn = findViewById(R.id.edit_guardian_delete_button)
        progressBar = findViewById(R.id.edit_guardian_loading)

        position = intent.extras?.get("position") as Int

        if(position == null) finish()

        setupEditFields(position)

        guardianAvatarImageView.setOnClickListener {
            choosePicture()
        }
        saveGuardianBttn.setOnClickListener {
            saveChanges(position)
        }
        deleteGuardianBttn.setOnClickListener {
            deleteGuardian(position)
        }
    }

    fun setupEditFields(position: Int){

        val currentGuardian = UserManager.currentUser.guardians[position]

        //Setup edittexts
        guardianNameEditText.setText(currentGuardian.displayName)
        guardianNumberEditText.setText(currentGuardian.phoneNumber)
        guardianEmailEditText.setText(currentGuardian.email)

        //Setup spinner
        val relationValues = resources.getStringArray(R.array.relationship)
        val index = relationValues.indexOf(currentGuardian.relationship.toString())
        guardianRelationSpinner.setSelection(index)

        //Setup avatar
        if(currentGuardian.avatar != null){
            try{
                Picasso.get().load(currentGuardian.avatar).into(guardianAvatarImageView)
                Log.d(EDIT_GUARDIAN, "AVATAR:${currentGuardian.avatar}")

            } catch (error:Exception){
                Log.d(EDIT_GUARDIAN,"Error: $error")
                Log.d(EDIT_GUARDIAN, "AVATAR: ${currentGuardian.avatar}")
                guardianAvatarImageView.setImageResource(R.drawable.ic_person_accent)
            }
        } else {
            guardianAvatarImageView.setImageResource(R.drawable.ic_person_accent)
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

        val name = guardianNameEditText.text.toString()
        val number = guardianNumberEditText.text.toString()
        val email = guardianEmailEditText.text.toString()
        val relation = guardianRelationSpinner.selectedItem.toString()

        UserManager.currentUser.guardians[position].displayName = name
        UserManager.currentUser.guardians[position].phoneNumber = number
        UserManager.currentUser.guardians[position].email = email
        UserManager.currentUser.guardians[position].relationship = relation

        UserManager.syncGuardianUids {
            //Check if guardian email has corresponding uid and sync it

            Log.d(EDIT_GUARDIAN,"Saving and syncing changes and uids")
            UserManager.syncChangesToFirebase()
        }
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
                        uploadImageToFirebaseStorage(selectedImage){ url->
                            Picasso.get().load(url).into(guardianAvatarImageView)
                            UserManager.currentUser.guardians[position].avatar = url.toString()
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
        val imagesRef = FirebaseStorage.getInstance().getReference("/images/$filename")

        imagesRef.putFile(photoUri)
            .addOnSuccessListener {
                Log.d(EDIT_GUARDIAN,"Image uploaded: ${it.metadata?.path}")

                imagesRef.downloadUrl.addOnSuccessListener { url->
                    callback.invoke(url)
                }
            }
            .addOnFailureListener {
                Log.d(EDIT_GUARDIAN, "Failed to upload image")
            }
    }
}
