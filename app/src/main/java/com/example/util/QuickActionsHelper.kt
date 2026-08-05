package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object QuickActionsHelper {

    fun makeCall(context: Context, phoneNumber: String) {
        if (phoneNumber.isBlank()) {
            Toast.makeText(context, "Phone number is empty", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${phoneNumber.trim()}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(context: Context, whatsappNumber: String, message: String = "") {
        if (whatsappNumber.isBlank()) {
            Toast.makeText(context, "WhatsApp number is empty", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val cleanNum = whatsappNumber.replace(Regex("[^0-9+]"), "")
            val formatted = if (cleanNum.startsWith("+")) cleanNum.substring(1) else cleanNum
            val url = "https://api.whatsapp.com/send?phone=$formatted&text=${Uri.encode(message)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    fun openGoogleMaps(context: Context, gpsLocation: String, address: String) {
        try {
            val uriStr = if (gpsLocation.isNotBlank() && gpsLocation.contains(",")) {
                "geo:$gpsLocation?q=$gpsLocation(Customer Location)"
            } else if (address.isNotBlank()) {
                "geo:0,0?q=${Uri.encode(address)}"
            } else {
                Toast.makeText(context, "Location or address not provided", Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${Uri.encode(gpsLocation.ifBlank { address })}")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open maps", Toast.LENGTH_SHORT).show()
        }
    }
}
