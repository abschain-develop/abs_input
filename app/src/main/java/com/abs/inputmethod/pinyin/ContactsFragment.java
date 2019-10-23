package com.abs.inputmethod.pinyin;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.abs.inputmethod.pinyin.constant.LoginStatus;
import com.abs.inputmethod.pinyin.contacts.AddContactActivity;
import com.abs.inputmethod.pinyin.contacts.ContactDetailsActivity;
import com.abs.inputmethod.pinyin.contacts.ContactsAdapter;
import com.abs.inputmethod.pinyin.contacts.ContactsAdapterDelegate;
import com.abs.inputmethod.pinyin.contacts.ContactsViewHolder;
import com.abs.inputmethod.pinyin.contacts.ContactsViewHolderListener;
import com.abs.inputmethod.pinyin.contacts.IndexBar;
import com.abs.inputmethod.pinyin.contacts.viewholder.CompanyIndexViewHolder;
import com.abs.inputmethod.pinyin.contacts.viewholder.CompanyNameViewHolder;
import com.abs.inputmethod.pinyin.db.Contacts;
import com.abs.inputmethod.pinyin.db.ContactsList;
import com.abs.inputmethod.pinyin.db.ContactsUtil;
import com.abs.inputmethod.pinyin.dialog.CustomDialog;
import com.abs.inputmethod.pinyin.dialog.CustomSingleBtnDialog;
import com.abs.inputmethod.pinyin.utils.FileUtil;
import com.abs.inputmethod.pinyin.utils.FormatUtil;
import com.abs.inputmethod.pinyin.utils.Utils;
import com.abs.inputmethod.pinyin.utils.file_choose.PhoneFileListActivity;
import com.google.gson.Gson;
import com.tb.inputmethod.pinyin.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactsFragment extends Fragment implements View.OnClickListener,
        IndexBar.OnIndexChangedListener,
        Handler.Callback {


    private static final String EXTRA_CONTENT = "contacts";
    private static final String THIS_FILE = "ContactsFragment";

    /**
     * 联系人加载完毕
     */
    private static final int MSG_LOAD_CONTACT_COMPLETE = 0;
    private static final int MSG_TOAST_MSG = 1;
    private static final int MSG_REFRESH_CONTACTS = 2;
    private static final int MSG_DISMISS_EXPORT_NOTICE_DIALOG = 3;
    /**
     * 请求权限req code
     */
    public static final int REQUEST_PERMISSION_CODE_EXPORT = 1;
    public static final int REQUEST_CHOOSE_FILE = 2;
    public static final int REQUEST_PERMISSION_CODE_IMPORT = 3;

    private View logoutView;
    private View noneView;
    private ImageView rightBtn;

    private RecyclerView contactRv;
    private IndexBar ibIndicator;
    private TextView tvIndicator;

    private List<Contacts> contactsList = new ArrayList<>();
    private ContactsAdapter<Contacts> adapter;

    private ExecutorService loadContact;
    private Handler handler;

    private PopupWindow menuPopup;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private ExecutorService exportContactsEs;
    private boolean isExportingContacts = false;

    private CustomSingleBtnDialog exportNoticeDialog;

    private boolean isImportingContacts = false;
    private ExecutorService importContactsEs;
    private CustomDialog importNoticeDialog;

    public static ContactsFragment newInstance(String content) {
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_CONTENT, content);
        ContactsFragment tabContentFragment = new ContactsFragment();
        tabContentFragment.setArguments(arguments);
        return tabContentFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(THIS_FILE, "onCreateView");
        View contentView = inflater.inflate(R.layout.fragment_contacts, null);

        ((TextView) contentView.findViewById(R.id.title_tv)).setText(R.string.fragment_contacts_title);
        contentView.findViewById(R.id.title_left_btn).setVisibility(View.INVISIBLE);
        rightBtn = contentView.findViewById(R.id.title_right_btn);
        rightBtn.setImageResource(R.drawable.title_btn_more);
        rightBtn.setOnClickListener(this);

        // logout layout
        logoutView = contentView.findViewById(R.id.fragment_contact_logout_layout);
        contentView.findViewById(R.id.fragment_contact_login_btn).setOnClickListener(this);

        // 无联系人页面
        noneView = contentView.findViewById(R.id.fragment_contacts_list_none_layout);
        contentView.findViewById(R.id.contact_list_none_add).setOnClickListener(this);
        contentView.findViewById(R.id.contact_list_none_import).setOnClickListener(this);

        contactRv = contentView.findViewById(R.id.fragment_contact_lists_rv);
        ibIndicator = contentView.findViewById(R.id.fragment_contact_lists_ib_indicator);
        tvIndicator = contentView.findViewById(R.id.fragment_contact_lists_tv_indicator);

        contactRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        contactRv.setAdapter(adapter);

        ibIndicator.setOnIndexChangedListener(this);

        return contentView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new ContactsAdapter<>(contactsList, delegate, new ContactsViewHolderListener() {
            @Override
            public void itemOnClick(Contacts contact) {
                Intent intent = new Intent(getActivity(), ContactDetailsActivity.class);
                intent.putExtra(ContactDetailsActivity.CONTACT_ID, contact.getID());
                getActivity().startActivity(intent);
            }
        });
        handler = new Handler(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (InputMethodApplication.getInstance().getLoginStatus() == LoginStatus.LOGGED) {
            logoutView.setVisibility(View.GONE);
            rightBtn.setVisibility(View.VISIBLE);

            loadContact();
        } else {
            logoutView.setVisibility(View.VISIBLE);
            rightBtn.setVisibility(View.INVISIBLE);
        }
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
    public void onDestroy() {
        super.onDestroy();
        dismissMenu();
        if (exportNoticeDialog != null && exportNoticeDialog.isShowing()) {
            exportNoticeDialog.dismiss();
        }
        if (importNoticeDialog != null && importNoticeDialog.isShowing()) {
            importNoticeDialog.dismiss();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_right_btn:
                // show contacts menu
                showMenu(view);
                break;
            case R.id.fragment_contact_login_btn:
                // 立即登录
                enterLogin();
                break;
            case R.id.fragment_contacts_menu_add:
            case R.id.contact_list_none_add:
                // 新建联系人
                enterAddContact();
                break;
            case R.id.fragment_contacts_menu_import:
            case R.id.contact_list_none_import:
                // 导入联系人
                importContacts();
                break;
            case R.id.fragment_contacts_menu_export:
                // 导出联系人
                exportContacts();
                break;
        }
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

    private void enterAddContact() {
        Log.d(THIS_FILE, "enter add contact view");
        dismissMenu();
        startActivity(new Intent(getActivity(), AddContactActivity.class));
    }

    public void enterLogin() {
        startActivity(new Intent(getActivity(), ImportKeystoreActivity.class));
    }

    public void showMenu(View anchor) {
        View view = getLayoutInflater().inflate(R.layout.fragment_contacts_menu, null);

        if (menuPopup == null) {
            menuPopup = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            menuPopup.setOutsideTouchable(true);
            view.findViewById(R.id.fragment_contacts_menu_add).setOnClickListener(this);
            view.findViewById(R.id.fragment_contacts_menu_import).setOnClickListener(this);
            view.findViewById(R.id.fragment_contacts_menu_export).setOnClickListener(this);
        }
        if (menuPopup.isShowing()) {
            menuPopup.dismiss();
        } else {
            menuPopup.showAsDropDown(anchor, Utils.dp2px(getActivity(), -84), Utils.dp2px(getActivity(), -10));
        }
    }

    private void dismissMenu() {
        if (menuPopup != null && menuPopup.isShowing()) {
            menuPopup.dismiss();
        }
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

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_LOAD_CONTACT_COMPLETE:
                if (contactsList.size() < 1) {
                    // 无联系人
                    noneView.setVisibility(View.VISIBLE);
                } else {
                    noneView.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
                break;
            case MSG_TOAST_MSG:
                if (message.obj != null) {
                    Toast.makeText(getActivity(), message.obj.toString(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(THIS_FILE, "MSG_TOAST_MSG message.obj == null");
                }
                break;
            case MSG_REFRESH_CONTACTS:
                loadContact();
                break;
            case MSG_DISMISS_EXPORT_NOTICE_DIALOG:
                if (exportNoticeDialog != null && exportNoticeDialog.isShowing()) {
                    exportNoticeDialog.dismiss();
                }
        }
        return false;
    }

    /**
     * 导出联系人
     */
    public void exportContacts() {
        dismissMenu();
        // 判断当前系统是否是Android6.0(对应API 23)以及以上
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            // 判断是否含有了写文件的权限
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE_EXPORT);
                return;
            }
        }

        if (isExportingContacts) {
            sendToastMsg("联系人正在导出，请稍等");
            return;
        }

        isExportingContacts = true;

        final String filePath = FileUtil.getExportFilePath();

        if (exportNoticeDialog == null) {
            exportNoticeDialog = new CustomSingleBtnDialog(getActivity(), R.style.noTitleDialog);
            exportNoticeDialog.setMessage("联系人导出成功后的路径：\n"
                    + filePath.replace(Environment.getExternalStorageDirectory().getAbsolutePath(), "/内部存储"));
            exportNoticeDialog.setYesOnclickListener("知道了", new CustomSingleBtnDialog.OnConfirmOnClickListener() {
                @Override
                public void onConfirmClick() {
                    exportNoticeDialog.dismiss();
                }
            });
        }
        if (!exportNoticeDialog.isShowing()) {
            exportNoticeDialog.show();
        }

        if (exportContactsEs == null) {
            exportContactsEs = Executors.newSingleThreadExecutor();
        }
        exportContactsEs.submit(new Runnable() {
            @Override
            public void run() {
                // 导出数据
                ArrayList<Contacts> contacts = ContactsUtil.getContacts();

                if (contacts != null && contacts.size() > 0) {
                    boolean result = FileUtil.writeToFile(filePath, new ContactsList(contacts).toString());
                    isExportingContacts = false;
                    sendToastMsg(result ? "联系人导出成功" : "联系人导出失败");
                } else {
                    isExportingContacts = false;
                    handler.sendEmptyMessage(MSG_DISMISS_EXPORT_NOTICE_DIALOG);
                    sendToastMsg("无联系人导出");
                }
                Log.i(THIS_FILE, "exportContacts complete");
            }
        });

    }

    private void sendToastMsg(String msg) {
        if (handler != null) {
            Message message = Message.obtain();
            message.what = MSG_TOAST_MSG;
            message.obj = msg;
            handler.sendMessage(message);
        } else {
            Log.i(THIS_FILE, "handToastMsg handler is null");
        }
    }

    /**
     * 导入联系人
     */
    public void importContacts() {
        dismissMenu();
        // 判断当前系统是否是Android6.0(对应API 23)以及以上
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            // 判断是否含有了写文件的权限
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.d(THIS_FILE, "importContacts requestPermissions");
                ActivityCompat.requestPermissions(getActivity(), PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE_IMPORT);
                return;
            }
        }

        if (isImportingContacts) {
            sendToastMsg("联系人正在导入，请稍等");
            return;
        }

        // todo
        // 跳转到文件选择器
        Intent intent = new Intent(getActivity(), PhoneFileListActivity.class);
        getActivity().startActivityForResult(intent, REQUEST_CHOOSE_FILE);
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        getActivity().startActivityForResult(intent, REQUEST_CHOOSE_FILE);
    }

    public void importContactsToSql(final String path) {
        Log.d(THIS_FILE, "path:" + path);
        if (importNoticeDialog == null) {
            importNoticeDialog = new CustomDialog(getActivity(), R.style.noTitleDialog);
            importNoticeDialog.setMessage("若出现联系人数据冲突，是否直接覆盖？");
        }
        importNoticeDialog.setYesOnclickListener("覆盖", new CustomDialog.OnConfirmOnClickListener() {
            @Override
            public void onConfirmClick() {
                importNoticeDialog.dismiss();
                importContactsToSql(path, true);
            }
        });
        importNoticeDialog.setCancelOnClickListener("忽略", new CustomDialog.OnCancelOnClickListener() {
            @Override
            public void onCancelClick() {
                importNoticeDialog.dismiss();
                importContactsToSql(path, false);
            }
        });
        if (!importNoticeDialog.isShowing()) {
            importNoticeDialog.show();
        }
    }

    /**
     * 导入联系人到数据库
     *
     * @param path            文件路径
     * @param autoFixConflict 是否自动解决冲突（即替换）
     */
    private void importContactsToSql(final String path, final boolean autoFixConflict) {
        Log.d(THIS_FILE, "importContactsToSql path:" + path + ", autoFixConflict:" + autoFixConflict);
        isImportingContacts = true;

        if (importContactsEs == null) {
            importContactsEs = Executors.newSingleThreadExecutor();
        }

        importContactsEs.submit(new Runnable() {
            @Override
            public void run() {
                Log.d(THIS_FILE, "importContactsToSql start");

                String contactsStr = FileUtil.readFromFile(path);
                boolean result = false;
                if (!TextUtils.isEmpty(contactsStr)) {
                    try {
                        Log.d(THIS_FILE, "importContactsToSql path:" + path);
                        Log.d(THIS_FILE, "importContactsToSql contactsStr:" + contactsStr);
                        Gson gson = new Gson();
                        ContactsList contactsList = gson.fromJson(contactsStr, ContactsList.class);
                        if (contactsList != null && contactsList.getContacts() != null) {
                            boolean needRefresh = false;
                            if (autoFixConflict) {
                                for (Contacts contact : contactsList.getContacts()) {
                                    needRefresh = true;
                                    ArrayList<Contacts> tmp = ContactsUtil.getContactsByPubKey(contact.getPubKey());
                                    if (tmp == null || tmp.size() < 1) {
                                        Log.d(THIS_FILE, "importContactsToSql inserts contacts:" + contact);
                                        ContactsUtil.insertContacts(contact.getNick(), contact.getAddress(), contact.getPubKey());
                                    } else {
                                        Log.d(THIS_FILE, "importContactsToSql update contacts:" + contact);
                                        ContactsUtil.updateContactById(tmp.get(0).getID(), contact.getNick(), null, null);
                                    }
                                }
                            } else {
                                // 忽略已重复联系人
                                for (Contacts contact : contactsList.getContacts()) {
                                    ArrayList<Contacts> tmp = ContactsUtil.getContactsByPubKey(contact.getPubKey());
                                    if (tmp == null || tmp.size() < 1) {
                                        Log.d(THIS_FILE, "importContactsToSql else inserts contacts:" + contact);
                                        ContactsUtil.insertContacts(contact.getNick(), contact.getAddress(), contact.getPubKey());
                                        needRefresh = true;
                                    }
                                }

                            }

                            if (needRefresh) {
                                handler.sendEmptyMessage(MSG_REFRESH_CONTACTS);
                            }
                        }
                        Log.d(THIS_FILE, "importContactsToSql contactsList:" + contactsList);
                        result = true;
                    } catch (Exception e) {
                        Log.e(THIS_FILE, "importContactsToSql parse error:" + path, e);
                    }
                } else {
                    Log.d(THIS_FILE, "importContactsToSql contactsStr is null");

                }
                isImportingContacts = false;
                sendToastMsg(result ? "联系人导入成功" : "联系人导入失败");
                Log.d(THIS_FILE, "importContactsToSql complete");
            }
        });
    }
}
