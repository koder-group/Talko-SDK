package com.koder.ellen.core

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.koder.ellen.model.ClientConfiguration
import com.koder.ellen.model.CurrentUser

class Prefs (context: Context) {
    companion object {
        const val TAG = "Prefs"
        lateinit var prefs: SharedPreferences

        const val PREFS_FILENAME = "com.koder.ellen.prefs"
        const val APP_ID = "AppId"  // Application Id (Tenant Id?)
        const val USER_TOKEN = "UserToken"  // User token to access platform API
        const val EXTERNAL_USER_ID = "ExternalUserId"   // User Id
        const val CLIENT_CONFIGURATION = "ClientConfiguration"
        const val CURRENT_USER = "CurrentUser"
        const val NOTIFICATION_TOKEN = "NotificationToken"
    }

    val gson = Gson()

    init {
        prefs = context.getSharedPreferences(PREFS_FILENAME, 0)
    }

    fun resetUser() {
        prefs.edit().putString(APP_ID, "").apply()
        prefs.edit().putString(USER_TOKEN, "").apply()
        prefs.edit().putString(EXTERNAL_USER_ID, "").apply()
        prefs.edit().putString(CURRENT_USER, "")
    }

    var appId: String?
        get() = prefs.getString(APP_ID, null)
        set(appId: String?) = prefs.edit().putString(APP_ID, appId).apply()
    // Alias of appId
    var tenantId: String?
        get() = prefs.getString(APP_ID, null)
        set(appId: String?) = prefs.edit().putString(APP_ID, appId).apply()
    var userToken: String?
        get() = prefs.getString(USER_TOKEN, null)
        set(token: String?) = prefs.edit().putString(USER_TOKEN, token).apply()
    var externalUserId: String?
        get() = prefs.getString(EXTERNAL_USER_ID, null)
        set(userId: String?) = prefs.edit().putString(EXTERNAL_USER_ID, userId).apply()
    var clientConfiguration: ClientConfiguration?
        get() {
            val json = prefs.getString(CLIENT_CONFIGURATION, null)
            return gson.fromJson(json, ClientConfiguration::class.java)
        }
        set(clientConfiguration: ClientConfiguration?)
        {
            val jsonString = gson.toJson(clientConfiguration)
            prefs.edit().putString(CLIENT_CONFIGURATION, jsonString).apply()
        }
    var currentUser: CurrentUser?
        get() {
            val json = prefs.getString(CURRENT_USER, null)
            return gson.fromJson(json, CurrentUser::class.java)
        }
        set(currentUser: CurrentUser?)
        {
            val jsonString = gson.toJson(currentUser)
            prefs.edit().putString(CURRENT_USER, jsonString).apply()
        }
    var notificationToken: String?
        get() = prefs.getString(NOTIFICATION_TOKEN, null)
        set(token: String?) = prefs.edit().putString(NOTIFICATION_TOKEN, token).apply()
}