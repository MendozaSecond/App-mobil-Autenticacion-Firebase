package com.example.pruebafirebase

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.pruebafirebase.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            signIn(email, password)
        }

        binding.selectImageButton.setOnClickListener {
            selectImage()
        }

        binding.uploadImageButton.setOnClickListener {
            uploadImage()
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserToFirestore(user.uid, email)
                    }
                    Toast.makeText(this, "Autenticacion Exitosa.", Toast.LENGTH_SHORT).show()
                    // Update UI with user information, or navigate to another activity
                } else {
                    val exception = task.exception
                    if (exception != null) {
                        Toast.makeText(this, "Autenticacion Fallida: ${exception.message}", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Error de autenticación: error desconocido", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun saveUserToFirestore(uid: String, email: String) {
        val user = hashMapOf(
            "uid" to uid,
            "email" to email
        )

        firestore.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Datos de usuario guardados en Firestore.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar los datos del usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        selectImageLauncher.launch(intent)
    }

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            imageUri = result.data!!.data
            binding.uploadImageButton.visibility = android.view.View.VISIBLE
        }
    }

    private fun uploadImage() {
        if (imageUri != null) {
            val storageRef: StorageReference = storage.reference.child("images/${imageUri!!.lastPathSegment}")
            storageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    Toast.makeText(this, "Imagen cargada exitosamente.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error subiendo imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "\n" +
                    "Por favor seleccione una imagen primero\n.", Toast.LENGTH_SHORT).show()
        }
    }
}