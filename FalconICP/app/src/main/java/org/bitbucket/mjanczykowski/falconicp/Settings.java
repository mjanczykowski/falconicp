package org.bitbucket.mjanczykowski.falconicp;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.WindowManager;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String KEY_SERVER_IP = "pref_key_server_ip";
	public static final String KEY_SERVER_PORT = "pref_key_server_port";
	public static final String KEY_TIMEOUT = "pref_key_timeout";
	public static final String KEY_AUTOCONNECT = "pref_key_autoconnect";
	public static final String KEY_BACKLIGHT = "pref_key_backlight";
	public static final String KEY_FULLSCREEN = "pref_key_fullscreen";
	public static final String KEY_LAND_DED_ONLY = "pref_key_land_ded_only";
	private EditTextPreference mServerIPPreference;
	private EditTextPreference mServerPortPreference;
	private EditTextPreference mTimeoutPreference;

	public Settings() {
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load XML preferences file
		addPreferencesFromResource(R.xml.preferences);
		// Get a reference to the preferences
		mServerIPPreference = (EditTextPreference) this.getPreferenceScreen()
				.findPreference(KEY_SERVER_IP);
		mServerPortPreference = (EditTextPreference) this.getPreferenceScreen()
				.findPreference(KEY_SERVER_PORT);
		mTimeoutPreference = (EditTextPreference) this.getPreferenceScreen()
				.findPreference(KEY_TIMEOUT);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// Setup initial values
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		
		if(sharedPreferences.getBoolean(Settings.KEY_FULLSCREEN, true) == true) {
    		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	}
    	else {
    		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	}
		
		mServerIPPreference.setSummary(sharedPreferences.getString(KEY_SERVER_IP, null));
		mServerPortPreference.setSummary(sharedPreferences.getString(KEY_SERVER_PORT, null));
		mTimeoutPreference.setSummary(sharedPreferences.getString(KEY_TIMEOUT, null) + " ms");
		this.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		this.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(KEY_SERVER_IP)) {
			mServerIPPreference.setSummary(sharedPreferences.getString(key, null));
		}
		else if(key.equals(KEY_SERVER_PORT)) {
			mServerPortPreference.setSummary(sharedPreferences.getString(key, null));
		}
		else if(key.equals(KEY_TIMEOUT)) {
			mTimeoutPreference.setSummary(sharedPreferences.getString(key, null));
		}
	}

}
