package com.bluetoothftpanalysis;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseUser;

import android.app.Application;
import android.widget.Toast;
 
public class ParseApplication extends Application {
 
    @Override
    public void onCreate() {
        super.onCreate();
 
        // Parse Initialization
        Parse.initialize(this, "n8ERkMvZcya8JoWsBo6em88rsyTz3c5WgtonV0A4", "f0a1oTwu1xvUmm8Ii4qqZnRoqQ9B7b8av90Jszbc");
        //Parse.enableLocalDatastore(this);
        try {
			ParseUser.logIn("nandan", "asdf");
			Toast.makeText(this, "User logged in", Toast.LENGTH_LONG).show();
		} catch (ParseException e) {
			Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
		}
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);

        ParseACL.setDefaultACL(defaultACL, true);
    }
 
}