package com.sarthak.payu.utils



import android.content.Context
import android.content.Intent
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.sarthak.payu.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class GoogleSignInResult(
    val displayName: String?,
    val email: String?,
    val isNewUser: Boolean
)

@Singleton
class GoogleAuthClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val auth = FirebaseAuth.getInstance()
    private val oneTapClient: SignInClient = Identity.getSignInClient(context)

    private val webClientId: String
        get() = context.getString(R.string.default_web_client_id)

    suspend fun getSignInIntentSender(): IntentSenderRequest? {
        return try {
            val result = oneTapClient.beginSignIn(
                BeginSignInRequest.builder()
                    .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                            .setSupported(true)
                            .setServerClientId(webClientId)
                            .setFilterByAuthorizedAccounts(false)
                            .build()
                    )
                    .setAutoSelectEnabled(false)
                    .build()
            ).await()
            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
        } catch (e: Exception) {
            null
        }
    }

    fun getLegacySignInIntent(): Intent? {
        return try {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
            GoogleSignIn.getClient(context, options).signInIntent
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signInWithIntent(intent: Intent): GoogleSignInResult? {
        return signInWithOneTapIntent(intent)
    }

    suspend fun signInWithLegacyIntent(intent: Intent): GoogleSignInResult? {
        return try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(intent).getResult(ApiException::class.java)
            val googleIdToken = account.idToken ?: return null
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user ?: return null
            GoogleSignInResult(
                displayName = user.displayName,
                email = user.email,
                isNewUser = authResult.additionalUserInfo?.isNewUser == true
            )
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun signInWithOneTapIntent(intent: Intent): GoogleSignInResult? {
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val googleIdToken = credential.googleIdToken ?: return null
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user ?: return null
            GoogleSignInResult(
                displayName = user.displayName,
                email = user.email,
                isNewUser = authResult.additionalUserInfo?.isNewUser == true
            )
        } catch (e: Exception) {
            null
        }
    }

    fun signOut() {
        auth.signOut()
        oneTapClient.signOut()
    }

    fun isSignedIn(): Boolean = auth.currentUser != null
    fun getCurrentUserName(): String? = auth.currentUser?.displayName
    fun getCurrentUserEmail(): String? = auth.currentUser?.email
}
