package com.example.guardiana;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SignInActivity extends Activity {

    private PreferencesManager manager;
    private EditText email;
    private EditText password;
    private GoogleSignInClient mGoogleSignInClient;
    private final int GGL_SIGN_IN = 666;
    private final int PHONE_LOGIN = 665;
    private final int SIGNUP = 664;
    private FirebaseAuth mAuth;
    public long pressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        manager = new PreferencesManager(this);
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.googleWebClientLogin))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        setListeners();
    }

    // Activate listeners
    private void setListeners() {
        findViewById(R.id.cirLoginButton).setOnClickListener(v -> onLoginClick(v));

        findViewById(R.id.google_butt).setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GGL_SIGN_IN);
        });
        findViewById(R.id.forgot_pass_butt).setOnClickListener(v -> sendPasswordResetEmail());
        findViewById(R.id.phone_butt).setOnClickListener(this::phoneLogin);
        findViewById(R.id.reg_now).setOnClickListener(this::signUp);
        findViewById(R.id.plus_butt).setOnClickListener(this::signUp);
    }

    // Phone butt clicked
    private void phoneLogin(View view) {
        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                .Builder(R.layout.activity_sign_in)
                .setPhoneButtonId(R.id.phone_butt)
                // ...
                .build();

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.PhoneBuilder().build()))
                        // For testing use false, change later to true
                        .setIsSmartLockEnabled(true)
                        .setAuthMethodPickerLayout(customLayout)
                        .setTheme(R.style.AppTheme_NoActionBar)
                        .build(),
                PHONE_LOGIN);
    }

    // Forgot pass clicked - sends email for pass recovery
    private void sendPasswordResetEmail() {
        if (email.getText().toString().isEmpty()) {
            Toast.makeText(SignInActivity.this, "Please type your email first",
                    Toast.LENGTH_SHORT).show();
        } else {
            mAuth.sendPasswordResetEmail(email.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, "Email sent.",
                                    Toast.LENGTH_SHORT).show();
                            Log.d("TAG", "Email sent.");
                        }
                    });

        }
    }

    // Sign up clicked - Plus butt / Register Now
    private void signUp(View view) {
        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
                .Builder(R.layout.activity_sign_in)
                .setEmailButtonId(R.id.reg_now)

                // ...
                .build();

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().build()))
                        // For testing use false, change later to true
                        .setIsSmartLockEnabled(true)
                        .setAuthMethodPickerLayout(customLayout)
                        .setTheme(R.style.AppTheme_NoActionBar)
                        .build(),
                SIGNUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Phone butt / +Plus butt / Register Now
        if (requestCode == PHONE_LOGIN || requestCode == SIGNUP) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                loginSuccess();
                // ...
            } else {
                manager.setLoggedIn(false);
                //startLoginActivity();
                Log.i("TAG", "onActivityResult: FFFFFFFFFFFFFFFFFFFFFFFF");
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }

        // Google butt result
        if (requestCode == GGL_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                /*Toast.makeText(SignInActivity.this, "Sign in failed.",
                        Toast.LENGTH_SHORT).show();*/
                manager.setLoggedIn(false);
                Log.w("TAG", "Google sign in failed", e);
                // ...
            }
        }
    }

    // Update global user id, update shared pref, finish & start home act'
    private void loginSuccess() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        App.setUserId(user.getEmail() != null && !user.getEmail().isEmpty() ? user.getEmail() : user.getPhoneNumber());
        manager.setLoggedIn(user != null);
        finish();
        startActivity(new Intent(this, HomeActivity.class));
    }

    // Login with Google button
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            loginSuccess();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Wrong email or password.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Disables back button
    @Override
    public void onBackPressed() {

    }

    // Login using login button passing email & password
    public void onLoginClick(View view) {
        mAuth = FirebaseAuth.getInstance();
        Log.i("TAG", "onLoginClick: email = " + email.getText().toString() + ", pass = " + password.getText().toString());
        if (!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty())
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithEmail:success");
                    loginSuccess();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("TAG", "signInWithEmail:failure", task.getException());
                    Toast.makeText(SignInActivity.this, "Wrong email or password.",
                            Toast.LENGTH_SHORT).show();
                }
            });
    }

    // Hide keyboard on screen click
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            pressTime = System.currentTimeMillis();
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            long releaseTime = System.currentTimeMillis();
            if (releaseTime - pressTime < 200) {
                if (getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}