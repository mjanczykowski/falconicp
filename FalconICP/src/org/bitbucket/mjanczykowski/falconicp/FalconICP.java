package org.bitbucket.mjanczykowski.falconicp;

import org.bitbucket.mjanczykowski.falconicp.MenuDialogFragment.MenuDialogListener;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class FalconICP extends FragmentActivity implements MenuDialogListener {
	
	private DataEntryDisplay dedView;
	private DataEntryDisplayThread dedThread;
	private MenuDialogFragment menuFragment = null;
	private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.v("activity state", "onCreate");
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.activity_icp);

        dedView = (DataEntryDisplay) findViewById(R.id.ded);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	Log.v("activity state", "onStart");
    	
    	dedThread = (DataEntryDisplayThread) dedView.getThread();    	
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	Log.v("activity state", "onResume");
    	
    	// Read settings
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    	if(sp.getBoolean(Settings.KEY_FULLSCREEN, true) == true) {
    		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	}
    	else {
    		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	}
    	
    	if(sp.getBoolean(Settings.KEY_BACKLIGHT, true) == true) {
    		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    	}
    	else {
    		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    	}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	Log.v("activity state", "onPause");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.icp, menu);
    	showMenuDialog();
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection
    	switch (item.getItemId()) {
    		case R.id.action_settings:
    			this.startActivity(new Intent(this, Settings.class));
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    public void buttonClicked(View view) {
    	showMenuDialog();
    }
    
    public void showMenuDialog() {
    	Log.d("showMenuDialog", "executed");
    	
    	if(menuFragment != null) {
    		menuFragment.dismiss();
    		connected = !connected;
    	}
    	menuFragment = MenuDialogFragment.newInstance(connected);
        menuFragment.show(getSupportFragmentManager(), "MenuDialogFragment");
    }

	@Override
	public void onMenuSettingsClick(DialogFragment dialog) {
		startActivity(new Intent(this, Settings.class));
	}

	@Override
	public void onMenuExitClick(DialogFragment dialog) {
		finish();
	}

	@Override
	public void onMenuConnectClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMenuDisconnectClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMenuDismiss(DialogFragment dialog) {
		menuFragment = null;
	}
}
