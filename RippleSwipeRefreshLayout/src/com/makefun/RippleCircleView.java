package com.makefun;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.widget.ImageView;

public class RippleCircleView extends ImageView {
	private static final int MAX_ALPHA = 255;
	private static final int CIRCLE_RADIUS = 25;
	private static final int DEFAULT_CIRCLE_COLOR = 0xff3f51b5;

	private Rect mImageRect = new Rect();
	private PorterDuffXfermode DEFAULT_XFERMODE = new PorterDuffXfermode(
			PorterDuff.Mode.CLEAR);
	private Matrix mMatrix = new Matrix();
	
	private int mDefaultRadius;
	private int mCenterY = 0;
	private int mRadius = 0;
	private int mAlpha = 255;
	private int mTargetAnimLength;
	private boolean mShowIcon = true;
	private Paint mPaint;
	private Bitmap mImageBitmap;

	public RippleCircleView(Context context) {
		super(context);
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		mDefaultRadius = (int) (metrics.density * CIRCLE_RADIUS);
		mCenterY = -mDefaultRadius;
		mRadius = mDefaultRadius;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(DEFAULT_CIRCLE_COLOR);
		mTargetAnimLength = (int) (metrics.density * (CIRCLE_RADIUS * 5));
		setCircleIcon(R.drawable.ic_refresh);
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int measuredWidth = getMeasuredWidth();
		mPaint.setAlpha(mAlpha);
		int y = mCenterY;
		canvas.drawCircle(measuredWidth / 2, y, mRadius, mPaint);
		if (mShowIcon && mImageBitmap != null) {
			int startShowImagePoint = mDefaultRadius * 1;
			int diff = mCenterY - startShowImagePoint;
			if (diff > 0) {
				int total = mTargetAnimLength - startShowImagePoint;
				float percent = (float) diff / total;
				if (percent > 0) {
					int size = mDefaultRadius - 15;
					mImageRect.set(measuredWidth / 2 - size, y - size,
							measuredWidth / 2 + size, y + size);
					int save = canvas.saveLayer(measuredWidth / 2 - size, y
							- size, measuredWidth / 2 + size, y + size, null,
							Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
									| Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
									| Canvas.FULL_COLOR_LAYER_SAVE_FLAG
									| Canvas.CLIP_TO_LAYER_SAVE_FLAG);
					int current = (int) (y - size + size * 2 * percent);
					canvas.drawBitmap(mImageBitmap, null, mImageRect, mPaint);
					canvas.drawBitmap(mImageBitmap, mMatrix, mPaint);
					mPaint.setXfermode(DEFAULT_XFERMODE);
					canvas.drawRect(getMeasuredWidth() / 2 - size, current,
							getMeasuredWidth() / 2 + size, y + size, mPaint);
					canvas.restoreToCount(save);
					mPaint.setXfermode(null);
				}
			}
		}
	}

	public void setCircleOffsetTop(int offset) {
		mCenterY = offset;
		invalidate();
	}

	public void setCircleColor(int color) {
		mPaint.setColor(color);
		invalidate();
	}

	public void setCircleRadius(int radius) {
		mRadius = radius;
		invalidate();
	}

	public void setCircleAlpha(float alpha) {
		mAlpha = (int) (alpha * MAX_ALPHA);
		invalidate();
	}

	public void setCircleIcon(int resId) {
		setCircleIcon(BitmapFactory.decodeResource(getResources(), resId));
	}

	public void setCircleIcon(Bitmap bitmap) {
		mImageBitmap = bitmap;
		invalidate();
	}

	public int getCenterY() {
		return mCenterY;
	}

	public int getDefaultRadius() {
		return mDefaultRadius;
	}
	
	public int getDefaultCircleColor(){
		return DEFAULT_CIRCLE_COLOR;
	}

	public void setShowIcon(boolean show) {
		this.mShowIcon = show;
		invalidate();
	}

	public void reset() {
		reset(false);
	}

	public void reset(boolean anim) {
		mAlpha = MAX_ALPHA;
		mRadius = mDefaultRadius;
		setShowIcon(true);
		if (anim) {
			ObjectAnimator animator = ObjectAnimator.ofInt(this, "circleOffsetTop",
					mCenterY, -mDefaultRadius);
			animator.setDuration(200);
			animator.start();
		} else {
			mCenterY = -mDefaultRadius;
		}
		invalidate();
	}

	public float fitCenter(Matrix matrix, Bitmap bitmap, int reqWidth,
            int reqHeight) {
    	//小的
        float scale;
        float dx = 0;
        float dy = 0;
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        float widthScale = 1f;
        float heightScale = 1f;
        //为了让小图居中显示，但加上，旋转效果不好
        if(bitmapWidth>=reqWidth||bitmapHeight>=reqHeight){ 
	        widthScale = (float) reqWidth / bitmapWidth;
	        heightScale = (float) reqHeight / bitmapHeight;
        }
    	scale = Math.min(widthScale, heightScale);
        if(widthScale ==1f && heightScale == 1f){
        	dx = (reqWidth - bitmapWidth * scale) * 0.5f;
        	dy = (reqHeight - bitmapHeight * scale) * 0.5f;
        }else{
	        if(widthScale>heightScale){
	        	dx = (reqWidth - bitmapWidth * scale) * 0.5f;
	        }else{
	        	dy = (reqHeight - bitmapHeight * scale) * 0.5f;
	        }
        }
        matrix.setScale(scale, scale);
        matrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
    	//充满width
        return scale;
    }
}
