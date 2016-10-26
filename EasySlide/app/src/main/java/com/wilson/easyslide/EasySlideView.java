package com.wilson.easyslide;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * @ClassName EasySlideView
 * @date 2016/10/25 15:33 
 * @author wilson
 * @Description
 * @modifier
 * @modify_time
 */

public class EasySlideView extends FrameLayout {

    public static final int LEFT_DRAG = 0;

    public static final int RIGHT_DRAG = 1;

    private ViewDragHelper mViewDragHelper;

    private View mContent; //内容视图

    private View mHidden; //隐藏视图

    private int mScrollRange;

    private int mDragPosition = LEFT_DRAG; //侧滑方向 左滑或右滑 默认往左滑

    private float mSensibility = 0.5f;//灵敏度 越小越灵敏

    private onSwipeListener mOnSwipeListener;

    private State mScrollState = State.CLOSE;


    enum State{
        OPEN,
        CLOSE,
        DRAGGING
    }

    public interface onSwipeListener{

        void onSwipeStateChange(State state);
    }

    public void setSwipeStateChangeListener(onSwipeListener onSwipeListener){this.mOnSwipeListener = onSwipeListener;}

    public EasySlideView(Context context) {
        this(context,null);
    }

    public EasySlideView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public EasySlideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        mViewDragHelper = ViewDragHelper.create(this,new DragCallBack());

    }


    private void initAttrs(Context context,AttributeSet attrs){

        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.EasySlideView);
        mDragPosition = ta.getInt(R.styleable.EasySlideView_dragPosition,mDragPosition);
        mSensibility = ta.getFloat(R.styleable.EasySlideView_sensor,mSensibility);

        ta.recycle();
    }

    //确定子view的位置
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        layoutContent(false);
    }
    

    private void layoutContent(boolean isOpen){

      Rect contentRect = computeContentViewRec(isOpen);
        mContent.layout(contentRect.left,contentRect.top,contentRect.right,contentRect.bottom);

      Rect hiddenRect = computeHiddenViewRec(contentRect);
        mHidden.layout(hiddenRect.left,hiddenRect.top,hiddenRect.right,hiddenRect.bottom);
    }

    
    /**
     * @Description 
     * @param isOpen  是否打开
     * @return 
     * @throws 
     */
    private Rect computeContentViewRec(boolean isOpen){

        int left = 0;
        if(isOpen)
            left = -mScrollRange;
        return  new Rect(left,0,left+mContent.getWidth(),mContent.getHeight());
    }

    /**
     * 通过内容区域所占矩形坐标计算侧滑菜单的矩形位置区域
     * @param contentRect 内容区域所占矩形
     * @return
     */
    private Rect computeHiddenViewRec(Rect contentRect){
        int left = contentRect.right;
        if(mDragPosition == RIGHT_DRAG)
            left = contentRect.left-mScrollRange;
        return  new Rect(left,0,left+mScrollRange,mHidden.getHeight());
        
    }

    private void open(){

        if(mViewDragHelper.smoothSlideViewTo(mContent,mDragPosition == LEFT_DRAG?-mScrollRange:mScrollRange,0)){
            ViewCompat.postInvalidateOnAnimation(this);
        }

    }

    private void close(){

        if(mViewDragHelper.smoothSlideViewTo(mContent,0,0)){
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void refreshScrollState(){
        int left = mContent.getLeft();
        if(left == 0){
            mScrollState = State.CLOSE;
        }else if(left == -mScrollRange || left == mScrollRange ){
            mScrollState = State.OPEN;
        }else
            mScrollState = State.DRAGGING;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mViewDragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContent = getChildAt(1);
        mHidden = getChildAt(0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mScrollRange = mHidden.getMeasuredWidth();
    }

    private class DragCallBack extends ViewDragHelper.Callback{


        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mContent;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {

            if(mDragPosition == LEFT_DRAG){
                return Math.min(Math.max(-mScrollRange,left),0);
            }else if(mDragPosition == RIGHT_DRAG){
                return Math.min(Math.max(0,left),mScrollRange);
            }
            return  left;
        }


        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return child.getTop();
        }


        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if(changedView == mContent)
                mHidden.offsetLeftAndRight(dx);
            else if(changedView == mHidden)
                mContent.offsetLeftAndRight(dx);

            refreshScrollState();
            if(mOnSwipeListener!=null)
                mOnSwipeListener.onSwipeStateChange(mScrollState);
//            invalidate();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            switch (mDragPosition){
                case LEFT_DRAG:
                    if(xvel == 0&&mContent.getLeft()<-mScrollRange*mSensibility){
                        open();
                    }else if(xvel<0){
                        open();
                    }else
                        close();
                    break;

                case RIGHT_DRAG:
                    if(xvel == 0&&mContent.getLeft()>mScrollRange*mSensibility){
                        open();
                    }else if(xvel>0){
                        open();
                    }else
                        close();
                    break;
            }


        }

        //当child view 是可点击的必须覆盖一下两个方法
        @Override
        public int getViewHorizontalDragRange(View child) {
            return 1;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return 1;
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }
}
