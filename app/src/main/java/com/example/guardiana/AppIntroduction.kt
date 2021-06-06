package com.example.guardiana

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.guardiana.objectdetect.DetectorActivity
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.github.appintro.model.SliderPagerBuilder
import com.google.android.material.snackbar.Snackbar


class AppIntroduction : AppIntro() {

    private lateinit var manager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        manager = PreferencesManager(this)

        if (manager.isFirstRun()) {
            setSlideSettings()
            showIntroSlides()
        } else if (!manager.isLoggedIn()) {
            goToLogin()
        } else {
            goToMain()
        }
    }

    private fun setSlideSettings() {
        setImmersiveMode()
        setTransformer(AppIntroPageTransformerType.Depth)
        //isColorTransitionsEnabled = true
        isVibrate = true
        vibrateDuration = 50L
        isSystemBackButtonLocked = true
        isWizardMode = true
        askForPermissions(
                permissions = arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET,
                        Manifest.permission.CAMERA
                ),
                slideNumber = 3,
                required = true)
    }

    private fun showIntroSlides() {
        val pageOne = SliderPagerBuilder()
                .title(getString(R.string.slide_one_top_text))
                .description(getString(R.string.slide_one_down_text))
                .imageDrawable(R.drawable.splashicon)
                .backgroundColor(getColor(R.color.slide_one))
                .build()

        val pageTwo = SliderPagerBuilder()
                .title(getString(R.string.slide_two_top_text))
                .description(getString(R.string.slide_two_down_text))
                .imageDrawable(R.drawable.splashicon)
                .backgroundColor(getColor(R.color.slide_two))
                .build()

        val pageThree = SliderPagerBuilder()
                .title(getString(R.string.slide_four_top_text))
                .description(getString(R.string.slide_four_down_text))
                .imageDrawable(R.drawable.splashicon)
                .backgroundColor(getColor(R.color.slide_three))
                .build()

        val pageFour = SliderPagerBuilder()
                .title(getString(R.string.slide_three_top_text))
                .description(getString(R.string.slide_three_down_text))
                .imageDrawable(R.drawable.splashicon)
                .backgroundColor(getColor(R.color.slide_four))
                .build()


        addSlide(AppIntroFragment.newInstance(pageOne))
        addSlide(AppIntroFragment.newInstance(pageTwo))
        addSlide(AppIntroFragment.newInstance(pageThree))
        addSlide(AppIntroFragment.newInstance(pageFour))
    }

    private fun goToMain() {
        startActivity(Intent(this, DetectorActivity::class.java))
        finish()
    }


    private fun goToLogin() {
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    // No skip button (using wizardMode)
    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        goToMain()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        manager.setFirstRun(false)

        if (!manager.isLoggedIn()) {
            goToLogin()
        } else {
            goToMain()
        }
    }


    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
    }

    override fun onUserDeniedPermission(permissionName: String) {
        // User pressed "Deny" on the permission dialog
        setImmersiveMode()
        manager.setFirstRun(true)
    }

    override fun onUserDisabledPermission(permissionName: String) {
        // User pressed "Deny" + "Don't ask again" on the permission dialog
        setImmersiveMode()

        val snackbar: Snackbar = Snackbar
                .make(findViewById(android.R.id.content), """You have previously declined this permission.
You must approve this permission in "Permissions" in the app settings on your device.""", Snackbar.LENGTH_LONG)
                .setAction("Settings") {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)))
                }
        val snackbarView = snackbar.view
        val textView = snackbarView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView
        textView.maxLines = 5 // show multiple line

        snackbar.show()
        manager.setFirstRun(true)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        setImmersiveMode()
    }


    override fun onBackPressed() {
        // Disabling back button to avoid going back to mainActivity
    }
}