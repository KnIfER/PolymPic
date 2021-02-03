package com.thegrizzlylabs.sardineandroid.impl;

import com.knziha.polymer.Utils.CMN;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Created by guillaume on 20/11/2017.
 */
class BasicAuthenticator implements Authenticator {

    private String userName;
    private String password;

    public BasicAuthenticator(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    @Override
    public Request authenticate(Route route, Response response) {
        if (response.request().header("Authorization") != null) {
			//CMN.Log("SSX::already authenticated.");
            return null; // Give up, we've already attempted to authenticate.
        }
		//CMN.Log("SSX::Authenticating for response: " + response);
		CMN.Log("SSX::Challenges: " + response.challenges());
        String credential = Credentials.basic(userName, password);
        return response.request().newBuilder()
                .header("Authorization", credential)
                .build();
    }
}
