package org.bitbucket.mjanczykowski.falconicp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class MenuDialogFragment extends DialogFragment {

	public interface MenuDialogListener {
		public void onMenuSettingsClick(DialogFragment dialog);
		public void onMenuExitClick(DialogFragment dialog);
		public void onMenuConnectClick(DialogFragment dialog);
		public void onMenuDisconnectClick(DialogFragment dialog);
		public void onMenuDismiss(DialogFragment dialog);
	}
	
	/** Activity listening for dialog actions */ 
	private MenuDialogListener mListener;
	
	/**
	 * Creates new instance depending on connection status. 
	 * @param connected TRUE = connected, FALSE = disconnected
	 * @return New instance of MenuDialogFragment
	 */
	public static MenuDialogFragment newInstance(boolean connected) {
		MenuDialogFragment f = new MenuDialogFragment();
		
		Bundle args = new Bundle();
		args.putBoolean("connected", connected);
		f.setArguments(args);
		
		return f;
	}
	
	public MenuDialogFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mListener = (MenuDialogListener) activity;
		}
		catch(ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement MenuDialogListener");
		}
	}
	
	@Override 
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final boolean connected = getArguments().getBoolean("connected");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		int resId;
		if (connected) {
			resId = R.array.menu_connected;
		}
		else {
			resId = R.array.menu_disconnected;
		}
		
		builder.setTitle(R.string.app_name)
			.setIcon(R.drawable.ic_launcher)
			.setItems(resId, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.i("menu dialog", Integer.toString(which));
					
					switch(which) {
						case 0:
							if(connected) {
								mListener.onMenuDisconnectClick(MenuDialogFragment.this);
							}
							else {
								mListener.onMenuConnectClick(MenuDialogFragment.this);
							}
							break;
						case 1:
							mListener.onMenuSettingsClick(MenuDialogFragment.this);
							break;
						case 2:
							mListener.onMenuExitClick(MenuDialogFragment.this);
							break;
					}
					// TODO
				}
			});

		return builder.create();
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		mListener.onMenuDismiss(this);
	}

}
