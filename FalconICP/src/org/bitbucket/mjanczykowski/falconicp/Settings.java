package org.bitbucket.mjanczykowski.falconicp;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity {

	public Settings() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
	}

}
