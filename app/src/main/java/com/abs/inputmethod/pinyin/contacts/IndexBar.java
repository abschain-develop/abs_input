package com.abs.inputmethod.pinyin.contacts;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abs.inputmethod.pinyin.utils.Utils;
import com.tb.inputmethod.pinyin.R;

public class IndexBar extends LinearLayout implements View.OnTouchListener {

    private static final String[] INDEXES = new String[]{"正在通讯", "A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
    private static final int TOUCHED_BACKGROUND_COLOR = 0x40000000;
    private static final String THIS_FILE = "IndexBar";
    private OnIndexChangedListener mListener;


    public void setOnIndexChangedListener(OnIndexChangedListener listener) {
        mListener = listener;
    }

    public IndexBar(Context context) {
        this(context, null);
    }

    public IndexBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndexBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public IndexBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.IndexBar);
        float indexTextSize = ta.getDimension(R.styleable.IndexBar_indexTextSize, Utils.dp2px(getContext(), 8));
        int indexTextColor = ta.getColor(R.styleable.IndexBar_indexTextColor, 0xFF616161);
        ta.recycle();

        setOrientation(VERTICAL);
        setOnTouchListener(this);
        for (String index : INDEXES) {
            if (index.equals("正在通讯")) {
                IconFontTextView text = new IconFontTextView(getContext());
                text.setText(Html.fromHtml("<font color=\"#5a5a5a\">&#xe87d;</font>"));
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.dp2px(getContext(), 12));
                text.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(getContext(), 18));
                text.setLayoutParams(params);
                addView(text);
            } else {
                TextView text = new TextView(getContext());
                text.setText(index);
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, indexTextSize);
                text.setTextColor(indexTextColor);
                text.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(getContext(), 18));
                text.setLayoutParams(params);
                addView(text);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                setBackgroundColor(TOUCHED_BACKGROUND_COLOR);
                handle(v, event);
                return true;
            case MotionEvent.ACTION_MOVE:
                handle(v, event);
                return true;
            case MotionEvent.ACTION_UP:
//                setBackgroundColor(Color.TRANSPARENT);
                handle(v, event);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void handle(View v, MotionEvent event) {
        int y = (int) event.getY();
        int height = v.getHeight();
        int position = INDEXES.length * y / height;
        if (position < 0) {
            position = 0;
        } else if (position >= INDEXES.length) {
            position = INDEXES.length - 1;
        }

        String index = INDEXES[position];
        Log.d(THIS_FILE, "handle:" + index);
        boolean showIndicator = event.getAction() != MotionEvent.ACTION_UP;
        if (mListener != null) {
            mListener.onIndexChanged(index, showIndicator);
        }
    }

    public interface OnIndexChangedListener {
        void onIndexChanged(String index, boolean showIndicator);
    }
}
