package org.bitbucket.mjanczykowski.falconicp;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class DriftWarnSwitch extends ImageView {
	
	public interface DriftWarnListener {
		public void onDriftWarnSwitch(State state);
	}
	
	/** Activity listening for dialog actions */ 
	private DriftWarnListener mListener = null;
	
	/** Indicates position of the switch */
	private State state = State.NORM;
	private State previousState = State.NORM;
	
	/* Coordinates registered on ACTION_DOWN */
	private float actionDownY;

	public DriftWarnSwitch(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float eventY = event.getY();
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				actionDownY = eventY;
				previousState = state;
				return true;
			case MotionEvent.ACTION_MOVE:
				float dy = eventY - actionDownY;
				float absDy = Math.abs(dy);
				if(absDy < 40) {
					setState(previousState);
					return true;
				}
				if(dy > 0) {
					setState(State.valueOf(previousState.getValue() - 1));
				}
				else {
					setState(State.valueOf(previousState.getValue() + 1));
				}
				return true;
			case MotionEvent.ACTION_UP:
				if(state != previousState) {
					Log.i("DriftWarn action", state.toString());
					if(mListener != null) {
						mListener.onDriftWarnSwitch(state);
					}
					// perform action
					if(state == State.WARN_RESET)
						setState(State.NORM);
				}
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * Sets action listener.
	 * @param l Action listener
	 */
	public void setActionListener(DriftWarnListener l) {
		mListener = l;
	}
	
	public void setState(State s) {
		if(state != s) {
			state = s;
			int resId;
			switch(s) {
				case DRIFT_CO:
					resId = R.drawable.driftwarn_up;
					break;
				case NORM:
					resId = R.drawable.driftwarn_center;
					break;
				case WARN_RESET:
				default:
					resId = R.drawable.driftwarn_down;
			}
			setImageResource(resId);
		}
	}

	/** Position of the switch */
	public enum State {
		DRIFT_CO(1), NORM(0), WARN_RESET(-1);
		
		private final int value;
		
		private State(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public static State valueOf(int i) {
			if(i == 0)
				return State.NORM;
			else if(i > 0)
				return State.DRIFT_CO;
			else
				return State.WARN_RESET;
		}
	}
}
