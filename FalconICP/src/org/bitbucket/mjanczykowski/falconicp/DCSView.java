package org.bitbucket.mjanczykowski.falconicp;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class DCSView extends ImageView {
	
	public interface DCSViewListener {
		public void onDCSMove(State state);
	}
	
	/** Activity listening for dialog actions */ 
	private DCSViewListener mListener = null;
	
	/* Indicates current direction of the DCS stick */
	private State state = State.CENTER;
	
	/* Coordinates registered on ACTION_DOWN */
	private float actionDownX;
	private float actionDownY;

	public DCSView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float eventX = event.getX();
		float eventY = event.getY();
		
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				actionDownX = eventX;
				actionDownY = eventY;
				return true;
			case MotionEvent.ACTION_MOVE:
				float dx = eventX - actionDownX;
				float dy = eventY - actionDownY;
				float absDx = Math.abs(dx);
				float absDy = Math.abs(dy);
				
				if(absDx < 40 && absDy < 40) {
					setState(State.CENTER);
					return true;
				}
				if(absDx > absDy) {
					if(dx > 0) {
						setState(State.RIGHT);
					}
					else {
						setState(State.LEFT);
					}
					return true;
				}
				if(dy > 0) {
					setState(State.DOWN);
				}
				else {
					setState(State.UP);
				}
				return true;
			case MotionEvent.ACTION_UP:
				if(state != State.CENTER) {
					Log.i("DCS action", state.toString());

					if(mListener != null) {
						mListener.onDCSMove(state);
					}
					setState(State.CENTER);
				}
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * Sets action listener.
	 * @param l Object with DCSViewListener interface
	 */
	public void setActionListener(DCSViewListener l) {
		mListener = l;
	}
	
	/**
	 * Updates DCS switch state and change image if necessary   
	 * @param s New DCS switch state
	 */
	private void setState(State s) {
		if(state != s)
		{
			state = s;
			int resId;
			switch(s) {
				case LEFT:
					resId = R.drawable.dcs_left;
					break;
				case RIGHT:
					resId = R.drawable.dcs_right;
					break;
				case UP:
					resId = R.drawable.dcs_up;
					break;
				case DOWN:
					resId = R.drawable.dcs_down;
					break;
				default:
					resId = R.drawable.dcs_center;
			}
			setImageResource(resId);
		}
	}
	
	/** States of DCS switch - direction of the stick */
	public enum State {
		CENTER, LEFT, UP, RIGHT, DOWN;
	}
}
