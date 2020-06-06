package Contacts

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import models.Guardian
import Managers.UserManager
import se.diimperio.guardians.R
import se.diimperio.guardians.RequestCodes


const val ADD_GUARDIAN: String = "ADD_GUARDIAN"

class AddGuardian : AppCompatActivity() {

    lateinit var guardianAvatarImageGroup: View
    lateinit var guardianName: EditText
    lateinit var guardianNumber: EditText
    lateinit var guardianEmail: EditText
    lateinit var guardianAvatar: ImageView
    lateinit var guardianRelation: Spinner
    lateinit var progressBar: ProgressBar

    lateinit var importGuardian: Button
    lateinit var saveGuardian: Button

    var avatarURI: String? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_guardian)

        //Set UI components
        guardianAvatarImageGroup = findViewById(R.id.add_avatar_image_group)
        guardianName = findViewById(R.id.add_guardian_name_edittext)
        guardianNumber = findViewById(R.id.add_guardian_number_edittext)
        guardianEmail = findViewById(R.id.add_guardian_email_edittext)
        guardianAvatar = findViewById(R.id.add_guardian_avatar)
        guardianRelation = findViewById<Spinner>(R.id.add_guardian_relationship_spinner)
        importGuardian = findViewById(R.id.add_guardian_import_button)
        saveGuardian = findViewById(R.id.add_guardian_save_button)
        progressBar = findViewById(R.id.add_guardian_loading)
        importGuardian.setOnClickListener {

            //Import contact from PhoneBook
            //Check permissions
            val permissions = arrayOf(android.Manifest.permission.READ_CONTACTS)
            if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, 1)
            } else {
                //Start contact selection intent
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE

                if (intent.resolveActivity(packageManager) != null) {
                    startActivityForResult(intent, RequestCodes.REQUEST_CONTACT_READ)
                }
            }
        }
        guardianAvatarImageGroup.setOnClickListener {
            choosePicture()
        }
        saveGuardian.setOnClickListener {
            saveGuardian()
        }
    }

    fun choosePicture() {
        //Select image from gallery
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            val mimeTypes =
                arrayOf("image/jpeg", "image/png")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            startActivityForResult(intent, RequestCodes.GALLERY_REQUEST_CODE);
        } else {
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                RequestCodes.GALLERY_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //On intent resolve use Uri from contact selection to get contact info
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RequestCodes.GALLERY_REQUEST_CODE -> {
                    val selectedImage = data?.data

                    if (selectedImage != null) {
                        Picasso.get().load(selectedImage).into(guardianAvatar)
                        avatarURI = selectedImage.toString()
                    }
                }
                RequestCodes.REQUEST_CONTACT_READ -> {
                    val contactUri = data?.data ?: return

                    getContactInfo(contactUri) { photoUri ->
                        if (photoUri != null) {
                            Picasso.get().load(photoUri).into(guardianAvatar)
                            avatarURI = photoUri
                        } else {
                            guardianAvatar.setImageResource(R.drawable.ic_person_accent)
                        }
                    }
                }
            }
        }
    }

    private fun uploadImageToFirebaseStorage(photoUri: Uri, callback: (photoUri: Uri) -> Unit) {

        if (photoUri == null) return

        val ref =
            FirebaseStorage.getInstance().getReference("/images/${UserManager.currentUser.uid}")

        ref.putFile(photoUri)
            .addOnSuccessListener {
                Log.d(ADD_GUARDIAN, "Sucessfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener { url ->
                    callback.invoke(url)
                }
            }
            .addOnFailureListener {
                Log.d(ADD_GUARDIAN, "Failed to save photo")
            }
    }

    private fun getContactInfo(contactUri: Uri, callback: (photoUri: String?) -> Unit) {

        //Select datatypes to aquire
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )
        //val emailProjection = arrayOf(ContactsContract.CommonDataKinds.Email.DATA1)

        //Resolve data of projected type
        val cursor = contentResolver.query(contactUri, projection, null, null, null)

        //val emailCursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, emailProjection, null,null,null)

        //Parse resolvable data to strings
        if (cursor!!.moveToFirst()) {
            val name =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            avatarURI =
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))

            //Set to UI Elements
            guardianName.setText(name)
            guardianNumber.setText(number)

            callback.invoke(avatarURI)

            /*
            if(emailCursor!!.moveToFirst()){
                val email = emailCursor.getString(emailCursor.getColumnIndex((ContactsContract.CommonDataKinds.Email.DATA1)))
                guardianEmail.setText(email)
            }
             */
        }
        cursor.close()

        //emailCursor?.close()
    }

    fun saveGuardian() {

        //If fields are empty require user to fill inputs
        if (guardianName.text == null || guardianName.text.toString() == "") {
            Toast.makeText(this, "Enter a valid name for your guardian.", Toast.LENGTH_SHORT).show()
        } else if (guardianNumber.text == null || guardianNumber.text.toString() == "") {
            Toast.makeText(this, "Enter a valid number for your guardian.", Toast.LENGTH_SHORT)
                .show()
        } else {
            //Check for permissions and if needed request them
            if (checkSelfPermission(android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.SEND_SMS),
                    RequestCodes.SEND_SMS
                )
            } else {

                val smsManager = SmsManager.getDefault()
                val alert =
                    "${UserManager.currentUser.name} added you to their list of \"Guardians\". This SMS is a test to ensure you receive their alerts."

                smsManager.sendTextMessage(guardianNumber.text.toString(), null, alert, null, null)
                Log.d(UserManager.USER_MANAGER, alert)
            }

            progressBar.visibility = VISIBLE

            //Create new guardian w/o avatar if avatar is null
            if (avatarURI != null) {
                uploadImageToFirebaseStorage(Uri.parse(avatarURI)){ link->
                    val newGuardian = Guardian(
                        null,
                        link.toString(),
                        guardianName.text.toString(),
                        guardianNumber.text.toString(),
                        guardianRelation.selectedItem.toString(),
                        guardianEmail.text.toString()
                    )
                    UserManager.addGuardian(newGuardian)
                    progressBar.visibility = GONE
                    finish()
                }
            } else {
                val newGuardian = Guardian(
                    null,
                    null,
                    guardianName.text.toString(),
                    guardianNumber.text.toString(),
                    guardianRelation.selectedItem.toString(),
                    guardianEmail.text.toString()
                )
                UserManager.addGuardian(newGuardian)
                progressBar.visibility = GONE
                finish()
            }
        }
    }
}
