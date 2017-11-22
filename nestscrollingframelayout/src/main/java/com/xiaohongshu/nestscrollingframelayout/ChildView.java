package com.xiaohongshu.nestscrollingframelayout;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Created by wupengjian on 17/11/20.
 */
public class ChildView extends FrameLayout implements NestedScrollingChild {

    private IPrint mPrint;
    private NestedScrollingChildHelper mChildHelper;
    private float mLastY;
    private int[] mConsume = new int[2], mOffsetInWindow = new int[2];
    private View mDirectChildView;
    private int mChildExpectedHeight, mMeasuredHeight, mTotalOffset;
    private Scroller mScroller;

    public ChildView(@NonNull Context context) {
        this(context, null);
    }

    public ChildView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChildView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context, new AccelerateDecelerateInterpolator());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDirectChildView = getChildAt(0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasuredHeight = mDirectChildView.getMeasuredHeight();
        mDirectChildView.measure(0, 0);
        mChildExpectedHeight = mDirectChildView.getMeasuredHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                getChildHelper().startNestedScroll(SCROLL_AXIS_VERTICAL);
                mLastY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int dy = (int) (event.getRawY() - mLastY);
                mLastY = event.getRawY();
                //1、ViewGroup先消耗一波
                if (getChildHelper().dispatchNestedPreScroll(0, dy, mConsume, mOffsetInWindow)) {
                    dy -= mConsume[1];
                }
                //2、自己消耗一波
                //手指从上往下
                int deltaY = 0;
                if (dy > 0 && mTotalOffset < 0) {
                    deltaY = dy;
                    if (deltaY + mTotalOffset > 0) {
                        deltaY = 0 - mTotalOffset;
                    }
                    mTotalOffset += deltaY;
                    scrollBy(0, -deltaY);
                    print(String.format("child cost %s , total offset is %d", deltaY, mTotalOffset));
                }
                //手指从下往上
                else if (dy < 0 && mTotalOffset > mMeasuredHeight - mChildExpectedHeight) {
                    deltaY = dy;
                    if (deltaY + mTotalOffset < mMeasuredHeight - mChildExpectedHeight) {
                        deltaY = mMeasuredHeight - mChildExpectedHeight - mTotalOffset;
                    }
                    mTotalOffset += deltaY;
                    scrollBy(0, -deltaY);
                    print(String.format("child cost %s , total offset is %d", deltaY, mTotalOffset));
                }
                //3、自己消耗完仍有剩余，交给父View
                if (dy != deltaY) {
                    dispatchNestedScroll(0, deltaY, 0, dy - deltaY, mOffsetInWindow);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getChildHelper().stopNestedScroll();
                break;
        }
        //很重要，否则下面的事件收不到了
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    public void setPrint(IPrint print) {
        mPrint = print;
    }

    private void print(String text) {
        if (mPrint != null && text != null) {
            mPrint.printInfo(text);
        }
    }

    public NestedScrollingChildHelper getChildHelper() {
        if (mChildHelper == null) {
            mChildHelper = new NestedScrollingChildHelper(this);
            mChildHelper.setNestedScrollingEnabled(true);
        }
        return mChildHelper;
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        getChildHelper().setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return getChildHelper().isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return getChildHelper().startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        getChildHelper().stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return getChildHelper().hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return getChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return getChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return getChildHelper().dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return getChildHelper().dispatchNestedPreFling(velocityX, velocityY);
    }
}
