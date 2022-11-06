package com.shyptsolution.googledriveapi

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.lang.Exception
import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import kotlinx.coroutines.*
import android.widget.*
import com.google.api.client.util.IOUtils
import java.io.*
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var mDrive: Drive
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mDrive = getDriveService(this)
        var addAttachment = findViewById<Button>(R.id.upload)
        addAttachment.setOnClickListener {
            GlobalScope.async(Dispatchers.IO) {
                val intent = Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT)
                startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)

            }
        }

    }


    fun getDriveService(context: Context): Drive {
        GoogleSignIn.getLastSignedInAccount(context).let { googleAccount ->
            val credential = GoogleAccountCredential.usingOAuth2(
                this, listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = googleAccount!!.account!!
            return Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(getString(R.string.app_name))
                .build()
        }
        var tempDrive: Drive
        return tempDrive
    }

    fun uploadFileToGDrive(context: Context) {
        mDrive.let { googleDriveService ->
            lifecycleScope.launch {
                try {


//                    val fileName = "Ticket"
                    val raunit = File("storage/emulated/0/Download", "download.png")
                    val gfile = com.google.api.services.drive.model.File()
                    gfile.name = "Subscribe"
                    val mimetype = "image/png"
                    val fileContent = FileContent(mimetype, raunit)
                    var fileid = ""


                    withContext(Dispatchers.Main) {

                        withContext(Dispatchers.IO) {
                            launch {
                                var mFile =
                                    googleDriveService.Files().create(gfile, fileContent).execute()

                            }
                        }


                    }


                } catch (userAuthEx: UserRecoverableAuthIOException) {
                    startActivity(
                        userAuthEx.intent
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("asdf", e.toString())
                    Toast.makeText(
                        context,
                        "Some Error Occured in Uploading Files" + e.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == RESULT_OK) {
            val selectedFile = data!!.data //The uri with the location of the file
            makeCopy(selectedFile!!)
            Toast.makeText(this,selectedFile.toString(),Toast.LENGTH_LONG).show()
        }
    }

    private fun makeCopy(fileUri: Uri) {
        val parcelFileDescriptor = applicationContext.contentResolver.openFileDescriptor(fileUri, "r", null)
        val inputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)
        val file = File(applicationContext.filesDir, getFileName(applicationContext.contentResolver, fileUri))
        val outputStream = FileOutputStream(file)
        IOUtils.copy(inputStream, outputStream)

    }

    private fun getFileName(contentResolver: ContentResolver, fileUri: Uri): String {

        var name = ""
        val returnCursor = contentResolver.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }

        return name
    }

}