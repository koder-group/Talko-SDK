package com.koder.ellenlibrary

import android.util.Log
import com.koder.ellen.Client
import com.koder.ellen.CompletionCallback
import com.koder.ellen.data.Result
import com.koder.ellen.model.User
import com.koder.ellen.screen.UserSearchScreen
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class myUserSearchScreen: UserSearchScreen() {

    companion object {
        const val TAG = "myUserSearchScreen"
        private val client = Client()
    }

    override fun searchUser(searchString: String, callback: CompletionCallback) {
        // TODO Add implementation to search for user asynchronously
        // Sample
        GlobalScope.launch {
            client.findUsers(searchString, object: CompletionCallback() {
                override fun onCompletion(result: Result<Any>) {
                    if(result is Result.Success) {
                        callback.onCompletion(com.koder.ellen.data.Result.Success((result.data as MutableList<User>).toList()))
                    }
                }
            })
        }
    }
}