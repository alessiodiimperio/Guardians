package Contacts

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.annotation.RequiresApi
import models.UserManager
import models.Guardian
import se.diimperio.guardians.R

const val ADD_GUARDIAN: String = "ADD_GUARDIAN"

class AddGuardian : AppCompatActivity() {

    lateinit var guardianName: EditText
    lateinit var guardianNumber: EditText
    lateinit var guardianAvatar: ImageView
    lateinit var guardianRelation: Spinner
    lateinit var avatarBitmap: Bitmap
    lateinit var progressBar: ProgressBar

    lateinit var importGuardian: Button
    lateinit var saveGuardian: Button

    var avatarURI: String? = null

    val REQUEST_CONTACT_INFO = 1


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_guardian)

        guardianName = findViewById(R.id.add_guardian_name_edittext)
        guardianNumber = findViewById(R.id.add_guardian_number_edittext)
        guardianAvatar = findViewById(R.id.add_guardian_avatar)
        guardianRelation = findViewById<Spinner>(R.id.add_guardian_relationship_spinner)
        importGuardian = findViewById(R.id.add_guardian_import_button)
        saveGuardian = findViewById(R.id.add_guardian_save_button)
        progressBar = findViewById(R.id.add_guardian_loading)
        importGuardian.setOnClickListener {

            //Import contact from phonebook

            //Check permissions
            if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), 1)
            } else {
                //Start contact selection intent
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE

                if (intent.resolveActivity(packageManager) != null) {
                    startActivityForResult(intent, REQUEST_CONTACT_INFO)
                }
            }
        }
        saveGuardian.setOnClickListener {
            saveGuardian()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //On intent resolve use Uri from contact selection to get contact info
        val contactUri = data?.data ?: return
        getContactInfo(contactUri)
    }

    fun getContactInfo(contactUri: Uri) {

        //Select datatypes to aquire
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )
        //Resolve data of projected type
        val cursor = contentResolver.query(contactUri, projection, null, null, null)

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

            if (avatarURI != null) {
                avatarBitmap =
                    MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(avatarURI))
                guardianAvatar.setImageBitmap(avatarBitmap)
            } else {
                guardianAvatar.setImageResource(R.drawable.ic_person_accent)
            }
        }
        cursor.close()
    }

    fun saveGuardian() {

        //If fields are empty require user to fill inputs
        if (guardianName.text == null || guardianName.text.toString() == "") {
            Toast.makeText(this, "Enter a valid name for your guardian.", Toast.LENGTH_SHORT).show()
        } else if (guardianNumber.text == null || guardianNumber.text.toString() == "") {
            Toast.makeText(this, "Enter a valid number for your guardian.", Toast.LENGTH_SHORT)
                .show()
        } else {

            progressBar.visibility = VISIBLE

            //Create new guardian w/o avatar if avatar is null
            if (avatarURI != null) {
                val newGuardian = Guardian(
                    null,
                    avatarURI.toString(),
                    guardianName.text.toString(),
                    guardianNumber.text.toString(),
                    guardianRelation.selectedItem.toString()
                )
                UserManager.addGuardian(newGuardian)
                progressBar.visibility = GONE
                finish()
            } else {
                val newGuardian = Guardian(
                    null,
                    null,
                    guardianName.text.toString(),
                    guardianNumber.text.toString(),
                    guardianRelation.selectedItem.toString()
                )
                UserManager.addGuardian(newGuardian)
                progressBar.visibility = GONE
                finish()
            }
        }
        UserManager.syncChangesToFirebase()
    }
}
