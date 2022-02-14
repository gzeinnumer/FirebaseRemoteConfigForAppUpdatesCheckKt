package com.gzeinnumer.firebaseremoteconfigforappupdatescheckkt

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

class MainActivity : AppCompatActivity() {
    private val TAG = "asdafasfasf"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkForUpdate()
    }

    private fun checkForUpdate() {
        val appVersion: String = getAppVersion(this)
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        val currentVersion =
            remoteConfig.getString("min_version_of_app")
        val minVersion =
            remoteConfig.getString("latest_version_of_app")

        Log.d(TAG, "checkForUpdate: " + currentVersion + "_" + minVersion)
        if (!TextUtils.isEmpty(minVersion) && !TextUtils.isEmpty(appVersion) && checkMandateVersionApplicable(
                getAppVersionWithoutAlphaNumeric(minVersion),
                getAppVersionWithoutAlphaNumeric(appVersion)
            )
        ) {
            onUpdateNeeded(true)
        } else if (!TextUtils.isEmpty(currentVersion) && !TextUtils.isEmpty(appVersion) && !TextUtils.equals(
                currentVersion,
                appVersion
            )
        ) {
            onUpdateNeeded(false)
        } else {
            moveForward()
        }
    }

    private fun checkMandateVersionApplicable(
        minVersion: String,
        appVersion: String
    ): Boolean {
        return try {
            val minVersionInt = minVersion.toInt()
            val appVersionInt = appVersion.toInt()
            minVersionInt > appVersionInt
        } catch (exp: NumberFormatException) {
            false
        }
    }

    private fun getAppVersion(context: Context): String {
        var result: String? = ""
        try {
            result = context.packageManager
                .getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.message?.let { Log.e("TAG", it) }
        }
        return result ?: ""
    }

    private fun getAppVersionWithoutAlphaNumeric(result: String): String {
        var version_str = ""
        version_str = result.replace(".", "")
        return version_str
    }

    private fun onUpdateNeeded(isMandatoryUpdate: Boolean) {
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle(getString(R.string.update_app))
            .setCancelable(false)
            .setMessage(if (isMandatoryUpdate) getString(R.string.dialog_update_available_message) else "A new version is found on Play store, please update for better usage.")
            .setPositiveButton(getString(R.string.update_now))
            { dialog, which ->
                openAppOnPlayStore(this, null)
            }

        if (!isMandatoryUpdate) {
            dialogBuilder.setNegativeButton(getString(R.string.later)) { dialog, which ->
                moveForward()
                dialog?.dismiss()
            }.create()
        }
        val dialog: AlertDialog = dialogBuilder.create()
        dialog.show()
    }

    private fun moveForward() {
        Toast.makeText(this, "Next Page Intent", Toast.LENGTH_SHORT).show()
    }

    fun openAppOnPlayStore(ctx: Context, package_name: String?) {
        var package_name = package_name
        if (package_name == null) {
            package_name = ctx.packageName
        }
        val uri = Uri.parse("market://details?id=$package_name")
        openURI(ctx, uri, "Play Store not found in your device")
    }

    fun openURI(
        ctx: Context,
        uri: Uri?,
        error_msg: String?
    ) {
        val i = Intent(Intent.ACTION_VIEW, uri)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        if (ctx.packageManager.queryIntentActivities(i, 0).size > 0) {
            ctx.startActivity(i)
        } else if (error_msg != null) {
            Toast.makeText(this, error_msg, Toast.LENGTH_SHORT).show()
        }
    }

}