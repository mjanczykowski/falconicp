package org.bitbucket.mjanczykowski.falconicp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class DEDView extends View {
	
	/** Handle to mask image */
	private final Drawable maskImage;
	
	/** Handle to DED font converter */
	private DEDFont font;
	
	/** Buffer image to draw DED chars on before scaling */
	private Bitmap bufferImage;
	private final int STEP_X;
	private final int STEP_Y;
	
	private byte[][] dedLines = new byte[5][25];
	
	private Paint p = new Paint();
	private Paint inv_p = new Paint();

	public DEDView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		maskImage = context.getResources().getDrawable(R.drawable.mask);
		
		font = new DEDFont(context);
		
		Bitmap mask = BitmapFactory.decodeResource(context.getResources(), R.drawable.mask);
		bufferImage = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Bitmap.Config.ARGB_8888);
		
		int charWidth = font.getCharWidth();
		int charHeight = font.getCharHeight();
		
		STEP_X = charWidth + (mask.getWidth() - 25*charWidth)/24;
		STEP_Y = charHeight + (mask.getHeight() - 5*charHeight)/4;
		
		p.setColor(Color.BLACK);
		p.setFilterBitmap(true);
		p.setAntiAlias(true);
		
		inv_p.setColor(Color.parseColor(context.getResources().getString(R.string.font_color)));
		inv_p.setFilterBitmap(true);
		inv_p.setAntiAlias(true);
	}

	public void setDedLines(byte[][] dedLines)
	{
		this.dedLines = dedLines;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		Log.i("DEDView", "onDraw");
		
		if(dedLines == null)
		{
			throw new IllegalStateException();
		}
		
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
		
		float scale = (float)canvas.getWidth() / (float)bufferImage.getWidth();
		int topY = 0;
		
		canvas.scale(scale, scale);

		synchronized(dedLines) {
			for(int i = 0; i < 5; i++) {
				int leftX = 0;
				for(int j = 0; j < 25; j++) {
					if(dedLines[i][j] != 0) {
						if(dedLines[i][j] < 0) {
							canvas.drawRect(leftX, topY, leftX+STEP_X, topY+STEP_Y, inv_p);
						}
						canvas.drawBitmap(font.getCharImage(dedLines[i][j]), leftX, topY, p);
					}
					leftX += STEP_X;
				}
				topY += STEP_Y;
			}
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
	

}
