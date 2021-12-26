package com.example.unitrackerv12.IngrReg

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.unitrackerv12.MapsActivity
import com.example.unitrackerv12.Mng.MngAccount
import com.example.unitrackerv12.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_signin.*
import kotlinx.android.synthetic.main.popup_modpsswd.*


class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        setContentView(R.layout.activity_signin)
        //Lleva a la pantalla SignUp
        SignUp.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
        //Envia email para recuperar contraseña
        FrgtPsswd.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Recuperar contraseña")
            val view = layoutInflater.inflate(R.layout.popup_modpsswd,null)
            val email =view.findViewById<EditText>(R.id.ModPsswd)
            builder.setView(view)
            builder.setPositiveButton("Enviar", DialogInterface.OnClickListener { _, _->
                SendEmail(email)
            })
            builder.setNegativeButton("Cancelar", DialogInterface.OnClickListener { _, _ ->  })
            builder.show()
        }
        //Inicia Sesion
        buttonInicSes.setOnClickListener {
            val inputMethodManager =  getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            if (SI_Email.text.toString().isNullOrEmpty() || SI_Password.text.toString().isNullOrEmpty())
                SI_Notificar.text = "Debes llenar todos los datos..."
            else {
                auth.signInWithEmailAndPassword(SI_Email.text.toString(),SI_Password.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            SI_Notificar.text =  "Ingresando :)"
                            val user = auth.currentUser
                            updateUI(user, SI_Email.text.toString() )
                        } else
                            SI_Notificar.text = "e-mail o contraseña incorrectos"
                    }
            }
        }
    }

    private fun updateUI(currentUser: FirebaseUser?, emailAdd: String) {
        if(currentUser !=null){
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("emailAddress", emailAdd);
            startActivity(intent)
            finish()
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null){
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    //Envia un email para restaurar la contraseña
    private fun SendEmail(username : EditText){
        if (username.text.toString().isEmpty()) {
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(username.text.toString()).matches()) {
            return
        }

        auth.sendPasswordResetEmail(username.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this,"Email enviado.", Toast.LENGTH_SHORT).show()
                }
            }
    }

}
