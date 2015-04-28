package com.makefun;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.view.View;

public class RippleSwipeRefreshLayout extends FrameLayout{
	
	private static final String TAG = "RippleSwipeRefreshLayout";
	
	private static final int INVALID_POINTER = -1;
	private static final float DRAG_RATE = 1.0f;
	private static final int TRIGGER_OFFSET = 150;	
	
	// 初始化用到的一些数值
	private int mTouchSlop; // 一个长度，当move大于等于这个长度，我们认为用户在滑动
	// 子view
	private View mTarget; // 子View，一般为ListView
	private RippleCircleView mCircleView;
	// 变量
	private int mTriggerOffset;
	private int mActivePointerId; // 处于活动的手指Id
	private float mInitDownY; // 初始按下的Y，在interceptTouchEvent获取
	private float mInitMotionY; // 开始记录运动的Y，这时候记录的是超过mTouchSlop的Y值
	private boolean mIsBeingDragged = false;// 是否在拉,也用这个字段来判断是否打断touch事件
	private boolean mAnimShowing = false;	//动画是否在执行

	private OnRefreshListener mListener;
	
	public RippleSwipeRefreshLayout(Context context) {
		this(context, null);
	}
	
	public RippleSwipeRefreshLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public RippleSwipeRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		final DisplayMetrics metrics = getResources().getDisplayMetrics();
		mTriggerOffset = (int) (metrics.density*TRIGGER_OFFSET);
		createCircleView();
		initAttr(context,attrs);
	}

	private void initAttr(Context context, AttributeSet attrs) {
		TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.RippleSwipeRefreshLayout, 0, 0);
		if(attr == null){
			return ;
		}
		int circleColor = attr.getColor(R.styleable.RippleSwipeRefreshLayout_rsr_circleColor, mCircleView.getDefaultCircleColor());
		mCircleView.setCircleColor(circleColor);
		int circleIconId = attr.getResourceId(R.styleable.RippleSwipeRefreshLayout_rsr_circleIcon, 0);
		if(circleIconId>0){
			mCircleView.setCircleIcon(circleIconId);
		}
		attr.recycle();
	}

	
	
	public interface OnRefreshListener{
		public void onRefresh();
	}
	
	public void setListener(OnRefreshListener listener){
		this.mListener = listener;
	}

	/********************** 触摸事件 *********************/
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		ensureTarget();
		final int action = MotionEventCompat.getActionMasked(ev);
		if(canChildScollUp()||mAnimShowing){
			return false;
		}
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
			mIsBeingDragged = false;
			final float initDownY = getMotionEventY(ev, mActivePointerId);
			if (initDownY == -1) {
				return false;
			}
			mInitDownY = initDownY;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mActivePointerId == INVALID_POINTER) {
				return false;
			}

			final float y = getMotionEventY(ev, mActivePointerId);
			if (y == -1) {
				return false;
			}
			final float yDiff = y - mInitDownY;
			if (yDiff > mTouchSlop && !mIsBeingDragged) {
				mInitMotionY = mInitDownY + mTouchSlop;
				mIsBeingDragged = true;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mIsBeingDragged = false;
			mActivePointerId = INVALID_POINTER;
			break;
		}
		return mIsBeingDragged;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		ensureTarget();
		final int action = MotionEventCompat.getActionMasked(ev);
		if(canChildScollUp()){
			return false;
		}
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
			mIsBeingDragged = false;
			break;
		case MotionEvent.ACTION_MOVE:
			final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
			if(pointerIndex<0){
				log("onTouch:Got ACTION_MOVE event but have an invalid active pointer id.");
				return false;
			}
			final float y = MotionEventCompat.getY(ev, pointerIndex);
			final float overscrollTop = (y-mInitMotionY)*DRAG_RATE; 
			if(mIsBeingDragged){
				mCircleView.setVisibility(View.VISIBLE);
				mCircleView.bringToFront();
				if(overscrollTop>mTriggerOffset){
					startRadiusAnim();
					mIsBeingDragged = false;
				}else{
					mCircleView.setCircleOffsetTop((int) overscrollTop-mCircleView.getDefaultRadius());
				}
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if(!mAnimShowing){
				System.out.println("go");
				mCircleView.reset(true);
			}else{
				System.out.println("hehe");
			}
			mIsBeingDragged = false;
			mActivePointerId = INVALID_POINTER;
			return false;
		}
		return true;
	}

	private void createCircleView(){
		mCircleView = new RippleCircleView(getContext());
		mCircleView.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
		mCircleView.setVisibility(View.GONE);
		addView(mCircleView);
	}
	
	private void ensureTarget() {
		if (mTarget == null) {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				if (!child.equals(mCircleView)) {
					mTarget = child;
					break;
				}
			}
		}
	}
	
	
	/******************Anim********************/
	private void startRadiusAnim(){
		mCircleView.setShowIcon(false);
		int measuredWidth = getMeasuredWidth();
		int measuredHeight = getMeasuredHeight();
		int height = Math.max(measuredHeight-mCircleView.getCenterY(), mCircleView.getCenterY());
		int width = measuredWidth/2;
		final int targetRadius = (int) Math.sqrt(width*width+height*height);
		ObjectAnimator radiusAnim = ObjectAnimator.ofInt(mCircleView, "circleRadius", mCircleView.getDefaultRadius(),targetRadius);
		radiusAnim.setDuration(400);
		radiusAnim.addListener(new RadiusAnimListener());
		radiusAnim.start();
	}
	
	private void startAlphaAnim() {
		if(mListener!=null){
			mListener.onRefresh();
		}
		ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(mCircleView, "circleAlpha", 1.0f,0);
		alphaAnim.setDuration(400);
		alphaAnim.addListener(new AlphaAnimListener());
		alphaAnim.start();
	}
	
	public void startRefresh(){
		if(mAnimShowing){
			return ;
		}
		ensureTarget();
		mCircleView.setShowIcon(true);
		ObjectAnimator offsetAnim = ObjectAnimator.ofInt(mCircleView, "circleOffsetTop", -mCircleView.getDefaultRadius(),mTriggerOffset);
		offsetAnim.setDuration(300);
		offsetAnim.addListener(new OffsetAnimListener());
		offsetAnim.start();
	}
	
	
	public abstract class BaseAnimListener implements AnimatorListener{
		@Override
		public void onAnimationStart(Animator animation) {
			mAnimShowing = true;
		}
		@Override
		public void onAnimationEnd(Animator animation) {
			mAnimShowing = false;
		}
		@Override
		public void onAnimationCancel(Animator animation) {
			mAnimShowing = false;
		}
		@Override
		public void onAnimationRepeat(Animator animation) {
		}
	}
	
	public class RadiusAnimListener extends BaseAnimListener{
		
		@Override
		public void onAnimationStart(Animator animation) {
			super.onAnimationStart(animation);
			mCircleView.setShowIcon(false);
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			startAlphaAnim();
		}
	}
	
	public class AlphaAnimListener extends BaseAnimListener{
		@Override
		public void onAnimationStart(Animator animation) {
			super.onAnimationStart(animation);
//			mTarget.setVisibility(View.GONE);
		}
		@Override
		public void onAnimationEnd(Animator animation) {
			super.onAnimationEnd(animation);
//			mTarget.setVisibility(View.VISIBLE);
			mCircleView.reset();
		}
	}
	
	public class OffsetAnimListener extends BaseAnimListener{
		
		@Override
		public void onAnimationStart(Animator animation) {
			super.onAnimationStart(animation);
			mCircleView.setVisibility(View.VISIBLE);
			mCircleView.bringToFront();
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			startRadiusAnim();
		}
	}
	
	/******************* 工具方法 ************
	 * 是否还能向上滑动，能滑动就没有到头。
	 * @return	
	 */
	private boolean canChildScollUp() {
		if (android.os.Build.VERSION.SDK_INT < 14) {
			if (mTarget instanceof AbsListView) {
				final AbsListView absListView = (AbsListView) mTarget;
				return absListView.getChildCount() > 0
						&& (absListView.getFirstVisiblePosition() > 0 || absListView
								.getChildAt(0).getTop() < absListView
								.getPaddingTop());
			}else{
				return mTarget.getScaleY()>0;
			}
		} else {
			return ViewCompat.canScrollVertically(mTarget, -1);
		}
	}

	private float getMotionEventY(MotionEvent ev, int activePointerId) {
		final int index = MotionEventCompat.findPointerIndex(ev,
				activePointerId);
		if (index < 0) {
			return -1;
		}
		return MotionEventCompat.getY(ev, index);
	}

	private void log(String msg) {
		Log.e(TAG, msg);
	}
	

	
}
