package com.abs.inputmethod.pinyin.utils.file_choose;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tb.inputmethod.pinyin.R;

import java.util.ArrayList;
import java.util.List;

public class CrumbView extends HorizontalScrollView {

    private Resources mRes;
    private LinearLayout mContainer;
    private final String THIS_FILE = "CrumbView";
    private CrumbItemClickListener listener;

    public CrumbView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setHorizontalScrollBarEnabled(false);

        mRes = context.getResources();

        initView(context);
    }

    private void initView(Context context) {
        mContainer = new LinearLayout(context);
        mContainer.setOrientation(LinearLayout.HORIZONTAL);
        mContainer.setPadding(mRes.getDimensionPixelOffset(R.dimen.crumb_view_padding), 0,
                mRes.getDimensionPixelOffset(R.dimen.crumb_view_padding), 0);
        mContainer.setGravity(Gravity.CENTER_VERTICAL);
        addView(mContainer);
    }

    private List<CrumbData> crumbs = new ArrayList<>();

    public void addCrumb(String crumb, String path) {
        Log.d(THIS_FILE, "addCrumb");
        crumbs.add(new CrumbData(crumb, path));
        updateCrumbs();
    }

    public void setCrumbItemClickListener(CrumbItemClickListener listener) {
        this.listener = listener;
    }

    public void removeCrumb() {
        Log.d(THIS_FILE, "removeCrumb");
        if (crumbs.size() > 1) {
            crumbs.remove(crumbs.size() - 1);
        }
        updateCrumbs();
    }

    public void removeCrumbs(String start) {
        Log.d(THIS_FILE, "removeCrumb");

        int startPos = -1;
        for (int i = 0; i < crumbs.size(); i++) {
            String crumb = crumbs.get(i).getName();
            if (crumb.equals(start)) {
                startPos = i + 1;
                break;
            }
        }
        if (startPos > 0) {
            crumbs = crumbs.subList(0, startPos);
        }
        updateCrumbs();
    }

    private void updateCrumbs() {
        Log.d(THIS_FILE, "updateCrumbs");
        // 面包屑的数量
        int numCrumbs = crumbs.size();

        mContainer.removeAllViews();
        for (int i = 0; i < numCrumbs; i++) {
            Log.d(THIS_FILE, "for i:" + i);
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.crumb_item_layout, null);
            TextView tv = itemView.findViewById(R.id.crumb_name);
            tv.setText(crumbs.get(i).getName());
            itemView.setTag(crumbs.get(i));
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(THIS_FILE, "crumbs item click");
                    removeCrumbs(((CrumbData) v.getTag()).getName());
                    if (listener != null) {
                        listener.onItemClick(((CrumbData) v.getTag()).getTag());
                    }
                }
            });
            mContainer.addView(itemView);
        }
        numCrumbs = mContainer.getChildCount();
        Log.d(THIS_FILE, "numCrumbs:" + numCrumbs);

        // 滑动到最后一个
        post(new Runnable() {
            @Override
            public void run() {
                fullScroll(ScrollView.FOCUS_RIGHT);
            }
        });
    }


}