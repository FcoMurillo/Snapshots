package com.example.snapshots

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.snapshots.databinding.FragmentAddBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.ref.PhantomReference

class AddFragment : Fragment() {

    private val RC_GALLERY = 18
    //Nombre de la base de datos
    private val PATH_SNAPSHOT = "snapshots"

    private lateinit var mBinding: FragmentAddBinding
    private lateinit var mStorageReference: StorageReference
    private lateinit var  mDatabaseReference: DatabaseReference

    private var mPhotoSelectedUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentAddBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.btnPost.setOnClickListener { postSnapshot() }

        mBinding.btnSelect.setOnClickListener { openGallery() }

        mStorageReference = FirebaseStorage.getInstance().reference
        mDatabaseReference = FirebaseDatabase.getInstance().reference.child(PATH_SNAPSHOT)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, RC_GALLERY)
    }

    //Subir imagen a Storage de Firebase
    private fun postSnapshot() {
        mBinding.progressBar.visibility = View.VISIBLE
        //para generar un nuevo nodo y extraer la llave, y con !! se le asegura que no sera nula
        val key = mDatabaseReference.push().key!!
        val storageReference = mStorageReference.child(PATH_SNAPSHOT).child("my_photo")
        if(mPhotoSelectedUri != null){
            storageReference.putFile(mPhotoSelectedUri!!)
                .addOnProgressListener {
                    //para calcular el porcentage de bytes transferidos con respecto al total
                    val progress = (100 * it.bytesTransferred/it.totalByteCount).toDouble()
                    mBinding.progressBar.progress = progress.toInt()
                    mBinding.tvMessage.text = "$progress%"
                }
                //Se hace Cundo ya se completo toda la subida
                .addOnCompleteListener {
                    mBinding.progressBar.visibility = View.INVISIBLE
                }
                //Solo se hace si unicamente todo el proceso ha sido exitoso y se subira a la BD (RealTime DataBase)
                .addOnSuccessListener {
                    Snackbar.make(mBinding.root, "Instantanea publicada", Snackbar.LENGTH_SHORT).show()
                    //Solo cuando se extrae correctamente la URL se procede a guardar
                    it.storage.downloadUrl.addOnSuccessListener {
                        saveSnapshot(key, it.toString(), mBinding.etTitle.text.toString().trim())
                        mBinding.tilTitle.visibility = View.GONE
                        mBinding.tvMessage.text = getString(R.string.post_message_title)
                    }
                }
                //Esto solo se ejecura si fracasa, por permisos, internet, etc.
                .addOnFailureListener {
                    Snackbar.make(mBinding.root, "No se pudo subir, intente mas tarde.", Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    //Para guardar URL dentro de RealTime DataBase para poderla visualizar en el listado
    private fun saveSnapshot(key: String, url: String, title: String){
        val snapshot = Snapshot(title = title, photoUrl = url)
        mDatabaseReference.child(key).setValue(snapshot)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == RC_GALLERY){
                mPhotoSelectedUri = data?.data
                mBinding.imgPhoto.setImageURI(mPhotoSelectedUri)
                mBinding.tilTitle.visibility = View.VISIBLE
                mBinding.tvMessage.text = getString(R.string.post_message_valid_title)
            }
        }
    }

}