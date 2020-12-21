package com.example.guardiana

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.github.appintro.model.SliderPagerBuilder


class AppIntroduction : AppIntro() {

    private lateinit var manager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        manager = PreferencesManager(this)
        if (manager.isFirstRun()) {
            setSlideSettings()
            showIntroSlides()
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
                slideNumber = 4,
                required = true)
    }

    private fun showIntroSlides() {
        manager.setFirstRun(false)
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
                .title(getString(R.string.slide_three_top_text))
                .description(getString(R.string.slide_three_down_text))
                .imageDrawable(R.drawable.splashicon)
                .backgroundColor(getColor(R.color.slide_three))
                .build()

        val pageFour = SliderPagerBuilder()
                .title(getString(R.string.slide_four_top_text))
                .description(getString(R.string.slide_four_down_text))
                .imageDrawable(R.drawable.splashicon)
                .backgroundColor(getColor(R.color.slide_four))
                .build()

        addSlide(AppIntroFragment.newInstance(pageOne))
        addSlide(AppIntroFragment.newInstance(pageTwo))
        addSlide(AppIntroFragment.newInstance(pageThree))
        addSlide(AppIntroFragment.newInstance(pageFour))
    }

    private fun goToMain() {
        finish()
       // startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        goToMain()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        goToMain()
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
        Log.d("Hello", "Changed")
    }

    override fun onUserDeniedPermission(permissionName: String) {
        // User pressed "Deny" on the permission dialog
        setImmersiveMode()
        manager.setFirstRun(true)
    }
    override fun onUserDisabledPermission(permissionName: String) {
        // User pressed "Deny" + "Don't ask again" on the permission dialog
        setImmersiveMode()
        manager.setFirstRun(true)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        setImmersiveMode()
    }
}