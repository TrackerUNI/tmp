package com.example.unitrackerv12.Mng

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.unitrackerv12.IngrReg.SignInActivity
import com.example.unitrackerv12.R
import com.example.unitrackerv12.UserManagerV
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_mngaccount.*
import kotlinx.android.synthetic.main.activity_signup.*
//import kotlinx.android.synthetic.main.dialog_view.view.*


class MngAccount : AppCompatActivity(){
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mngaccount)
        nomUser.text= auth.currentUser?.uid
        textIDUser.text = auth.currentUser?.uid?.let { UserManagerV.get(it)?.username }
        buttonCerrarSes.setOnClickListener{
            auth.signOut()
            startActivity(Intent(this,SignInActivity::class.java))
        }
        /*buttonElimCuenta.setOnClickListener{
            val view = View.inflate(this@MngAccount, R.layout.dialog_view, null)

            val builder = AlertDialog.Builder(this@MngAccount)
            builder.setView(view)

            val dialog = builder.create()
            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(false)

            view.btnconfirm.setOnClickListener {


            }
            view.btnCancel.setOnClickListener {
                    =
            }

        }
        //auth.currentUser.uid  id del usuario
        //auth!!.currentUser?.let { UserManagerV.remove(it) }*/
    }

}


