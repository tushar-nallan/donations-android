package com.tramsun.donations

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import java.util.*

class LoginActivity : AppCompatActivity() {

    private val TAG: String = "LoginActivity"

    //Request Codes
    private val FACEBOOK_LOG_IN_CODE = 1
    private val GOOGLE_LOG_IN_CODE = 2

    //Init views
    private lateinit var facebookSignInButton: Button
    private lateinit var googleSignInButton: Button

    //Facebook Callback manager
    private var callbackManager: CallbackManager? = null

    //Google Sign In Client
    private var googleSignInClient: GoogleSignInClient? = null

    //Firebase Auth object
    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initiate firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //Initiate Callback Manager
        callbackManager = CallbackManager.Factory.create()

        //Get sign in button ids
        facebookSignInButton = findViewById(R.id.facebook_login)
        googleSignInButton = findViewById(R.id.google_login)

        //Facebook Callback registration
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                Log.d(TAG, "Login Success")
                handleFacebookAccessToken(result.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "Login Cancelled")
            }

            override fun onError(error: FacebookException?) {
                Log.d(TAG, "Login Failed")
            }
        })

        //Listener for Facebook sign in button
        facebookSignInButton.setOnClickListener { LoginManager.getInstance().logInWithReadPermissions(this@LoginActivity, Arrays.asList("email", "public_profile")) }

        //Listener for Google Sign In button
        googleSignInButton.setOnClickListener { googleSignIn() }

        //Configure Google Sign In
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this@LoginActivity, googleSignInOptions)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {

        Log.d(TAG, "facebookAccessToken" + token)

        val facebookCredentials: AuthCredential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth!!.signInWithCredential(facebookCredentials).addOnCompleteListener(this@LoginActivity) { facebookSignInTask ->
            //                NgoListActivity
            if (facebookSignInTask.isSuccessful) {
                Log.d(TAG, "Facebook SignIn Success; Current User" + firebaseAuth!!.currentUser)
                intent = Intent(this@LoginActivity, NgoListActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                Log.e(TAG, "Firebase authentication with Facebook SignIn credentials Failed", facebookSignInTask.exception)
            }
        }
    }

    //Start Google Sign In
    private fun googleSignIn() {
        val signInIntent = googleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOG_IN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOG_IN_CODE) {
            val signInGoogleTask: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            //If Google Sign In successful, authenticate with Firebase
            try {
                val googleAccount: GoogleSignInAccount = signInGoogleTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(googleAccount)
            } catch (e: ApiException) {
                Log.e(TAG, "Failed to login google", e)
            }
        } else {
            callbackManager?.onActivityResult(requestCode, resultCode, data)
        }
    }

    //Authenticate Google Account with Firebase
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val googleCredentials = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth!!.signInWithCredential(googleCredentials)
                .addOnCompleteListener(this@LoginActivity, object : OnCompleteListener<AuthResult> {
                    override fun onComplete(googleSignInTask: Task<AuthResult>) {
                        if (googleSignInTask.isSuccessful) {
                            Log.d(TAG, "Google SignIn Success; Current User" + firebaseAuth!!.currentUser)
                            intent = Intent(this@LoginActivity, NgoListActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish()
                        } else {
                            Log.e(TAG, "Firebase authentication with Google SignIn credentials Failed", googleSignInTask.exception)
                        }
                    }
                })
    }
}

