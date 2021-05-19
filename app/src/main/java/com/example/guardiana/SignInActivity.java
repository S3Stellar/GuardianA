package com.example.guardiana;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.guardiana.model.Profile;
import com.example.guardiana.repository.UserMarkerRepository;
import com.example.guardiana.repository.firebase.ProfileFirebaseRepository;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.github.ybq.android.spinkit.style.ThreeBounce;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

import static android.content.ContentValues.TAG;

public class SignInActivity extends Activity {

    private PreferencesManager manager;
    private EditText email;
    private EditText password;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int GGO_SIGNIN_CODE = 666;
    private static final int PHONE_LOGIN_CODE = 665;
    private static final int SIGNUP_CODE = 664;
    private FirebaseAuth mAuth;
    private long pressTime = 0;
    private ProfileFirebaseRepository profileFirebaseRepository;
    private UserMarkerRepository userMarkerRepository;
    private Vibrator mVibrator;
    private Toast toast;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        manager = new PreferencesManager(this);
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        mAuth = FirebaseAuth.getInstance();
        userMarkerRepository = UserMarkerRepository.getInstance();
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        toast = new Toast(getApplicationContext());

        progressBar = (ProgressBar)findViewById(R.id.loadingBar);
        progressBar.setIndeterminateDrawable(new ThreeBounce());

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
            mVibrator.vibrate(80);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GGO_SIGNIN_CODE);
        });
        findViewById(R.id.forgot_pass_butt).setOnClickListener(v -> sendPasswordResetEmail());
        findViewById(R.id.phone_butt).setOnClickListener(this::phoneLogin);
        findViewById(R.id.reg_now).setOnClickListener(this::signUp);
        findViewById(R.id.plus_butt).setOnClickListener(this::signUp);
    }

    // Phone butt clicked
    private void phoneLogin(View view) {
        mVibrator.vibrate(80);
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
                PHONE_LOGIN_CODE);
    }

    // Forgot pass clicked - sends email for pass recovery
    private void sendPasswordResetEmail() {
        mVibrator.vibrate(80);
        if (email.getText().toString().isEmpty()) {
            Toast.makeText(SignInActivity.this, "Please type your email first",
                    Toast.LENGTH_SHORT).show();
        } else {
            mAuth.sendPasswordResetEmail(email.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, "Check your email box!",
                                    Toast.LENGTH_SHORT).show();
                            Log.d("TAG", "Email sent.");
                        }
                    });

        }
    }

    // Sign up clicked - Plus butt / Register Now
    private void signUp(View view) {
        mVibrator.vibrate(80);
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
                SIGNUP_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Phone butt / +Plus butt / Register Now
        if (requestCode == PHONE_LOGIN_CODE || requestCode == SIGNUP_CODE) {

            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                loginSuccess(user);
                profileFirebaseRepository = ProfileFirebaseRepository.getInstance();
                checkNewUserAndSave(response, user);
                // ...
            } else {
                manager.setLoggedIn(false);
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
            }
        }

        // Google butt result
        if (requestCode == GGO_SIGNIN_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                manager.setLoggedIn(false);
            }
        }
    }

    private void checkNewUserAndSave(IdpResponse response, FirebaseUser user) {
        if (response.isNewUser()) {
            Profile profile;
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                App.setUserId(user.getPhoneNumber());
                profile = new Profile(user.getPhoneNumber(), 0, new Date());
            } else {
                profile = new Profile(user.getEmail(), user.getDisplayName(), 0, new Date());
            }
            profileFirebaseRepository.save(
                    profile, data1 -> Log.i(TAG, "onActivityResult: profile saved success"),
                    e -> Log.i(TAG, "onActivityResult: profile saved failed"));
        }
    }

    // Update global user id, update shared pref, finish & start home act'
    private void loginSuccess(FirebaseUser user) {
        App.setUserId(user.getEmail() != null && !user.getEmail().isEmpty() ? user.getEmail() : user.getPhoneNumber());
        manager.setLoggedIn(true);

        finish();
        startActivity(new Intent(this, HomeActivity.class));
    }

    // Login with Google button
    private void firebaseAuthWithGoogle(String idToken) {
        progressBar.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        loginSuccess(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()));

                        if (Objects.requireNonNull(task.getResult().getAdditionalUserInfo()).isNewUser()) {
                            profileFirebaseRepository = ProfileFirebaseRepository.getInstance();
                            profileFirebaseRepository.save(new Profile(Objects.requireNonNull(task.getResult().getAdditionalUserInfo().getProfile()).get("email") + "",
                                            task.getResult().getAdditionalUserInfo().getProfile().get("name") + "",
                                            0,
                                            new Date()), data1 -> Log.i(TAG, "onActivityResult: profile saved success"),
                                    e -> Log.i(TAG, "onActivityResult: profile saved failed"));
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(SignInActivity.this, "Wrong email or password.",
                                Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                });
    }


    // Login using login button passing email & password
    public void onLoginClick(View view) {
        mAuth = FirebaseAuth.getInstance();
        mVibrator.vibrate(80);
        CircularProgressButton r = findViewById(R.id.cirLoginButton);

        if (!email.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
            r.startAnimation();
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    loginSuccess(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()));
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(SignInActivity.this, "Wrong email or password.",
                            Toast.LENGTH_SHORT).show();
                }
                r.revertAnimation();
            });
        } else {
            showAToast("Please enter login details.");
        }
    }

    public void showAToast(String st) { //"Toast toast" is declared in the class
        try {
            toast.getView().isShown();     // true if visible
            toast.setText(st);
        } catch (Exception e) {         // invisible if exception
            toast = Toast.makeText(this, st, Toast.LENGTH_SHORT);
        }
        toast.show();  //finally display it
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

    // Disables back button
    @Override
    public void onBackPressed() {
    }
}