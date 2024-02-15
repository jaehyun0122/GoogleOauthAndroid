package com.jawon.han.googleloginwithrestapi

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationService.TokenResponseCallback


class RedirectActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("jay", "redirect create")
        /*val redirectUri = intent.data

        Log.d("jay", "redirect ${redirectUri?.getQueryParameter("code")}")*/
        val resp = AuthorizationResponse.fromIntent(intent)
        val ex = AuthorizationException.fromIntent(intent)
        if (resp != null) {
            // authorization completed
            Log.d("jay", "${resp}")
        } else {
            // authorization failed, check ex for more details
            Log.d("jay", "${ex}")
        }

        AuthorizationService(this).performTokenRequest(
            resp!!.createTokenExchangeRequest()
        ) { resp, ex ->
            if (resp != null) {
                Log.d("jay", "${resp}")
            } else {
                // authorization failed, check ex for more details
                Log.d("jay", "${ex}")
            }
        }


        finish()
    }
}