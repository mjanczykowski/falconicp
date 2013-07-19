package org.bitbucket.mjanczykowski.falconicp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DataEntryDisplay extends SurfaceView implements SurfaceHolder.Callback {
	
	/** The thread that actually draws the DED */
	private DataEntryDisplayThread thread = null;
	
	private SurfaceHolder holder;
	
	private Context dedContext;
	
	/** Handle to mask image */
	private final Drawable maskImage;

	public DataEntryDisplay(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		maskImage = context.getResources().getDrawable(R.drawable.mask);
				
		// Register to SurfaceHolder
		holder = this.getHolder();
		holder.addCallback(this);
		
		dedContext = context;

		this.setFocusable(true); //make sure we get events
	}

	/**
	 * Returns the drawing thread coresponding to this DataEntryDisplay.
	 * @return the drawing thread
	 */
	public DataEntryDisplayThread getThread() {
		if(thread == null) {
			// Create the thread - it's started in surfaceCreated()!
			thread = new DataEntryDisplayThread(holder, dedContext);
		}
		return thread;
	}
	
	/**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (!hasWindowFocus && thread != null) {
			thread.pause();
		}
	}
	
	/**
	 * Calculates measures of the canvas based on mask image dimensions ratio.
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = Math.round(width * maskImage.getIntrinsicHeight() / maskImage.getIntrinsicWidth());
		this.setMeasuredDimension(width, height);
	}
	
	/* Callback invoked when the surface dimensions change. */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d("surface state", "surfaceChanged");
		thread.setSurfaceSize(width, height);	
	}

	/*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
		Log.d("surface state", "surfaceCreated");
        thread.setRunning(true);
        thread.start();		
	}

	/*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
		Log.d("surface state", "surfaceDestroyed");
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            	Log.w("InterruptedException", e.toString());
            }
        }
        thread = null;
	}
}
