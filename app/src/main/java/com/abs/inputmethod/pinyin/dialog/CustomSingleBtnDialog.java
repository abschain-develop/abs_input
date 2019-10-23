package com.abs.inputmethod.pinyin.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tb.inputmethod.pinyin.R;

public class CustomSingleBtnDialog extends Dialog {

    private static final String THIS_FILE = "CustomDialog";
    private Button confirmBtn;//确定按钮
    private TextView titleTV;//消息标题文本
    private TextView messageTv;//消息提示文本
    private String titleStr;//从外界设置的title文本
    private String messageStr;//从外界设置的消息文本
    //确定文本和取消文本的显示的内容
    private String confirmStr;
    private OnConfirmOnClickListener yesOnclickListener;//确定按钮被点击了的监听器

    public CustomSingleBtnDialog(@NonNull Context context) {
        super(context);
    }

    public CustomSingleBtnDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CustomSingleBtnDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    /**
     * 设置确定按钮的显示内容和监听
     *
     * @param str
     * @param yesOnclickListener
     */
    public void setYesOnclickListener(String str, OnConfirmOnClickListener yesOnclickListener) {
        if (str != null) {
            confirmStr = str;
        }
        this.yesOnclickListener = yesOnclickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(THIS_FILE, "dialog oncreate");
        setContentView(R.layout.custom_single_btn_dialog);
        //空白处不能取消动画
        setCanceledOnTouchOutside(false);
        //初始化界面控件
        initView();

        //初始化界面数据
        initData();
        //初始化界面控件的事件
        initEvent();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        confirmBtn = findViewById(R.id.custom_dialog_confirm_btn);
        titleTV = findViewById(R.id.custom_dialog_title_tv);
        messageTv = findViewById(R.id.custom_dialog_message_tv);
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void initData() {
        //如果用户自定了title和message
        if (titleStr != null) {
            titleTV.setText(titleStr);
        }
        if (messageStr != null) {
            messageTv.setText(messageStr);
        }
        //如果设置按钮文字
        if (confirmStr != null) {
            confirmBtn.setText(confirmStr);
        }
    }

    /**
     * 初始化界面的确定和取消监听
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (yesOnclickListener != null) {
                    yesOnclickListener.onConfirmClick();
                }
            }
        });
    }

    /**
     * 从外界Activity为Dialog设置标题
     *
     * @param title
     */
    public void setTitle(String title) {
        titleStr = title;
    }

    /**
     * 从外界Activity为Dialog设置message
     *
     * @param message
     */
    public void setMessage(String message) {
        messageStr = message;
    }

    public interface OnCancelOnClickListener {
        public void onCancelClick();
    }

    public interface OnConfirmOnClickListener {
        public void onConfirmClick();
    }
}
