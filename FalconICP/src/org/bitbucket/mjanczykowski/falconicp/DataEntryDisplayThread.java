package org.bitbucket.mjanczykowski.falconicp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;

public class DataEntryDisplayThread extends Thread {
	
	/** Handle to the surface holder */
	private SurfaceHolder mSurfaceHolder;
	
	/** Indicates whether the surface has been created and is ready to draw */
	private boolean mRun = false;
	
	/** Indicates whether the connection is currently established */
	private boolean connected = false;
	
	/** Handle to DED font converter */
	private DEDFont font;
	
	/** Buffer image to draw DED chars on before scaling */
	private Bitmap bufferImage;
	private final int STEP_X;
	private final int STEP_Y;
	
	private byte[][] dedLines = new byte[5][25]; 
	private byte[][] dedLinesInverted = new byte[5][25];

	public DataEntryDisplayThread(SurfaceHolder holder, Context context) {
		mSurfaceHolder = holder;
		
		font = new DEDFont(context);
		
		Bitmap mask = BitmapFactory.decodeResource(context.getResources(), R.drawable.mask);
		bufferImage = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);
		
		int charWidth = font.getCharWidth();
		int charHeight = font.getCharHeight();
		
		STEP_X = charWidth + (mask.getWidth() - 25*charWidth)/24;
		STEP_Y = charHeight + (mask.getHeight() - 5*charHeight)/4;
	}
	
	@Override
	public void run() {
		Log.i("DEDThread state", "Thread started");
		while(mRun) {
			Canvas c = null;
			try {
				c = mSurfaceHolder.lockCanvas(null);
				synchronized(mSurfaceHolder) {
					//TO-DO - update DED lines from tcp connection
					dedLines[0][10] = 67;
					dedLines[0][11] = 68;
					dedLines[0][12] = 69;
					dedLinesInverted[0][9] = 2;
					dedLinesInverted[0][13] = 2;
					dedLines[2][10] = 19;
					dedLines[2][11] = 21;
					dedLines[2][12] = 10;
					dedLines[2][13] = 16;
					dedLines[2][14] = 19;
					dedLines[2][15] = 7;
					dedLinesInverted[0][0] = 2;
					dedLinesInverted[4][24] = 2;
					//

					doDraw(c);
				}
			}
			finally {
				if(c != null) {
					mSurfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
		Log.i("DEDThread state", "Closing thread");
	}
	
	/**
	 * Closes connection and pauses drawing.
	 */
	public void pause() {
		//TO-DO
	}
	
	/**
	 * Resumes drawing. If previously connected -> reconnect.
	 */
	public void unpause() {
		//TO-DO
	}

	/**
	 * Sets new surface size.
	 * @param width surface width
	 * @param height surface height
	 */
	public void setSurfaceSize(int width, int height) {
		//TO-DO
	}
	
	/**
     * Used to signal the thread whether it should be running or not.
     * Passing true allows the thread to run; passing false will shut it
     * down if it's already running. Calling start() after this was most
     * recently called with false will result in an immediate shutdown.
     * 
     * @param b true to run, false to shut down
     */
	public void setRunning(boolean b) {
		mRun = b;
	}
	
	/**
	 * Draws DED lines on given canvas
	 * @param canvas Canvas to draw on
	 */
	private void doDraw(Canvas canvas) {
		Paint p = new Paint();
		p.setColor(Color.BLACK);
		p.setFilterBitmap(true);
		p.setAntiAlias(true);
		canvas.drawRect(new Rect(0,0, canvas.getWidth(), canvas.getHeight()), p);
		
		float scale = (float)canvas.getWidth() / (float)bufferImage.getWidth();
		int topY = 0;
		
		canvas.scale(scale, scale);

		for(int i = 0; i < 5; i++) {
			int leftX = 0;
			for(int j = 0; j < 25; j++) {
				if(dedLines[i][j] != 0) {
					canvas.drawBitmap(font.getCharImage(dedLines[i][j], false), leftX, topY, p);
				}
				else if(dedLinesInverted[i][j] != 0) {
					canvas.drawBitmap(font.getCharImage(dedLinesInverted[i][j], true), leftX, topY, p);
				}
				leftX += STEP_X;
			}
			topY += STEP_Y;
		}
	}
}
