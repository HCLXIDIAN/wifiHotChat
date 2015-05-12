package com.ty.hcl.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.ty.hcl.R;

/**
 * Created by huangchuanliang on 2015/3/16.
 */
public class SlidingMenu extends HorizontalScrollView {
    private final String TAG = "SlidingMenu";
    private int mSliding_RightPadding;

    private int mSreenWidth;//

    private int mMenuWidth;
    private int mHalfMenuWidth;

    private boolean isOpen;

    private boolean once;
    public SlidingMenu(Context context) {
        this(context, null);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, null, 0);
    }

    public SlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SlidingMenu,
                defStyleAttr, 0);
        int n = typedArray.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case R.styleable.SlidingMenu_rightPadding:
                    mSliding_RightPadding = typedArray.getDimensionPixelSize(attr,
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, getResources().getDisplayMetrics()));//转锟斤拷锟斤拷准锟竭达拷姆锟斤拷锟斤拷锟�50dip
                    break;
            }
        }
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (!once){
            LinearLayout wrapper = (LinearLayout)getChildAt(0);
            ViewGroup menu = (ViewGroup)wrapper.getChildAt(0);
            ViewGroup content = (ViewGroup)wrapper.getChildAt(1);
            mMenuWidth = mSreenWidth - mSliding_RightPadding;
            menu.getLayoutParams().width = mMenuWidth;
            //content.getLayoutParams().width = mSreenWidth;
        }
        super.measure(widthMeasureSpec,heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed){
            this.scrollTo(mMenuWidth,0);
            once = true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action){
            case MotionEvent.ACTION_UP:
                int scrollx = getScrollX();
                Log.e(TAG,"scrollx +="+scrollx);
                if (scrollx > mHalfMenuWidth){
                    this.smoothScrollTo(mMenuWidth,0);
                    isOpen = false;
                }else{
                    this.smoothScrollTo(0,0);
                     isOpen = true;
                }
        }
        return super.onTouchEvent(ev);
    }

          public void openMenu(){
              if (isOpen)
                  return;
              this.smoothScrollTo(0,0);
              isOpen = true;
          }

           public void closeMenu(){
               if (!isOpen)
                   return;
               this.smoothScrollTo(0,0);
               isOpen = false;
           }

          public void toggle(){
              if (isOpen)
                  closeMenu();
              else
                  openMenu();
          }
}
