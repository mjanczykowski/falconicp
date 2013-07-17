package org.bitbucket.mjanczykowski.falconicp;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class FalconICP extends Activity {
	
	private DataEntryDisplay dedView;
	private DataEntryDisplayThread dedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.v("activity state", "onCreate");
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
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
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	Log.v("activity state", "onPause");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.icp, menu);
        return true;
    }
    
    
}
