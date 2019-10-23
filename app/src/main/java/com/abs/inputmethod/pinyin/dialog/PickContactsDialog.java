package com.abs.inputmethod.pinyin.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abs.inputmethod.pinyin.InputMethodApplication;
import com.abs.inputmethod.pinyin.contacts.ContactsAdapter;
import com.abs.inputmethod.pinyin.contacts.ContactsAdapterDelegate;
import com.abs.inputmethod.pinyin.contacts.ContactsViewHolder;
import com.abs.inputmethod.pinyin.contacts.ContactsViewHolderListener;
import com.abs.inputmethod.pinyin.contacts.IndexBar;
import com.abs.inputmethod.pinyin.contacts.viewholder.CompanyIndexViewHolder;
import com.abs.inputmethod.pinyin.contacts.viewholder.CompanyNameViewHolder;
import com.abs.inputmethod.pinyin.db.Contacts;
import com.abs.inputmethod.pinyin.db.ContactsUtil;
import com.abs.inputmethod.pinyin.utils.FormatUtil;
import com.tb.inputmethod.pinyin.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PickContactsDialog extends Dialog implements Handler.Callback,
        IndexBar.OnIndexChangedListener {

    private static final String THIS_FILE = "PickContactsDialog";

    private ExecutorService loadContact;
    private Handler handler = new Handler(this);
    private IndexBar ibIndicator;
    private TextView tvIndicator;
    /**
     * 联系人加载完毕
     */
    private static final int MSG_LOAD_CONTACT_COMPLETE = 0;
    private List<Contacts> contactsList = new ArrayList<>();
    private RecyclerView contactRv;
    private ContactsAdapter<Contacts> adapter;
    private ListItemClickListener itemClickListener;

    public PickContactsDialog(@NonNull Context context) {
        super(context);
    }

    public PickContactsDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected PickContactsDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(THIS_FILE, "dialog oncreate");
        setContentView(R.layout.pick_contacts_dialog);
        // 使对话框内容全屏
        getWindow().setLayout((ViewGroup.LayoutParams.MATCH_PARENT), ViewGroup.LayoutParams.MATCH_PARENT);
        //空白处不能取消动画
        setCanceledOnTouchOutside(false);
        adapter = new ContactsAdapter<>(contactsList, delegate, new ContactsViewHolderListener() {
            @Override
            public void itemOnClick(Contacts contact) {
                Log.d(THIS_FILE, "itemOnClick:" + contact);
                // 设置正在通讯联系人
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(contact);
                }
            }
        });
        //初始化界面控件
        initView();

        //初始化界面数据
        initData();
        //初始化界面控件的事件
        initEvent();
    }

    private ContactsAdapterDelegate<Contacts> delegate = new ContactsAdapterDelegate<Contacts>() {
        @Override
        public Class<? extends ContactsViewHolder<Contacts>> getViewHolderClass(int position) {
            Log.d(THIS_FILE, "position:" + position);
            if (contactsList.get(position).isIndex()) {
                return CompanyIndexViewHolder.class;
            } else {
                return CompanyNameViewHolder.class;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(THIS_FILE, "onStart");
        loadContact();
    }

    /**
     * 加载联系人
     */
    private void loadContact() {
        Log.d(THIS_FILE, "loadContact");
        if (loadContact == null) {
            loadContact = Executors.newSingleThreadExecutor();
        }

        loadContact.submit(new Runnable() {
            @Override
            public void run() {
                // 加载联系人
                ArrayList<Contacts> tmpContacts = ContactsUtil.getContacts();
                List<String> indexs = new ArrayList<>();
                ArrayList<Contacts> sortsContacts = new ArrayList<>();
                ArrayList<Contacts> notLetterContacts = new ArrayList<>();
                for (Contacts tmp : tmpContacts) {
                    Log.d(THIS_FILE, tmp.toString());
                    if (tmp.getAddress().equals(InputMethodApplication.getInstance().getCurrentAddress())) {
                        sortsContacts.add(0, tmp);
                        sortsContacts.add(0, new Contacts("正在通讯", true));
                    } else {
                        String index = tmp.getPinyin().substring(0, 1);
                        if (FormatUtil.isLetter(index)) {
                            if (indexs.contains(index)) {
                                sortsContacts.add(tmp);
                            } else {
                                sortsContacts.add(new Contacts(index, true));
                                sortsContacts.add(tmp);
                                indexs.add(index);
                            }
                        } else {
                            notLetterContacts.add(tmp);
                        }
                    }
                }
                if (notLetterContacts.size() > 0) {
                    sortsContacts.add(new Contacts("#", true));
                    sortsContacts.addAll(notLetterContacts);
                }
                contactsList.clear();
                contactsList.addAll(sortsContacts);

                handler.sendEmptyMessage(MSG_LOAD_CONTACT_COMPLETE);
            }
        });
    }


    /**
     * 初始化界面控件
     */
    private void initView() {
        contactRv = findViewById(R.id.fragment_contact_lists_rv);
        contactRv.setLayoutManager(new LinearLayoutManager(getContext()));
        contactRv.setAdapter(adapter);
        ibIndicator = findViewById(R.id.fragment_contact_lists_ib_indicator);
        tvIndicator = findViewById(R.id.fragment_contact_lists_tv_indicator);
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void initData() {

    }

    /**
     * 初始化界面的确定和取消监听
     */
    private void initEvent() {
        findViewById(R.id.dialog_cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        ibIndicator.setOnIndexChangedListener(this);
    }

    @Override
    public boolean handleMessage(Message message) {
        Log.d(THIS_FILE, "handleMessage:" + message.what);
        switch (message.what) {
            case MSG_LOAD_CONTACT_COMPLETE:
                adapter.notifyDataSetChanged();
                break;
        }
        return false;
    }

    public void setItemClickListener(ListItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onIndexChanged(String index, boolean showIndicator) {
        int position = -1;
        for (Contacts contact : contactsList) {
            if (TextUtils.equals(contact.getNick(), index)) {
                position = contactsList.indexOf(contact);
                break;
            }
        }
        if (position != -1) {
            contactRv.smoothScrollToPosition(position);
        }
        if (index.equals("正在通讯")) {
            tvIndicator.setText(Html.fromHtml("&#xe87d"));
        } else {
            tvIndicator.setText(index);
        }
        tvIndicator.setVisibility(showIndicator ? View.VISIBLE : View.GONE);
    }

    public interface ListItemClickListener {
        void onItemClick(Contacts contact);
    }
}