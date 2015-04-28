package com.example.rippleswiperefreshlayoutsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
	}
	
	public void basic(View v){
		Intent intent = new Intent(this,BasicActivity.class);
		startActivity(intent);
	}
	
	public void custom(View v){
		Intent intent = new Intent(this,CustomActivity.class);
		startActivity(intent);
	}
}
