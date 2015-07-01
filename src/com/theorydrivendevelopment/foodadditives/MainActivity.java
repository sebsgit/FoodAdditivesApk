package com.theorydrivendevelopment.foodadditives;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.text.util.Linkify;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity {

	private AdditivesDatabase database;
	private int lastDisplayed;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);
		
		database = new AdditivesDatabase(this);
		
		EditText edit = (EditText)findViewById(R.id.editText1);
		edit.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					if (v.getText() != null)
						MainActivity.this.showAdditive(v.getText().toString());
				}
				return false;
			}
		});
	
		Spinner spin = (Spinner)findViewById(R.id.spinner);
		ArrayList<String> items = database.dangerIds();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
							android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        spin.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String s = arg0.getSelectedItem().toString();
				MainActivity.this.showAdditive(s);
			}
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
        
        spin = (Spinner)findViewById(R.id.spinner_name);
		items = database.dangerNames();
		adapter = new ArrayAdapter<String>(this,
							android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        spin.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String s = arg0.getSelectedItem().toString();
				MainActivity.this.showAdditiveByName(s);
			}
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
        
        TextView mainView = (TextView)findViewById(R.id.textViewDesc);
        mainView.setOnTouchListener(new OnSwipeTouchListener(){
        	public void onSwipeLeft(){
        		MainActivity.this.displayRight();
        	}
        	public void onSwipeRight(){
        		MainActivity.this.displayLeft();
        	}
        });
	}

    void displayLeft(){
		this.showAdditive(this.database.nextLeft(lastDisplayed));
    }
    void displayRight(){
		this.showAdditive(this.database.nextRight(lastDisplayed));
    }
    	
	public void showAdditive(String name){
		if (name == null || name.length()==0)
			return;
		final int number = Integer.parseInt(name);
		this.showAdditive(number);
	}
	
	public void showAdditiveByName(String name){
		if (name==null || name.length()==0)
			return;
		this.showAdditive(this.database.nameToNumber(name));
	}
	
	public void showAdditive(int number){
		TextView nameView = (TextView)findViewById(R.id.textViewName);
		TextView descView = (TextView)findViewById(R.id.textViewDesc);
		TextView grpView = (TextView)findViewById(R.id.textViewGroup);
		grpView.setTextColor(Color.MAGENTA);
		if (database.isValid(number)) {
			if (database.containsDescription(number))
				this.lastDisplayed = number;
			AdditivesDatabase.Additive add = database.getDetails(number);
			descView.setText(add.description);
			nameView.setText(add.name);
			if (database.isDangerous(number)) {
				nameView.setTextColor(Color.RED);
			} else {
				nameView.setTextColor(Color.BLUE);
			}
			grpView.setText(database.getCategoryString(number));
		} else {
			nameView.setText(getString(R.string.bad_number));
			descView.setText(getString(R.string.no_details));
			grpView.setText(getString(R.string.catUnknown));
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		if (item.getItemId() == R.id.action_about){
			StringBuilder msg = new StringBuilder();
			msg.append(getString(R.string.text_about));
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(msg.toString()).setTitle(R.string.action_about);
			AlertDialog dialog = builder.create();
			dialog.show();
			TextView messageView = (TextView)dialog.findViewById(android.R.id.message);
			messageView.setGravity(android.view.Gravity.CENTER);
			Linkify.addLinks(messageView, Linkify.ALL);
			return true;
		}
		return false;
	}

	class OnSwipeTouchListener implements OnTouchListener {

	    private final GestureDetector gestureDetector = new GestureDetector(MainActivity.this,new GestureListener());

	    public boolean onTouch(final View view, final MotionEvent motionEvent) {
	        return gestureDetector.onTouchEvent(motionEvent);
	    }

	    private final class GestureListener extends SimpleOnGestureListener {

	        private static final int SWIPE_THRESHOLD = 100;
	        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

	        @Override
	        public boolean onDown(MotionEvent e) {
	            return true;
	        }

	        @Override
	        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	            boolean result = false;
	            try {
	                float diffY = e2.getY() - e1.getY();
	                float diffX = e2.getX() - e1.getX();
	                if (Math.abs(diffX) > Math.abs(diffY)) {
	                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
	                        if (diffX > 0) {
	                            onSwipeRight();
	                        } else {
	                            onSwipeLeft();
	                        }
	                    }
	                } else {
	                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
	                        if (diffY > 0) {
	                            onSwipeBottom();
	                        } else {
	                            onSwipeTop();
	                        }
	                    }
	                }
	            } catch (Exception exception) {
	                exception.printStackTrace();
	            }
	            return result;
	        }
	    }

	    public void onSwipeRight() {
	    }

	    public void onSwipeLeft() {
	    }

	    public void onSwipeTop() {
	    }

	    public void onSwipeBottom() {
	    }
	}

	
}
