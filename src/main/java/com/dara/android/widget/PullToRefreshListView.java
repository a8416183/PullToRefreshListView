
package com.dara.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Scroller;

import com.dara.R;

public class PullToRefreshListView extends ListView {

    private static final int MOVING_POINTER_DISTANCE = 20;

    private static final int SCROLLING_DURATION = 300;

    // Container use the solve the issue of showing space after hiding the
    // loading view
    private LinearLayout mHeaderContainer;

    private LinearLayout mFooterContainer;

    private View mHeaderView;

    private View mFooterView;

    private State mState = State.NONE;

    private final Scroller mScroller; // Control the scroll back of the list

    private int mLastY; // Remember the action down's coordinate

    private boolean mScrolling = false;

    public Runnable mMovement = new Runnable() {

        @Override
        public void run() {
            if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
                switch (mState) {
                    case PULL_DOWN:
                        mHeaderView.setPadding(mHeaderView.getPaddingLeft(), mScroller.getCurrY(),
                                mHeaderView.getPaddingRight(), mHeaderView.getPaddingBottom());
                        break;
                    case PULL_UP:
                        mFooterView.setPadding(mFooterView.getPaddingLeft(),
                                mFooterView.getPaddingTop(), mFooterView.getPaddingRight(),
                                mScroller.getCurrY());
                        break;
                }
            } else {
                resetScroller();
            }
        }
    };

    public PullToRefreshListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mScroller = new Scroller(context, new LinearInterpolator());
        initRefreshView();
    }

    private void initRefreshView() {
        initRefreshViewContainer();

        final LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        mHeaderView = inflater.inflate(R.layout.view_list_loading, mHeaderContainer, false);
        mFooterView = inflater.inflate(R.layout.view_list_loading, mFooterContainer, false);

        mHeaderContainer.addView(mHeaderView);
        mFooterContainer.addView(mFooterView);

        mHeaderView.setVisibility(View.GONE);
        mFooterView.setVisibility(View.GONE);
    }

    private void initRefreshViewContainer() {
        final Context context = getContext();
        final AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                AbsListView.LayoutParams.FILL_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);

        mHeaderContainer = new LinearLayout(context);
        mHeaderContainer.setLayoutParams(params);

        mFooterContainer = new LinearLayout(context);
        mFooterContainer.setLayoutParams(params);
    }

    private void resetScroller() {
        mScroller.abortAnimation();
        mHeaderView.setPadding(mHeaderView.getPaddingLeft(), 0, mHeaderView.getPaddingRight(),
                mHeaderView.getPaddingBottom());
        mFooterView.setPadding(mFooterView.getPaddingLeft(), mFooterView.getPaddingTop(),
                mFooterView.getPaddingRight(), 0);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.addHeaderView(mHeaderContainer);
        super.addFooterView(mFooterContainer);
        super.setAdapter(adapter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        final int y = (int)ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    resetScroller();
                }
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mScrolling) {
                    mState = detectRefreshAction(y);

                    if (mState != State.NONE) {
                        mScrolling = true;
                    }
                }

                if (mState != State.NONE) {
                    doPullAction(y);
                }

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mScrolling = false;
                doReleasePull();
                break;
        }
        return super.onTouchEvent(ev);
    }

    private State detectRefreshAction(int y) {
        final int dy = y - mLastY;
        if (dy >= 20) {
            mHeaderView.setVisibility(View.VISIBLE);
            return State.PULL_DOWN;
        } else if (dy <= 20) {
            mFooterView.setVisibility(View.VISIBLE);
            return State.PULL_UP;
        }
        return State.NONE;
    }

    private void doPullAction(int y) {
        switch (mState) {
            case PULL_DOWN:
                doPullDownAction(y);
                break;
            case PULL_UP:
                doPullUpAction(y);
                break;
        }
    }

    private void doPullDownAction(int y) {
        mHeaderView.setPadding(mHeaderView.getPaddingLeft(), y - mLastY,
                mHeaderView.getPaddingRight(), mHeaderView.getPaddingBottom());
    }

    private void doPullUpAction(int y) {
        mFooterView.setPadding(mFooterView.getPaddingLeft(), mFooterView.getPaddingTop(),
                mFooterView.getPaddingRight(), mLastY - y);
    }

    private void doReleasePull() {
        switch (mState) {
            case PULL_DOWN:
                mScroller.startScroll(0, mHeaderView.getPaddingTop(), 0,
                        -mHeaderView.getPaddingTop(), SCROLLING_DURATION);
                break;
            case PULL_UP:
                mScroller.startScroll(0, mFooterView.getPaddingBottom(), 0,
                        -mFooterView.getPaddingBottom(), SCROLLING_DURATION);
                break;
        }
        post(mMovement);
    }

    public void onRefreshCompleted() {
        mHeaderView.setVisibility(View.GONE);
        mFooterView.setVisibility(View.GONE);
    }

    public enum State {
        NONE, PULL_DOWN, PULL_UP
    }

    public interface onRefreshListener {
        public void onPullUp();

        public void onPullDown();
    }
}
