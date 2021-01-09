package com.sourav.multiplesmssender

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.sourav.multiplesmssender.MySMSservice.Companion.startActionSMS
import com.wafflecopter.multicontactpicker.ContactResult
import com.wafflecopter.multicontactpicker.LimitColumn
import com.wafflecopter.multicontactpicker.MultiContactPicker
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    var results: List<ContactResult> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_CONTACTS
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) { /* ... */
                    }

                    override fun onPermissionRationaleShouldBeShown(
                            permissions: List<PermissionRequest>, token: PermissionToken) { /* ... */
                    }
                }).check()
        if (!isAccessibilityOn(applicationContext)) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        button_choose_contacts.setOnClickListener(View.OnClickListener {
            MultiContactPicker.Builder(this@MainActivity) //Activity/fragment context
                    .hideScrollbar(false)
                    .showTrack(true)
                    .searchIconColor(Color.WHITE)
                    .setChoiceMode(MultiContactPicker.CHOICE_MODE_MULTIPLE)
                    .handleColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
                    .bubbleColor(ContextCompat.getColor(this@MainActivity, R.color.colorAccent))
                    .bubbleTextColor(Color.WHITE)
                    .setTitleText("Select Contacts")
                    .setLoadingType(MultiContactPicker.LOAD_ASYNC)
                    .limitToColumn(LimitColumn.NONE)
                    .setActivityAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out)
                    .showPickerForResult(CONTACT_PICKER_REQUEST)
        })
        btn_sms.setOnClickListener {
            startActionSMS(applicationContext, txt_message.text.toString(),
                    txt_count.text.toString(), results)
        }

        val intent = IntentFilter("my.own.broadcast")
        LocalBroadcastManager.getInstance(this).registerReceiver(myLocalBroadcastReceiver, intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CONTACT_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                results = MultiContactPicker.obtainResult(data)
                val names = StringBuilder(results[0].displayName)
                for (j in results.indices) {
                    if (j != 0) names.append(", ").append(results[j].displayName)
                }
                txt_mobile_numbers!!.setText(names)
                Log.d("MyTag", results[0].displayName)
            } else if (resultCode == RESULT_CANCELED) {
                println("User closed the picker without selecting items.")
            }
        }
    }

    private val myLocalBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val result = intent.getStringExtra("result")
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAccessibilityOn(context: Context): Boolean {
        var accessibilityEnabled = 0
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.applicationContext.contentResolver,
                    Settings.Secure.ACCESSIBILITY_ENABLED)
        } catch (ignored: SettingNotFoundException) {
        }
        val colonSplitter = SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(context.applicationContext.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            if (settingValue != null) {
                colonSplitter.setString(settingValue)
                while (colonSplitter.hasNext()) {
                    val accessibilityService = colonSplitter.next()
                }
            }
        } else {
            Log.v("", "***ACCESSIBILITY IS DISABLED***")
        }
        return false
    }

    companion object {
        private const val CONTACT_PICKER_REQUEST = 202
    }
}