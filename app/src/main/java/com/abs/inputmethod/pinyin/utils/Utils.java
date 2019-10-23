/**
 *
 */
package com.abs.inputmethod.pinyin.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.abs.inputmethod.pinyin.ImportKeystoreActivity;
import com.abs.inputmethod.pinyin.InputMethodApplication;
import com.abs.inputmethod.pinyin.IpksManager;
import com.abs.inputmethod.pinyin.MainActivity;
import com.abs.inputmethod.pinyin.MineFragment;
import com.abs.inputmethod.pinyin.constant.LoginStatus;
import com.abs.inputmethod.pinyin.db.Contacts;
import com.abs.inputmethod.pinyin.db.ContactsUtil;
import com.abs.inputmethod.pinyin.db.SessionKey;
import com.abs.inputmethod.pinyin.db.SessionKeyUtil;
import com.abs.inputmethod.pinyin.dialog.CustomDialog;
import com.abs.inputmethod.pinyin.dialog.PickContactsDialog;
import com.abs.inputmethod.pinyin.model.IpksSessionKey;
import com.tb.inputmethod.pinyin.R;

import java.util.ArrayList;

public class Utils {

    private static final String THIS_FILE = "Utils";

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    private static final long SESSION_KEY_TIME_OUT = 3 * 24 * 60 * 60 * 1000;

    public static void setIpksContact(String address, String pubKey) {
        ArrayList<SessionKey> keys = SessionKeyUtil.getSessionKey(address);
        if (keys == null || keys.size() < 1) {
            Log.d(THIS_FILE, "generate new session key");
            String sessionKey = Base64.encodeToString(AlgorithmUtil.generateSessionKey(128), Base64.NO_WRAP);
            long version = System.currentTimeMillis();
            SessionKeyUtil.insertSessionKey(address, version, sessionKey);
            IpksSessionKey ipksSessionKey = new IpksSessionKey(version, sessionKey);
            IpksManager.getInstance().sendMsg(pubKey, ipksSessionKey.toString().getBytes());
            InputMethodApplication.getInstance().setCurrentAddress(address);
            InputMethodApplication.getInstance().setSessionKey(address, new SessionKey(null, address, sessionKey, version));
        } else {
            for (SessionKey sessionKey : keys) {
                Log.d(THIS_FILE, sessionKey.toString());
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - keys.get(0).getVersion() > SESSION_KEY_TIME_OUT) {
                // 更新密钥
                Log.d(THIS_FILE, "update session key");
                String sessionKey = Base64.encodeToString(AlgorithmUtil.generateSessionKey(128), Base64.NO_WRAP);
                SessionKeyUtil.insertSessionKey(address, currentTime, sessionKey);
                IpksSessionKey ipksSessionKey = new IpksSessionKey(currentTime, sessionKey);
                IpksManager.getInstance().sendMsg(pubKey, ipksSessionKey.toString().getBytes());
                InputMethodApplication.getInstance().setCurrentAddress(address);
                InputMethodApplication.getInstance().setSessionKey(address, new SessionKey(null, address, sessionKey, currentTime));
            } else {
                Log.d(THIS_FILE, "set current address");
                InputMethodApplication.getInstance().setCurrentAddress(address);
                InputMethodApplication.getInstance().setSessionKey(address, keys.get(0));
            }
        }
    }

    private static PickContactsDialog pickContactsDialog;
    private static CustomDialog loginDialog;
    private static CustomDialog noContactsDialog;

    public static void setCurrentAddress(final Context context) {
        // choose contacts
        if (InputMethodApplication.getInstance().getLoginStatus() == LoginStatus.LOGGED) {
            // 已登录，复制公钥到粘贴板
            if (IpksManager.getInstance().isRunning()) {
                // 查看是否有联系人
                ArrayList<Contacts> contacts = ContactsUtil.getContacts();
                if (contacts != null && contacts.size() > 0) {
                    // 选择正在通讯联系人
                    if (pickContactsDialog == null) {
                        pickContactsDialog = new PickContactsDialog(context, R.style.noTitleDialog);
                        pickContactsDialog.setItemClickListener(new PickContactsDialog.ListItemClickListener() {
                            @Override
                            public void onItemClick(Contacts contact) {
                                Log.d(THIS_FILE, "onItemClick:" + contact);
                                // 设置正在通讯地址
                                Utils.setIpksContact(contact.getAddress(), contact.getPubKey());
                                pickContactsDialog.dismiss();
                            }
                        });
                        Window window = pickContactsDialog.getWindow();
                        window.setBackgroundDrawableResource(android.R.color.transparent);
                        WindowManager.LayoutParams lp = window.getAttributes();
                        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                        window.setAttributes(lp);
                        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                    }
                    if (!pickContactsDialog.isShowing()) {
                        pickContactsDialog.show();
                    }
                } else {
                    if (noContactsDialog == null) {
                        noContactsDialog = new CustomDialog(context, R.style.noTitleDialog);
                        noContactsDialog.setMessage("当前没有联系人，前往添加并设置");
                        noContactsDialog.setCancelOnClickListener(context.getString(R.string.cancel), new CustomDialog.OnCancelOnClickListener() {
                            @Override
                            public void onCancelClick() {
                                Log.d(THIS_FILE, "取消");
                                noContactsDialog.cancel();
                            }
                        });
                        noContactsDialog.setYesOnclickListener("立即前往", new CustomDialog.OnConfirmOnClickListener() {
                            @Override
                            public void onConfirmClick() {
                                Log.d(THIS_FILE, "登录");
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.putExtra("action", MineFragment.ACTION_SET_ADDRESS);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                context.startActivity(intent);
                                noContactsDialog.cancel();
                            }
                        });
                        Window window = noContactsDialog.getWindow();
                        window.setBackgroundDrawableResource(android.R.color.transparent);
                        WindowManager.LayoutParams lp = window.getAttributes();
                        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                        window.setAttributes(lp);
                        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
                    }

                    if (!noContactsDialog.isShowing()) {
                        noContactsDialog.show();
                    }
                }
            } else {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("action", MineFragment.ACTION_SET_ADDRESS);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
        } else {
            // 未登录
            userNotLogin(context);
        }
    }

    /**
     * 用户未登录
     */
    public static void userNotLogin(final Context context) {
        Log.d(THIS_FILE, "用户未登录");
        //  用户未登录
        if (loginDialog == null) {
            loginDialog = new CustomDialog(context, R.style.noTitleDialog);
            loginDialog.setMessage("需要登录摩尔斯输入法才能使用加解密功能，是否登录？");
            loginDialog.setCancelOnClickListener(context.getString(R.string.cancel), new CustomDialog.OnCancelOnClickListener() {
                @Override
                public void onCancelClick() {
                    Log.d(THIS_FILE, "取消登录");
                    loginDialog.cancel();
                }
            });
            loginDialog.setYesOnclickListener("登录", new CustomDialog.OnConfirmOnClickListener() {
                @Override
                public void onConfirmClick() {
                    Log.d(THIS_FILE, "登录");
                    Intent intent = new Intent(context, ImportKeystoreActivity.class);
                    intent.putExtra("action", ImportKeystoreActivity.ACTION_IMPORT_AND_SET_ADDRESS);
                    context.startActivity(intent);
                    loginDialog.cancel();
                }
            });
            Window window = loginDialog.getWindow();
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            window.setAttributes(lp);
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
        if (!loginDialog.isShowing()) {
            loginDialog.show();
        }
    }

}
