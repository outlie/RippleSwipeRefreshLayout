package com.example.rippleswiperefreshlayoutsample;

import com.makefun.RippleSwipeRefreshLayout;
import com.makefun.RippleSwipeRefreshLayout.OnRefreshListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CustomActivity extends ActionBarActivity implements OnRefreshListener, OnClickListener {
	
	private static final int LOAD_SUCCESS = 1;
	
	private View mEmpty;
	private View mProcess;
	private ListView mList;
	private RippleSwipeRefreshLayout mRefreshLayout;
	
	private Animation mListAnim;
	private LayoutAnimationController mListAnimController;
	
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case LOAD_SUCCESS:
				fillData();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_custom);
		initView();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void initView() {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		mRefreshLayout = (RippleSwipeRefreshLayout) findViewById(R.id.refreshLayout);
		mRefreshLayout.setListener(this);
		mProcess = findViewById(R.id.process);
		mEmpty = findViewById(R.id.empty);
		mEmpty.setOnClickListener(this);
		mList = (ListView) findViewById(R.id.list);
		mList.setEmptyView(mEmpty);
		mListAnim = AnimationUtils.loadAnimation(this, R.anim.my_slide_in);
		mListAnimController = new LayoutAnimationController(mListAnim);
		mListAnimController.setOrder(LayoutAnimationController.ORDER_NORMAL);
		mListAnimController.setDelay(0.2f);
	}
	
	private void fillData() {
		mProcess.setVisibility(View.GONE);
		MyAdapter adapter = new MyAdapter(Cheeses.sCheeseStrings);
		mList.setAdapter(adapter);
		mList.setLayoutAnimation(mListAnimController);
		mList.startAnimation(mListAnim);
	}

	@Override
	public void onRefresh() {
		mProcess.setVisibility(View.VISIBLE);
		mHandler.sendEmptyMessageDelayed(LOAD_SUCCESS, 2000);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.empty:
			mEmpty.setVisibility(View.GONE);
			mRefreshLayout.startRefresh();
			break;
		}
	}
	
	public class MyAdapter extends BaseAdapter {

		private String[] mArray;

		public MyAdapter(String[] array) {
			this.mArray = array;
		}

		@Override
		public int getCount() {
			return mArray.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(CustomActivity.this,
						android.R.layout.simple_list_item_1, null);
			}
			TextView t = (TextView) convertView;
			t.setText(mArray[position]);
			t.setTextColor(0xff1d1d1d);
			return convertView;
		}
	}

}

