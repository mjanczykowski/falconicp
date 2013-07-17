package org.bitbucket.mjanczykowski.falconicp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class DEDFont {
	/** The table of complete font to cut from */
	private final Bitmap _font;
	
	/** Array of bitmaps representing single signs */
	private final Bitmap[] _charBitmaps = new Bitmap[256];
	
	/** Array of bitmaps representing single signs in inverted colors */ 
	private final Bitmap[] _invertedCharBitmaps = new Bitmap[256];
	
	private int charWidth = 0;
	private int charHeight = 0;
	
	public DEDFont(Context context) {
		Resources res = context.getResources();
		_font = BitmapFactory.decodeResource(res, R.drawable.font);
		charWidth = _font.getWidth() / 16;
		charHeight = _font.getHeight() / 16;
	}
	
	/**
	 * Returns bitmap of specified char
	 * @param b Byte - number of the character
	 * @param inverted - false for normal colors, true for inverted colors
	 * @return bitmap of char
	 */
	public Bitmap getCharImage(byte b, boolean inverted) {
		if(b >= 32) {
			b -= 32;
		}
		
		Bitmap[] charArray = _charBitmaps;
		if(inverted)
			charArray = _invertedCharBitmaps;
		
		if(charArray[b] == null) {
			int leftX = (b % 16) * charWidth;
			int topY = (b / 16) * charHeight;
			if(inverted) {
				topY += _font.getHeight() / 2;
			}
			//cut the image
			Bitmap charImage = Bitmap.createBitmap(_font, leftX, topY, charWidth, charHeight);
			//store it in cache of char images
			charArray[b] = charImage;
		}
		
		return charArray[b];
	}
	
	/**
	 * Returns width of a single character
	 * @return width of a single character
	 */
	public int getCharWidth() {
		return this.charWidth;
	}
	
	/**
	 * Returns height of a single character
	 * @return height of a single character
	 */
	public int getCharHeight() {
		return this.charHeight;
	}
}
