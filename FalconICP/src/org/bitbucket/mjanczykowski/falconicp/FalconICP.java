package org.bitbucket.mjanczykowski.falconicp;

import java.lang.ref.WeakReference;

import org.bitbucket.mjanczykowski.falconicp.DCSView.DCSViewListener;
import org.bitbucket.mjanczykowski.falconicp.DriftWarnSwitch.DriftWarnListener;
import org.bitbucket.mjanczykowski.falconicp.MenuDialogFragment.MenuDialogListener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class FalconICP extends FragmentActivity implements MenuDialogListener, DCSViewListener, DriftWarnListener {
	
	private DEDView dedView;
	private TcpClientThread tcpThread;
	private MenuDialogFragment menuFragment = null;
	private AlertDialog exitDialog = null;
	private boolean connected = false;
	private boolean resumed = false;
	
	private final IncomingHandler handler = new IncomingHandler(this);
	private byte[][] dedLines = new byte[5][26]; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.v("activity state", "onCreate");
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.activity_icp);

        dedView = (DEDView) findViewById(R.id.ded);
        dedView.setDedLines(dedLines);
        
        DCSView dcs = (DCSView) findViewById(R.id.DCS);
        if(dcs != null) {
        	dcs.setActionListener(this);
        }
        
        DriftWarnSwitch dws = (DriftWarnSwitch) findViewById(R.id.DriftWarn);
        if(dws != null) {
        	dws.setActionListener(this);
        }
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	Log.v("activity state", "onStart");
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
    	
    	if(sp.getBoolean(Settings.KEY_AUTOCONNECT, false) == true) {
    		connect();
    	}
    	else if(exitDialog == null || exitDialog.isShowing() == false){
    		showMenuDialog();
    	}
    	
    	this.resumed = true;
    }
    
    @Override
    protected void onPause() { 	
    	this.resumed = false;
    	
    	super.onPause();
    	
    	if(menuFragment != null) {
    		menuFragment.dismissAllowingStateLoss();
    		menuFragment = null;
    	}
    	    	
    	if(exitDialog != null)
    	{
    		exitDialog.dismiss();
    		exitDialog = null;
    	}
		
    	
    	disconnect();
    	
    	Log.v("activity state", "onPause");
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	Log.v("activity state", "onDestroy");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	showMenuDialog();
        return true;
    }
    
    @Override
    public void onBackPressed() {
    	Log.i("FalconICP", "onBackPressed");
    	if(connected) {
    		showMenuDialog();
    	}
    	else {
    		showExitDialog();
    	}
    }

    /**
     * Action performed on ICP button click. Sends callback to server.
     * @param view Button
     */
    public void buttonClicked(View view) {
    	if(connected) {
    		String callback = (String)view.getTag();
    		if(callback != null) {
    			Log.v("buttonClicked", callback);
    			tcpThread.sendCallback(callback);
    		}
    	}
    }
    
    /**
     * Generates and shows menu dialog.
     */
    public void showMenuDialog() {
    	Log.d("showMenuDialog", "executed");

    	//if(menuFragment != null && menuFragment.isResumed()) {
    	if(menuFragment != null) {
    		menuFragment.dismissAllowingStateLoss();
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
		Log.i("FalconICP", "onMenuExitClick");
		showExitDialog();
	}

	@Override
	public void onMenuConnectClick(DialogFragment dialog) {
		connect();
	}

	@Override
	public void onMenuDisconnectClick(DialogFragment dialog) {
		disconnect();		
	}

	@Override
	public void onMenuDismiss(DialogFragment dialog) {
		menuFragment = null;
	}
	
	private void showExitDialog() {
		Log.i("exit dialog", "show");
		exitDialog = new AlertDialog.Builder(this)
			.setMessage(R.string.dialog_exit_message)
			.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(FalconICP.this.menuFragment == null) {
						FalconICP.this.showMenuDialog();
					}
					FalconICP.this.exitDialog = null;
				}
			})
			.setPositiveButton(R.string.dialog_exit, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					FalconICP.this.exitDialog = null;
					FalconICP.this.finish();
				}
			})
			.create();
		
		exitDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if(FalconICP.this.menuFragment == null) {
					FalconICP.this.showMenuDialog();
				}
				FalconICP.this.exitDialog = null;
			}
			
		});
		
		exitDialog.show();
	}
	
	/**
	 * Runs client thread and tries to connect to the server.
	 */
	private void connect()
	{
		if(connected) {
			disconnect();
		}
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String ip = sp.getString(Settings.KEY_SERVER_IP, "0.0.0.0");
		Log.v("FalconICP", ip);
		if(ip.equals("0.0.0.0")) {
			// Load settings if no IP set
			startActivity(new Intent(this, Settings.class));
			return;
		}
		tcpThread = new TcpClientThread(ip, Integer.parseInt(sp.getString(Settings.KEY_SERVER_PORT, "30456")), Integer.parseInt(sp.getString(Settings.KEY_TIMEOUT, "2000")), handler);
		tcpThread.setDedLines(dedLines);
		Log.d("main thread", "starting tcp thread");
		tcpThread.start();
		Log.d("main thread", "tcp thread started");
		connected = true;
		
		if(menuFragment != null) {
			menuFragment.dismiss();
		}
	}
	
	/**
	 * Stops client thread if one exists.
	 */
	private void disconnect()
	{
		connected = false;
		if(tcpThread != null)
		{
			tcpThread.closeThread();
			try {
				tcpThread.join(1000);
			} catch (InterruptedException e) {
				Log.d("FalconICP", "InterruptedException: " + e.getMessage());
			}
	    	tcpThread = null;
		}
	}
	
	/**
	 * Show toast with information about connection state.
	 * @param text Information to show
	 */
	private void showToast(String text) {
		int duration = Toast.LENGTH_LONG;
		
		Toast.makeText(getApplicationContext(), text, duration).show();
	}
	
	/**
	 * Static class for incoming transmission handler.
	 * Using WeakReferences to avoid memory leaks.
	 */
	static class IncomingHandler extends Handler {
		private final WeakReference<FalconICP> ref;
		
		public IncomingHandler(FalconICP ref) {
			this.ref = new WeakReference<FalconICP>(ref);
		}
		
		@Override
		public void handleMessage(Message msg) {
			FalconICP icp = ref.get();
			
			String txt = (String)msg.obj;
			if(txt == null) {
				txt = "";
			}
			
			switch(msg.what) {
				case DATA:
					Log.v("message", "data");
					icp.dedView.invalidate();
					break;
				case CONNECTED:
					Log.v("message", "connected");
					icp.connected = true;
					icp.showToast("Connected to " + txt);
					break;
				case DISCONNECTED:
					Log.v("message", "disconnected");
					icp.connected = false;
					icp.tcpThread = null;
					icp.showToast("Disconnected");
					if(icp.resumed) {
						icp.showMenuDialog();
					}
					break;
				case CONNECTION_ERROR:
					Log.v("message", "connection_error");
					icp.connected = false;
					icp.tcpThread = null;
					icp.showToast("Connection error: " + txt);
					icp.showMenuDialog();
					break;
			}
		}
		
		public static final int CONNECTED = 1;
		public static final int DISCONNECTED = 2;
		public static final int CONNECTION_ERROR = 3;
		public static final int DATA = 4;
	}

	@Override
	public void onDCSMove(DCSView.State state) {
		if(!connected) {
			return;
		}
		String callback;
		switch(state) {
			case LEFT:
				callback = "SimICPResetDED";
				break;
			case RIGHT:
				callback = "SimICPDEDSEQ";
				break;
			case UP:
				callback = "SimICPDEDUP";
				break;
			case DOWN:
				callback = "SimICPDEDDOWN";
				break;
			default:
				callback = "";
		}
		tcpThread.sendCallback(callback);
	}

	@Override
	public void onDriftWarnSwitch(DriftWarnSwitch.State state) {
		if(!connected) {
			return;
		}
		String callback;
		switch(state) {
			case DRIFT_CO:
				callback = "SimDriftCOOn";
				break;
			case NORM:
				callback = "SimDriftCOOff";
				break;
			case WARN_RESET:
				callback = "SimWarnReset";
				break;
			default:
				callback = "";
		}
		tcpThread.sendCallback(callback);
	}
}
