package com.example.unitrackerv12.Mng

import android.content.*
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.unitrackerv12.IngrReg.SignInActivity
import com.example.unitrackerv12.MapsActivity
import com.example.unitrackerv12.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_mngaccount.*



class MngAccount : AppCompatActivity(){
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mngaccount)

        //Se muestra el id de usuario en el textIDUser
        textIDUser.setText(auth.currentUser?.uid)
        //Cerrar sesion
        buttonCerrarSes.setOnClickListener{
            auth.signOut()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
        //Copiar el id de usuario al portapapeles
        textIDUser.setOnClickListener{
            copyTextToClipboard(textIDUser.text.toString())
        }
        //Envia un email para restaurar la contraseña
        buttonModPasswd.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Recuperar contraseña")
            val view = layoutInflater.inflate(R.layout.popup_modpsswd,null)
            builder.setView(view)
            //val email = auth.currentUser?.email
            //ModPsswd.setText(email)
            builder.setPositiveButton("Enviar", DialogInterface.OnClickListener {_,_->
                SendEmail(auth.currentUser?.email)
            })
            builder.setNegativeButton("Cancelar", DialogInterface.OnClickListener { _, _ ->  })
            builder.show()
        }
        //Eliminar cuenta
        buttonElimCuenta.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.popup_dltacc, null)
            builder.setTitle("Esta acción no tiene vuelta atrás...")
            builder.setView(view)
            builder.setPositiveButton("Eliminar", DialogInterface.OnClickListener {_,_->
                auth!!.currentUser?.delete()
                auth.signOut()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            })
            builder.setNegativeButton("Cancelar", DialogInterface.OnClickListener { _, _ ->  })
            builder.show()
        }
        btnmngAccount1.isEnabled = false
        /*btnmngAccount1.setOnClickListener {
            val intent = Intent(this, MngAccount::class.java)
            startActivity(intent)
            finish()
        }*/
        btnMapa1.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }
        /*btnmngTracking1.setOnClickListener {
            val intent = Intent(this, ::class.java)
            startActivity(intent)
            finish()
        }*/
    }

    //copiar el texto al portapapeles
    private fun copyTextToClipboard(copy :String) {
        val textToCopy = copy
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Mi UserID", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "UserID copiado al portapapeles", Toast.LENGTH_LONG).show()
    }
    //Envia un email para restaurar la contraseña
    private fun SendEmail(username: String?){
        if (username.toString().isEmpty()) {
            Toast.makeText(this, "Ingrese un correo válido", Toast.LENGTH_LONG).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(username.toString()).matches()) {
            Toast.makeText(this, "No existen cuentas con ese correo", Toast.LENGTH_LONG).show()
            return
        }

        auth.sendPasswordResetEmail(username.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this,"Email enviado.",Toast.LENGTH_SHORT).show()
                }
            }
    }
}


