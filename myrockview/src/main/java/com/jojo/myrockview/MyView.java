package com.jojo.myrockview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MyView extends View {
    private Paint rockPaint;
    private Point mRockerPosition, mCenterPoint;
    private int width, height;
    private Paint mBitPaint;
    private Bitmap upBitmap, downBitmap, forceUpBitmap, leftBitmap, rightBitmap;
    private Resources resources;
    private int direction;
    private float rockRadius;
    private Callback callback;

    public MyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // 获取自定义属性
        initAttribute(context, attrs);
        resources = getResources();
        initBitmap(direction);
        initPaint();
    }

    private void initAttribute(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyView);
        direction = typedArray.getInteger(R.styleable.MyView_direction, 0);
        rockRadius = typedArray.getDimension(R.styleable.MyView_rockRadius, DensityUtils.dp2px(context, 20));
    }

    private void initPaint() {
        mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitPaint.setFilterBitmap(true);
        mBitPaint.setDither(true);
        rockPaint = new Paint();
        rockPaint.setColor(Color.WHITE);
        mRockerPosition = new Point(-100000, -100000);//防抖
        mCenterPoint = new Point();
    }

    private void initBitmap(int direction) {
        if (direction == 0) {
            upBitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.ic_keyboard_arrow_up_white_24dp))
                    .getBitmap();
            downBitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.ic_keyboard_arrow_down_white_24dp))
                    .getBitmap();
            forceUpBitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.ic_keyboard_capslock_white_24dp))
                    .getBitmap();
        } else {
            leftBitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.ic_keyboard_arrow_left_white_24dp))
                    .getBitmap();
            rightBitmap = ((BitmapDrawable) resources.getDrawable(R.drawable.ic_keyboard_arrow_right_white_24dp))
                    .getBitmap();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getWidth();
        height = getHeight();
        if (direction == 0) {
            mCenterPoint.set(width / 2, height / 2 + height / 8);
            if (mRockerPosition.x == -100000) {
                mRockerPosition.y = mCenterPoint.y;
            }
            canvas.drawCircle(mCenterPoint.x, mRockerPosition.y, rockRadius, rockPaint);
            canvas.drawBitmap(upBitmap, null, new Rect(0, height / 4, width, height / 2), mBitPaint);
            canvas.drawBitmap(downBitmap, null, new Rect(0, 3 * height / 4, width, height), mBitPaint);
            canvas.drawBitmap(forceUpBitmap, null, new Rect(0, 0, width, height / 4), mBitPaint);
        } else {
            mCenterPoint.set(width / 2, height / 2);
            if (mRockerPosition.y == -100000) {
                mRockerPosition.x = mCenterPoint.x;
            }
            canvas.drawCircle(mRockerPosition.x, mCenterPoint.y, rockRadius, rockPaint);
            canvas.drawBitmap(leftBitmap, null, new Rect(0, 0, width / 4, height), mBitPaint);
            canvas.drawBitmap(rightBitmap, null, new Rect(3 * width / 4, 0, width, height), mBitPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                movePosition(moveX, moveY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (direction == 0) {
                    moveRocker(-100000, 0);
                } else {
                    moveRocker(0, -100000);
                }
                if (callback != null) {
                    callback.call(Forward.STOP);
                }
                break;
        }
        return true;
    }

    private void moveRocker(float x, float y) {
        mRockerPosition.set((int) x, (int) y);
        invalidate();
    }

    private void movePosition(float tx, float ty) {
        if (direction == 0) {
            if (ty < width / 3) {
                moveRocker(tx, rockRadius);
                if (callback != null) {
                    callback.call(Forward.FORCE_UP);
                }
            } else if (ty > height - rockRadius) {
                moveRocker(tx, height - rockRadius);
                if (callback != null) {
                    callback.call(Forward.DOWN);
                }
            } else {
                if ((ty - mCenterPoint.y - rockRadius) > 0) {
                    if (callback != null) {
                        callback.call(Forward.DOWN);
                    }
                } else if ((ty - mCenterPoint.y > -rockRadius && ty - mCenterPoint.y <= 0)
                        || (ty - mCenterPoint.y < rockRadius && ty - mCenterPoint.y >= 0)) {
                    if (callback != null) {
                        callback.call(Forward.STOP);
                    }
                } else {
                    if ((mCenterPoint.y - ty) > (height / 4 + height / 8)) {
                        if (callback != null) {
                            callback.call(Forward.FORCE_UP);
                        }
                    } else {
                        if (callback != null) {
                            callback.call(Forward.UP);
                        }
                    }
                }
                moveRocker(tx, ty);
            }
        } else {
            if (tx < rockRadius) {
                moveRocker(rockRadius, ty);
                if (callback != null) {
                    callback.call(Forward.LEFT);
                }
            } else if (tx > width - rockRadius) {
                moveRocker(width - rockRadius, ty);
                callback.call(Forward.RIGHT);
            } else {
                if (tx < (width / 2 - rockRadius)) {
                    callback.call(Forward.LEFT);
                } else if ((tx > width / 2 + rockRadius)) {
                    callback.call(Forward.RIGHT);
                } else {
                    callback.call(Forward.STOP);
                }
                moveRocker(tx, ty);
            }
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public enum Forward {
        UP, DOWN, FORCE_UP, STOP, LEFT, RIGHT
    }

    public interface Callback {
        void call(Forward forward);
    }
}
