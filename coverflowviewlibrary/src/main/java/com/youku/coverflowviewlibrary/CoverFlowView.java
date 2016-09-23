package com.youku.coverflowviewlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.BaseAdapter;
import android.widget.Scroller;

/**
 * Created by Rrtoyewx on 2016/9/23.
 */

public class CoverFlowView<T extends BaseAdapter> extends View {
    public static final String TAG = CoverFlowView.class.getSimpleName();

    public static final int GRAVITY_TOP_VALUE = 0;
    public static final int GRAVITY_BOTTOM_VALUE = 1;
    public static final int GRAVITY_CENTER_VALUE = 2;

    public static final int LAYOUT_MODE_MATH_PARENT = 0;
    public static final int LAYOUT_MODE_WARP_CONTENT = 1;

    //default value
    private static final int DEFAULT_VISIBLE_COUNTS = 3;
    private static final Interpolator DEFAULT_ANIMATION_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private static final int DEFAULT_LOWEST_ALPHA = 75;
    private static final float DEFAULT_REFLECTION_HEIGHT_FRACTION = 30f;
    private static final int DEFAULT_REFLECTION_GAP = 10;
    private static final int DEFAULT_GRAVITY = GRAVITY_TOP_VALUE;
    private static final int DEFAULT_LAYOUT_MODE = LAYOUT_MODE_MATH_PARENT;


    @CoverFlowViewGravity
    private int mGravity;
    @CoverFlowViewLayoutMode
    private int mLayoutMode;

    private float mDiverAlpha;
    private int mLowestAlpha = DEFAULT_LOWEST_ALPHA;

    private float mReflectionHeightFraction;
    private float mReflectionGap;
    private Matrix mReflectionTransformer;

    private int mVisibleItemCount;
    private int mHalfVisibleItemCount;
    private Matrix mItemTransformer;


    private int mItemOffset = 0;
    private Rect mCoverFlowPaddingRect;

    private T mAdapter;
    private int mItemCount;

    private Paint mDrawChildPaint;
    private PaintFlagsDrawFilter mDrawFilter;


    private Scroller mScroller;
    private Interpolator mAnimationInterpolator = DEFAULT_ANIMATION_INTERPOLATOR;

    @IntDef({GRAVITY_TOP_VALUE, GRAVITY_BOTTOM_VALUE, GRAVITY_CENTER_VALUE})
    public @interface CoverFlowViewGravity {

    }

    @IntDef({LAYOUT_MODE_MATH_PARENT, LAYOUT_MODE_WARP_CONTENT})
    public @interface CoverFlowViewLayoutMode {

    }

    public CoverFlowView(Context context) {
        this(context, null);
    }

    public CoverFlowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CoverFlowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initAttributes(context, attrs);
        initTransformerMatrix();
        initPaint();

        mScroller = new Scroller(context, mAnimationInterpolator);
    }

    @SuppressWarnings("")
    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CoverFlowView);

        mVisibleItemCount = typedArray.getInt(
                R.styleable.CoverFlowView_visible_item,
                DEFAULT_VISIBLE_COUNTS);
        setVisibleItemCount(mVisibleItemCount);

        mReflectionHeightFraction = typedArray.getFraction(
                R.styleable.CoverFlowView_reflection_height,
                100,
                0,
                DEFAULT_REFLECTION_HEIGHT_FRACTION);
        if (mReflectionHeightFraction > 100) {
            mReflectionHeightFraction = 100;
        }
        mReflectionHeightFraction /= 100;

        mReflectionGap = typedArray.getDimension(
                R.styleable.CoverFlowView_reflection_gap,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_REFLECTION_GAP, context.getResources().getDisplayMetrics()));

        mGravity = typedArray.getInt(R.styleable.CoverFlowView_gravity, DEFAULT_GRAVITY);
        mLayoutMode = typedArray.getInt(R.styleable.CoverFlowView_layout_mode, DEFAULT_LAYOUT_MODE);

        typedArray.recycle();

        Log.d(TAG, "mVisibleItemCount : " + mVisibleItemCount
                + " mHalfVisibleItemCount : " + mHalfVisibleItemCount
                + " mReflectionHeightFraction : " + mReflectionHeightFraction
                + " mReflectionGap : " + mReflectionGap
                + " mGravity : " + mGravity
                + " mLayoutMode : " + mLayoutMode);
    }

    private void initTransformerMatrix() {
        mItemTransformer = new Matrix();
        mReflectionTransformer = new Matrix();
    }

    private void initPaint() {
        mDrawChildPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    public void setAdapter(T adapter) {
        mAdapter = adapter;
    }

    public void setVisibleItemCount(int visibleItemCount) {
        if (mVisibleItemCount % 2 == 0) {
            throw new IllegalArgumentException("init visible item count must be an odd number");
        }

        if (visibleItemCount < 3) {
            throw new IllegalArgumentException("visible item count must lager than 3");
        }

        mHalfVisibleItemCount = mVisibleItemCount >> 1;
        mDiverAlpha = (255 - mLowestAlpha) / mHalfVisibleItemCount;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mAdapter == null && mItemCount == 0) {
            return;
        }

        initPaddingRect();
        int maxMeasureChild = -1;
        int measureHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        int measureHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        int measureWidthSize = MeasureSpec.getSize(widthMeasureSpec);

        int availableHeight = measureHeightSize
                - mCoverFlowPaddingRect.bottom
                - mCoverFlowPaddingRect.top;

        int mid = (int) Math.floor(mItemOffset + 0.5);
        calculateActuallyPosition(mid - mHalfVisibleItemCount);
    }

    private int calculateActuallyPosition(int index){

        return -1;
    }


    private void initPaddingRect() {
        if (mCoverFlowPaddingRect == null) {
            mCoverFlowPaddingRect = new Rect();
        }

        mCoverFlowPaddingRect.left = getPaddingLeft();
        mCoverFlowPaddingRect.right = getPaddingRight();
        mCoverFlowPaddingRect.top = getPaddingTop();
        mCoverFlowPaddingRect.bottom = getPaddingBottom();
    }

}
