package com.example.snapshots

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.snapshots.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import java.util.Arrays

class MainActivity : AppCompatActivity() {

    private val RC_SING_IN = 21

    private lateinit var mBinding: ActivityMainBinding

    private lateinit var mActiveFragment: Fragment
    private lateinit var mFragmentManager: FragmentManager

    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    private var mFirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupAuth()
        setupBottomNav()
    }

    private fun setupAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser
            //Si el usuario no se ha logeado, se lanzara la vista donde puede hacerlo
            if(user == null){
                startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    //.setIsSmartLockEnabled(false) //Desactiva SmartLock porque ocaciona el error de FirebaseUI
                    //Aqui se puden añadir todos lo proveedores que queremos
                    .setAvailableProviders(
                        Arrays.asList(
                            //Acceso con correo y contraseña
                            AuthUI.IdpConfig.EmailBuilder().build(),
                            //Acceso con google
                            AuthUI.IdpConfig.GoogleBuilder().build())
                    )
                    .build(), RC_SING_IN)
            }
        }
    }

    //Al hacerlo de este modo no se pierden los progresos de los fragmentos
    private fun setupBottomNav(){
        mFragmentManager = supportFragmentManager

        val homeFragment = HomeFragment()
        val addFragment = AddFragment()
        val profileFragment = ProfileFragment()

        mActiveFragment = homeFragment

        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, profileFragment, ProfileFragment::class.java.name)
            .hide(profileFragment)
            .commit()
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, addFragment, AddFragment::class.java.name)
            .hide(addFragment)
            .commit()
        mFragmentManager.beginTransaction()
            .add(R.id.hostFragment, homeFragment, HomeFragment::class.java.name)
            .commit()

        mBinding.bottomNav.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.action_home -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(homeFragment).commit()
                    mActiveFragment = homeFragment
                    true
                }
                R.id.action_add -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(addFragment).commit()
                    mActiveFragment = addFragment
                    true
                }
                R.id.action_frofile -> {
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(profileFragment).commit()
                    mActiveFragment = profileFragment
                    true
                }
                else -> false
            }
        }
    }

    //Se añade el Listener(mAuthListener) en el ciclo de vida
    override fun onResume() {
        super.onResume()
        mFirebaseAuth?.addAuthStateListener(mAuthListener)
    }

    //Para liberar recursos
    override fun onPause() {
        super.onPause()
        mFirebaseAuth?.removeAuthStateListener(mAuthListener)
    }

    //Se usara para tratar el caso de Firebase UID (El id que se le genera a cada usuario en firebase = UID de usuario)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SING_IN){
            //Se valida si el usuario pudo iniciar sesion, y regreso a esta pantalla
            if (requestCode == RESULT_OK){
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
            } else {
                //Esto se hace para validar si el usuario cancelo el inico de sesion
                if(IdpResponse.fromResultIntent(data) == null){
                    finish()
                }
            }
        }
    }
}