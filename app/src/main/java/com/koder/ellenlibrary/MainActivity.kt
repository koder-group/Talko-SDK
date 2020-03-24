package com.koder.ellenlibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.koder.ellen.Client
import com.koder.ellen.Messenger

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val userToken = "eyJhbGciOiJIUzI1NiIsImtpZCI6IjE3YzRhYmQ4YTE3MjQ0OTdiZmViMjBiMWM0ZDhmYjU0IiwidHlwIjoiSldUIn0.eyJ0ZW5hbnRfaWQiOiJFRkI5NEM3RS03MUU5LTQwNkItOTA1OS02MUFDMDUyMjdGMUIiLCJ1c2VyX2lkIjoiRUQ0QjkzQTMtMzUwMS00QThCLUJGNEItRDc1NTYyOUVDNDkzIiwidXNlcl9uYW1lIjoiaGFwcHlhdGtvZGVyIiwicHJvZmlsZV9pbWFnZSI6Imh0dHBzOi8vZmlyZWJhc2VzdG9yYWdlLmdvb2dsZWFwaXMuY29tL3YwL2IvZWxsZW4tZmlyZWJhc2UtZXhhbXBsZS5hcHBzcG90LmNvbS9vL0F2YXRhcnMlMkZ1c2VyLTIyLnBuZz9hbHQ9bWVkaWEmdG9rZW49ODk3YjI2MWUtMjA3MC00OWE1LWIwZjQtYjdkZDExN2Y0M2IzIiwiZXhwIjoxNTg1MDI0MzcxLCJpc3MiOiJodHRwczovL2VsbGVuLmtvZGVyLmNvbS9hcGkvbWFuYWdlbWVudCIsImF1ZCI6Imh0dHBzOi8vZWxsZW4ua29kZXIuY29tL2FwaS9tZXNzYWdpbmcifQ.H6oHEPDqtVRwPAYhB5Eef1-GivHQVsnaQD2QRig0_Rs"
        val userId = "ed4b93a3-3501-4a8b-bf4b-d755629ec493"
        Messenger.set(userToken, userId)
//        Client.logout()
    }
}
