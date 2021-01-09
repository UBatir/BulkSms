package com.sourav.multiplesmssender

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.wafflecopter.multicontactpicker.ContactResult
import java.net.URLEncoder
import java.util.*

class MySMSservice : IntentService("MySMSservice") {
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_SMS == action) {
                val message= intent.getStringArrayListExtra(MESSAGE)
                val count = intent.getStringExtra(COUNT)
                val mobile_number = intent.getStringArrayExtra(MOBILE_NUMBER)
                handleActionSMS(message, count, mobile_number)
            }
        }
    }

    private fun handleActionSMS(message: ArrayList<String>, count: String, mobile_number: Array<String>) {
        try {
            if (mobile_number.isNotEmpty()) {
                for (j in mobile_number.indices) {
                    for (i in 0 until count.toInt()) {
                        val smsManager = SmsManager.getDefault()
                        smsManager.sendMultipartTextMessage(mobile_number[j], null, message,
                                null, null)
                        sendBroadcastMessage("Result " + (i + 1) + ": Message sent to " +
                                mobile_number[j] + " successfully")
                    }
                }
            }
        } catch (ignored: Exception) {
        }
    }

    private fun sendBroadcastMessage(message: String) {
        val localIntent = Intent("my.own.broadcast")
        localIntent.putExtra("result", message)
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)
    }

    companion object {
        private const val ACTION_SMS = "com.sourav.multiplesmssender.action.FOO"
        private const val ACTION_WHATSAPP = "com.sourav.multiplesmssender.action.BAZ"
        private const val MESSAGE = "com.sourav.multiplesmssender.extra.PARAM1"
        private const val COUNT = "com.sourav.multiplesmssender.extra.PARAM2"
        private const val MOBILE_NUMBER = "com.sourav.multiplesmssender.extra.PARAM3"
        private const val IS_EACH_WORD = "com.sourav.multiplesmssender.extra.PARAM4"

        @JvmStatic
        fun startActionSMS(context: Context, message: String?, count: String?,
                           mobile_numbers: List<ContactResult>) {
            val numbers: MutableList<String> = ArrayList()
            for (i in mobile_numbers.indices) {
                numbers.add(mobile_numbers[i].phoneNumbers[0].number)
            }
            val numberArray = numbers.toTypedArray()
            val intent = Intent(context, MySMSservice::class.java)
            intent.action = ACTION_SMS
            intent.putExtra(MESSAGE, message)
            intent.putExtra(COUNT, count)
            intent.putExtra(MOBILE_NUMBER, numberArray)
            context.startService(intent)
        }
    }
}
