package com.jawon.han.googleloginwithrestapi

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenResponse

class MainActivity : AppCompatActivity() {

    private var authService: AuthorizationService? = null
    private var authState: AuthState? = null /*Google Login 인증 정보 관리*/
    private var idToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.loginBtn)
        button.setOnClickListener { requestLogin() }

        val logoutButton = findViewById<Button>(R.id.logout)
        logoutButton.setOnClickListener { requestLogout() }

        val refreshButton = findViewById<Button>(R.id.refresh)
        refreshButton.setOnClickListener { refreshToken() }

        /**
         * 1. 앱 접속시
         *  1)
         * 2. 로그인시 데이터 베이스에서 json data 확인.
         *  1) 없으면 로그인 버튼 활성화
         *  2) 있으면 authState에 json data setting
         *
          */


        authService = AuthorizationService(this)
        authState = AuthState.jsonDeserialize("{\"refreshToken\":\"1\\/\\/0evyhZVoGSIWWCgYIARAAGA4SNwF-L9IrxG3sehHXqx_couXfQ7t11XkCodwDnQT85uqPQZ7aOtTH2EmefK-Em18dZdFHjtAg6Rs\",\"scope\":\"openid https:\\/\\/www.googleapis.com\\/auth\\/youtubepartner https:\\/\\/www.googleapis.com\\/auth\\/userinfo.email\",\"lastAuthorizationResponse\":{\"request\":{\"configuration\":{\"authorizationEndpoint\":\"https:\\/\\/accounts.google.com\\/o\\/oauth2\\/v2\\/auth\",\"tokenEndpoint\":\"https:\\/\\/www.googleapis.com\\/oauth2\\/v4\\/token\"},\"clientId\":\"815911049800-hhs9l91v4f31in31sr5pt5ei94v0usst.apps.googleusercontent.com\",\"responseType\":\"code\",\"redirectUri\":\"com.jawon.han.googleloginwithrestapi:\\/oauth2redirect\",\"scope\":\"https:\\/\\/www.googleapis.com\\/auth\\/youtubepartner email\",\"state\":\"Y4WIzsF8j40SedQlpIQbWA\",\"nonce\":\"9Oi-4LepbE_5IOKpzAQ9vw\",\"codeVerifier\":\"db_FK-gS5J_maO6Nx_H4xNIcO5EMxy3q-YktM8LOsj8Vy61fv2VQ0oosGOnBoyahyBfoCBeKOdvAG9p1jTh7sQ\",\"codeVerifierChallenge\":\"xs8JuBE_CGQpczI_B8BHMcEyhah2l7EyJytL1LFHnJY\",\"codeVerifierChallengeMethod\":\"S256\",\"additionalParameters\":{}},\"state\":\"Y4WIzsF8j40SedQlpIQbWA\",\"code\":\"4\\/0AeaYSHCX2MlkSqOpvdnjcgBs_wYZtI9t1fA8QIYw8MONIbPlU_yz9XiLm_c64fn_S2CYKg\",\"scope\":\"email https:\\/\\/www.googleapis.com\\/auth\\/youtubepartner https:\\/\\/www.googleapis.com\\/auth\\/userinfo.email openid\",\"additional_parameters\":{\"authuser\":\"0\",\"hd\":\"selvashc.com\",\"prompt\":\"consent\"}},\"mLastTokenResponse\":{\"request\":{\"configuration\":{\"authorizationEndpoint\":\"https:\\/\\/accounts.google.com\\/o\\/oauth2\\/v2\\/auth\",\"tokenEndpoint\":\"https:\\/\\/www.googleapis.com\\/oauth2\\/v4\\/token\"},\"clientId\":\"815911049800-hhs9l91v4f31in31sr5pt5ei94v0usst.apps.googleusercontent.com\",\"nonce\":\"9Oi-4LepbE_5IOKpzAQ9vw\",\"grantType\":\"authorization_code\",\"redirectUri\":\"com.jawon.han.googleloginwithrestapi:\\/oauth2redirect\",\"authorizationCode\":\"4\\/0AeaYSHCX2MlkSqOpvdnjcgBs_wYZtI9t1fA8QIYw8MONIbPlU_yz9XiLm_c64fn_S2CYKg\",\"codeVerifier\":\"db_FK-gS5J_maO6Nx_H4xNIcO5EMxy3q-YktM8LOsj8Vy61fv2VQ0oosGOnBoyahyBfoCBeKOdvAG9p1jTh7sQ\",\"additionalParameters\":{}},\"token_type\":\"Bearer\",\"access_token\":\"ya29.a0AfB_byDWdW8bim5JsBcZJRoIxBIygVoJkavOSv8s-MjrGq4IbVHAy9nlNBokNjFkMIIT2b9UShBZY7Y_i6-NK_JeC7cWRCY_U2Cw6dsgQ5S7bKMwUxqFHEjzWVvMOovIZfK_R7A4QdJy1HlNkzXajNGvGNpEz9sDqY4baCgYKARcSARISFQHGX2MiO7NYfWrQDf4JYP10dWL8zA0171\",\"expires_at\":1707984817770,\"id_token\":\"eyJhbGciOiJSUzI1NiIsImtpZCI6ImVkODA2ZjE4NDJiNTg4MDU0YjE4YjY2OWRkMWEwOWE0ZjM2N2FmYzQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI4MTU5MTEwNDk4MDAtaGhzOWw5MXY0ZjMxaW4zMXNyNXB0NWVpOTR2MHVzc3QuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI4MTU5MTEwNDk4MDAtaGhzOWw5MXY0ZjMxaW4zMXNyNXB0NWVpOTR2MHVzc3QuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDI4MzI4NDc0MTkzMzUxMTY1MTUiLCJoZCI6InNlbHZhc2hjLmNvbSIsImVtYWlsIjoiamF5LmouamVvbmdAc2VsdmFzaGMuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF0X2hhc2giOiJrM1BjWFVWaXJqX1cyeThjczF2bUd3Iiwibm9uY2UiOiI5T2ktNExlcGJFXzVJT0twekFROXZ3IiwiaWF0IjoxNzA3OTgxMjE2LCJleHAiOjE3MDc5ODQ4MTZ9.dsszzvMYaVrREr28SwlbEJ-AOd-rHTYCjeH4O_cboQ63BNxOyhc57iSpye_b17IzKLcOxuf1dotJdaqzzPI6H49p5uM_uksj-Xs8NLTkvLvvz3ZDcMfbGAORJ-zyvmeZf2epFLOGVDjRXD-o4nk7_bvijNFXUrFwf1FvRLsOZxfhLrGHF0z-z7cxVIaKhRgA12Zc7cTjQDuhdn4odRSj1URH4gRTHqepWMadZwRWIcOp-Lok61bEm73m89n6U6jT-npioRi6tefQHdUW1NwPVVDvN-fRnbwP6yN767ONljtKMW8eL1n1iByW-W1YdW3VExqey-7bFG0EyBr8hC6n8A\",\"refresh_token\":\"1\\/\\/0evyhZVoGSIWWCgYIARAAGA4SNwF-L9IrxG3sehHXqx_couXfQ7t11XkCodwDnQT85uqPQZ7aOtTH2EmefK-Em18dZdFHjtAg6Rs\",\"scope\":\"openid https:\\/\\/www.googleapis.com\\/auth\\/youtubepartner https:\\/\\/www.googleapis.com\\/auth\\/userinfo.email\",\"additionalParameters\":{}}}")


        Log.d("jay", "${authState!!.accessToken}")
    }

    private fun refreshToken(){
        authService!!.performTokenRequest(authState!!.createTokenRefreshRequest(), object : AuthorizationService.TokenResponseCallback{
            override fun onTokenRequestCompleted(response: TokenResponse?, ex: AuthorizationException?) {
                Log.d("jay", "refrash result ${response?.accessToken}")
                Log.d("jay", "refrash result ${response?.refreshToken}")

                authState!!.update(response, ex)
            }
        })
    }

    private fun requestLogout() {
        authState!!.authorizationServiceConfiguration
        // 로그아웃시 인증 상태 초기화
        authState = AuthState()
    }


    private fun requestLogin() {
        val config = AuthorizationServiceConfiguration(
            Uri.parse("https://accounts.google.com/o/oauth2/v2/auth"), /* authorization endpoint */
            Uri.parse("https://www.googleapis.com/oauth2/v4/token") /* token 요청 endpoint */
        )

        val request = AuthorizationRequest.Builder(
            config,
            "815911049800-hhs9l91v4f31in31sr5pt5ei94v0usst.apps.googleusercontent.com", /* Google Cloud clientID */
            ResponseTypeValues.CODE, /* response Type */
            Uri.parse("com.jawon.han.googleloginwithrestapi:/oauth2redirect") /* redirect URI. ex) package:/oauth2redirect */
        )
            .setScope("https://www.googleapis.com/auth/youtubepartner email") /*YouTube Scope*/
            .build()

        val authIntent = authService!!.getAuthorizationRequestIntent(request)

        launcher.launch(authIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data

        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("jay", "success")
            val resp = data?.let { AuthorizationResponse.fromIntent(it) }
            val ex = AuthorizationException.fromIntent(data)

            if (resp != null) {
                // Authorization 완료
                val authCode = resp.authorizationCode
                authState!!.update(resp, ex)
                Log.d("jay", "auth code : ${authCode}")

                // authCode로 Token 교환 작업
                authService!!.performTokenRequest(
                    resp.createTokenExchangeRequest(),
                    object : AuthorizationService.TokenResponseCallback {
                        override fun onTokenRequestCompleted(response: TokenResponse?, ex: AuthorizationException?) {
                            if (response != null) {
                                Log.d("jay", "access token : ${response.accessToken}")
                                Log.d("jay", "refresh token : ${response.refreshToken}")
//                                Log.d("jay", "id token : ${response.idToken}")
                                authState!!.update(response, ex)
                                idToken = response.idToken.toString()

                                Log.d("jay", "auth state : ${authState!!.jsonSerialize()}")
                            } else {
                                Log.d("jay", "token fail")
                            }
                        }
                    }
                )

            } else {
                // Authorization 실패
                Log.e("LoginActivity", "Authorization failed: $ex")
            }
        } else {
            Log.d("jay", "fail")
        }
    }
}


