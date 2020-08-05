package com.koder.ellen.core

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.koder.ellen.model.ClientConfiguration
import com.koder.ellen.model.EllenUser
import org.json.JSONObject


internal class Prefs (context: Context) {
    companion object {
        const val TAG = "Prefs"
        private lateinit var prefs: SharedPreferences

        const val PREFS_FILENAME = "com.koder.ellen.prefs"
        const val APP_ID = "AppId"  // Application Id (Tenant Id?)
        const val USER_TOKEN = "UserToken"  // User token to access platform API
        const val EXTERNAL_USER_ID = "ExternalUserId"   // User Id
        const val CLIENT_CONFIGURATION = "ClientConfiguration"
        const val CURRENT_USER = "CurrentUser"
        const val NOTIFICATION_TOKEN = "NotificationToken"
        const val LAST_READ_MAP = "LastReadMap"
    }

    val gson = GsonBuilder().disableHtmlEscaping().create()

    init {
        prefs = context.getSharedPreferences(PREFS_FILENAME, 0)
    }

    fun resetUser() {
        prefs.edit().putString(APP_ID, "").apply()
        prefs.edit().putString(USER_TOKEN, "").apply()
        prefs.edit().putString(EXTERNAL_USER_ID, "").apply()
        prefs.edit().putString(CLIENT_CONFIGURATION, "").apply()
        prefs.edit().putString(CURRENT_USER, "").apply()
        prefs.edit().putString(NOTIFICATION_TOKEN, "").apply()
//        prefs.edit().putString(LAST_READ_MAP, "").apply()
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
    // Alias of externalUserId
    var userId: String?
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
    var currentUser: EllenUser?
        get() {
            val json = prefs.getString(CURRENT_USER, null)
            return gson.fromJson(json, EllenUser::class.java)
        }
        set(currentUser: EllenUser?)
        {
            val jsonString = gson.toJson(currentUser)
            prefs.edit().putString(CURRENT_USER, jsonString).apply()
        }
    var notificationToken: String?
        get() = prefs.getString(NOTIFICATION_TOKEN, null)
        set(token: String?) = prefs.edit().putString(NOTIFICATION_TOKEN, token).apply()

    // New message indicator
    // Map of <Conversation Id, Timestamp>
    fun setConversationLastRead(conversationId: String, timeRead: Long) {
        // Get map
        var jsonString: String = prefs.getString(LAST_READ_MAP, JSONObject().toString())!!
        val listType = object : TypeToken<HashMap<String, Long>>() {}.type
        val map: HashMap<String, Long> = Gson().fromJson(jsonString, listType)
        // Set map
        map.set(conversationId, timeRead)
        jsonString = Gson().toJson(map)
        prefs.edit().putString(LAST_READ_MAP, jsonString).apply()
    }

    fun getConversationLastRead(conversationId: String): Long? {
        // Get map
        var jsonString: String = prefs.getString(LAST_READ_MAP, JSONObject().toString())!!
        if(jsonString.isNullOrBlank()) {
            return 0
        }
        val listType = object : TypeToken<HashMap<String, Long>>() {}.type
        val map: HashMap<String, Long> = Gson().fromJson(jsonString, listType)
        // Read map
        return map.get(conversationId)
    }
}