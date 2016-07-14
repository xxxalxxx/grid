package com.forcelain.android.awesomerecyclerview.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class AwesomeLayoutManager extends RecyclerView.LayoutManager {

    private static final long TRANSITION_DURATION_MS = 300;
    private static final String TAG = "AwesomeLayoutManager";


    private static final float SCALE_THRESHOLD_PERCENT = 0.66f;
    private static final float ITEM_HEIGHT_PERCENT = 0.75f;
    private SparseArray<View> viewCache = new SparseArray<>();

    private int mAnchorPos;

    public AwesomeLayoutManager(Context context) {

    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }



    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        fill(recycler);
        mAnchorPos = 0;
    }

    private void fill(RecyclerView.Recycler recycler) {

        View anchorView = getAnchorView();
        viewCache.clear();
        for (int i = 0, cnt = getChildCount(); i < cnt; i++) {
            View view = getChildAt(i);
            int pos = getPosition(view);
            viewCache.put(pos, view);
        }

        for (int i = 0; i < viewCache.size(); i++) {
            detachView(viewCache.valueAt(i));
        }

        fillUp(anchorView, recycler);
        fillDown(anchorView, recycler);

        for (int i=0; i < viewCache.size(); i++) {
            recycler.recycleView(viewCache.valueAt(i));
        }
        
      //  updateViewScale();
    }

    private void fillUp(@Nullable View anchorView, RecyclerView.Recycler recycler) {
        int anchorPos;
        int anchorTop = 0;
        if (anchorView != null){
            anchorPos = getPosition(anchorView);
            anchorTop = getDecoratedTop(anchorView);
        } else {
            anchorPos = mAnchorPos;
        }

        boolean fillUp = true;
        int pos = anchorPos - 1;

        int height = getHeight();
        int width = getWidth();

        int viewBottom = anchorTop;
        int viewHeight = (int) (getHeight() * ITEM_HEIGHT_PERCENT);
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(getHeight(), View.MeasureSpec.EXACTLY);


        while (fillUp && pos >= 0){
            int clampedPos = pos % 6;
            View view = viewCache.get(pos);
            if (view == null){
                view = recycler.getViewForPosition(pos);
                addView(view, 0);

                measureChild(view, 0, 0);
                int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);

                boolean rectNewsItem = clampedPos == 0 || clampedPos == 5;
                int viewLeft = rectNewsItem ? 0 : width / 2;
                int viewRight =  rectNewsItem ? decoratedMeasuredWidth : decoratedMeasuredWidth;
                //int viewBottom = rectNewsItem ? viewTop + viewHeight : viewTop + viewHeight / 2;

                layoutDecorated(view, viewLeft, viewBottom - viewHeight, decoratedMeasuredWidth/2, viewBottom);
            } else {
                attachView(view);
                viewCache.remove(pos);
            }
            viewBottom = getDecoratedTop(view);
            fillUp = (viewBottom > 0);
            pos--;
        }
    }

    private void fillDown(@Nullable View anchorView, RecyclerView.Recycler recycler) {
        int anchorPos;
        int anchorTop = 0;
        if (anchorView != null){
            anchorPos = getPosition(anchorView);
            anchorTop = getDecoratedTop(anchorView);
        } else {
            anchorPos = mAnchorPos;
        }

        int pos = anchorPos;
        boolean fillDown = true;
        int height = getHeight();
        int width = getWidth();
        int top = anchorTop;
        int itemCount = getItemCount();
        int viewHeight = width / 2;

        while (fillDown && pos < itemCount){
            View view = viewCache.get(pos);
            int clampedPos = pos % 6;
            if (view == null){

                view = recycler.getViewForPosition(pos);
                addView(view);
                measureChild(view, 0, 0);
                int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view) / 2;
                boolean rectNewsItem = clampedPos == 0 || clampedPos == 5;
                boolean smallTopRow = clampedPos == 1 || clampedPos == 3;
                int viewTop = getDecoratedTop(view);
                int viewLeft;
                int viewRight;
                int viewBottom;

                if (rectNewsItem)  {
                    viewBottom = viewTop + decoratedMeasuredWidth;
                } else if (smallTopRow) {
                    viewBottom = viewTop + decoratedMeasuredWidth / 2;
                } else {
                    viewTop += decoratedMeasuredWidth / 2;
                    viewBottom = viewTop + decoratedMeasuredWidth;
                }

                if (clampedPos == 1 || clampedPos == 2 || clampedPos == 5) {
                    viewLeft = decoratedMeasuredWidth;
                } else {
                    viewLeft = 0;
                }

                viewRight = viewLeft + decoratedMeasuredWidth;


               // int viewLeft = rectNewsItem ? 0 : decoratedMeasuredWidth;
               // int viewRight =  viewLeft + decoratedMeasuredWidth;
             //   int viewBottom = rectNewsItem ? viewTop + decoratedMeasuredWidth : viewTop + decoratedMeasuredWidth / 2;

                layoutDecorated(view, viewLeft, viewTop, viewRight, viewBottom);
                Log.d("__DBG", "l:" + viewLeft + " r:" + viewRight + " t:" + viewTop + " b:" + viewBottom);
            } else {
                attachView(view);
                viewCache.remove(pos);
            }

            top = getDecoratedBottom(view);


            fillDown = top <= height;
            pos++;
        }
    }


    private View getAnchorView() {
        int childCount = getChildCount();
        Rect mainRect = new Rect(0, 0, getWidth(), getHeight());
        int maxSquare = 0;
        View anchorView = null;
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            int top = getDecoratedTop(view);
            int bottom = getDecoratedBottom(view);
            int left = getDecoratedLeft(view);
            int right = getDecoratedRight(view);
            Rect viewRect = new Rect(left, top, right, bottom);
            boolean intersect = viewRect.intersect(mainRect);
            if (intersect){
                int square = viewRect.width() * viewRect.height();
                if (square > maxSquare){
                    anchorView = view;
                }
            }
        }
        return anchorView;
    }
/*
    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        if (position >= getItemCount()) {
            Log.e(TAG, "Cannot scroll to " + position + ", item count is " + getItemCount());
            return;
        }

        LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return AwesomeLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected int getVerticalSnapPreference() {
                return SNAP_TO_START;
            }
        };
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

    private PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        final int firstChildPos = getPosition(getChildAt(0));
        final int direction = targetPosition < firstChildPos ? -1 : 1;

            return new PointF(0, direction);

    }*/

    @Override
    public boolean canScrollVertically() {
        return true;
    }

/*
    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int delta = scrollHorizontallyInternal(dx);
        offsetChildrenHorizontal(-delta);
        fill(recycler);
        return delta;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int delta = scrollVerticallyInternal(dy);
        offsetChildrenVertical(-delta);
        fill(recycler);
        return delta;
    }

    private int scrollVerticallyInternal(int dy) {
        int childCount = getChildCount();
        int itemCount = getItemCount();
        if (childCount == 0){
            return 0;
        }

        final View topView = getChildAt(0);
        final View bottomView = getChildAt(childCount - 1);

        int viewSpan = getDecoratedBottom(bottomView) - getDecoratedTop(topView);
        if (viewSpan <= getHeight()) {
            return 0;
        }

        int delta = 0;
        if (dy < 0){
            View firstView = getChildAt(0);
            int firstViewAdapterPos = getPosition(firstView);
            if (firstViewAdapterPos > 0){
                delta = dy;
            } else {
                int viewTop = getDecoratedTop(firstView);
                delta = Math.max(viewTop, dy);
            }
        } else if (dy > 0){
            View lastView = getChildAt(childCount - 1);
            int lastViewAdapterPos = getPosition(lastView);
            if (lastViewAdapterPos < itemCount - 1){
                delta = dy;
            } else {
                int viewBottom = getDecoratedBottom(lastView);
                int parentBottom = getHeight();
                delta = Math.min(viewBottom - parentBottom, dy);
            }
        }
        return delta;
    }

    private int scrollHorizontallyInternal(int dx) {
        int childCount = getChildCount();
        int itemCount = getItemCount();
        if (childCount == 0){
            return 0;
        }

        final View leftView = getChildAt(0);
        final View rightView = getChildAt(childCount - 1);

        int viewSpan = getDecoratedRight(rightView) - getDecoratedLeft(leftView);
        if (viewSpan <= getWidth()) {
            return 0;
        }

        int delta = 0;
        if (dx < 0){
            View firstView = getChildAt(0);
            int firstViewAdapterPos = getPosition(firstView);
            if (firstViewAdapterPos > 0){
                delta = dx;
            } else {
                int viewLeft = getDecoratedLeft(firstView);
                delta = Math.max(viewLeft, dx);
            }
        } else if (dx > 0){
            View lastView = getChildAt(childCount - 1);
            int lastViewAdapterPos = getPosition(lastView);
            if (lastViewAdapterPos < itemCount - 1){
                delta = dx;
            } else {
                int viewRight = getDecoratedRight(lastView);
                delta = Math.min(viewRight - getWidth(), dx);
            }
        }
        return delta;
    }

    private void measureChildWithDecorationsAndMargin(View child, int widthSpec, int heightSpec) {
        Rect decorRect = new Rect();
        calculateItemDecorationsForChild(child, decorRect);
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
        widthSpec = updateSpecWithExtra(widthSpec, lp.leftMargin + decorRect.left,
                lp.rightMargin + decorRect.right);
        heightSpec = updateSpecWithExtra(heightSpec, lp.topMargin + decorRect.top,
                lp.bottomMargin + decorRect.bottom);
        child.measure(widthSpec, heightSpec);
    }

    private int updateSpecWithExtra(int spec, int startInset, int endInset) {
        if (startInset == 0 && endInset == 0) {
            return spec;
        }
        final int mode = View.MeasureSpec.getMode(spec);
        if (mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.EXACTLY) {
            return View.MeasureSpec.makeMeasureSpec(
                    View.MeasureSpec.getSize(spec) - startInset - endInset, mode);
        }
        return spec;
    }*/
}
