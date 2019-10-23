package com.abs.inputmethod.pinyin.utils.file_choose;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.abs.inputmethod.pinyin.BaseActivity;
import com.github.promeg.pinyinhelper.Pinyin;
import com.tb.inputmethod.pinyin.R;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件选择器
 */
public class PhoneFileListActivity extends BaseActivity implements AdapterView.OnItemClickListener, Handler.Callback,
        View.OnClickListener {

    private static final String THIS_FILE = "PhoneFileListActivity";
    private final int LOAD_COMPLETE = 0;
    private ListView phoneFileLv;
    private View noneView;
    private PhoneFileListAdapter adapter;
    private List<SDCardFile> datas = new ArrayList<>();

    /**
     * 当前打开的文件列表目录，默认sd card根目录
     */
    private String dir = Environment.getExternalStorageDirectory()
            .getAbsolutePath();
    /**
     * 当前打开文件列表的上一级路径
     */
    private String fatherPath = "";

    protected ProgressDialog dialog;

    private Handler handler;
    private ExecutorService loadDataExecutor;

    private CrumbView crumbView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_phone_file);
        ((TextView) findViewById(R.id.title_tv)).setText("选择文件");
        findViewById(R.id.title_left_btn).setOnClickListener(this);
        dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        handler = new Handler(this);
        loadDataExecutor = Executors.newSingleThreadExecutor();

        phoneFileLv = findViewById(R.id.document_phone_file_lv);
        noneView = findViewById(R.id.document_phone_file_none_layout);
        crumbView = findViewById(R.id.crumb_view);
        crumbView.setCrumbItemClickListener(new CrumbItemClickListener() {
            @Override
            public void onItemClick(String crumb) {
                dir = crumb;
                getData();
            }
        });

        phoneFileLv.setOnItemClickListener(this);

        adapter = new PhoneFileListAdapter(this, datas);
        phoneFileLv.setAdapter(adapter);

        getData();
        crumbView.addCrumb("内部存储", dir);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.title_left_btn:
                finish();
                break;
        }
    }

    /**
     * 手机存储item click listener。
     * <p/>
     * 目录进入子目录，文件则选中。
     */
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                            long arg3) {
        if (datas.get(position).getFileType() == SDCardFile.TYPE_DIRECTORY) {
            dir = datas.get(position).getFilePath();
            // 点击目录时进入子目录
            getData();
            crumbView.addCrumb(datas.get(position).getFilename(), dir);
        } else { // 点击文件时关闭文件管理器，并选中文件
            Intent intent = new Intent();
            intent.putExtra("path", datas.get(position).getFilePath());
            Log.d(THIS_FILE, "path:" + datas.get(position).getFilePath());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    /**
     * 手机存储back，非顶级目录则返回上一级目录
     *
     * @return true，已处于顶级目录；false，非顶级目录。
     */
    public boolean back() {
        if (!TextUtils.isEmpty(fatherPath)) {
            dir = fatherPath;
            getData();
            crumbView.removeCrumb();
            return false;
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        if (back()) {
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        dismissDialog();
    }

    protected void dismissDialog() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }

    /**
     * 获取手机存储文件列表
     *
     * @return
     */
    private void getData() { // 将目录数据填充到链表中
        loadDataExecutor.execute(new Runnable() {

            @Override
            public void run() {

                List<SDCardFile> list = new ArrayList<SDCardFile>();
                SDCardFile cardFile = null;
                File f = new File(dir); // 打开当前目录
                File[] files = f.listFiles(); // 获取当前目录中文件列表

                if (!dir.equals(Environment.getExternalStorageDirectory()
                        .getAbsolutePath())) { // 不充许进入/sdcard上层目录
                    fatherPath = f.getParent();
                } else {
                    fatherPath = "";
                }

                if (files != null) { // 将目录中文件填加到列表中
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].getName().startsWith(".")) {
                            continue;
                        }
                        cardFile = new SDCardFile(files[i].getName(),
                                files[i].getPath(),
                                files[i].isDirectory() ? SDCardFile.TYPE_DIRECTORY
                                        : SDCardFile.getFileType(files[i].getName()),
                                files[i].lastModified(), files[i].length(), fatherPath);
                        list.add(cardFile);
                    }
                }

                Collections.sort(list, new FileComparer1());
                Collections.sort(list, new FileComparer2());

                datas.clear();
                datas.addAll(list);

                handler.sendEmptyMessage(LOAD_COMPLETE);
            }
        });
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case LOAD_COMPLETE:
                if (datas.size() == 0) {
                    noneView.setVisibility(View.VISIBLE);
                } else {
                    noneView.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
                break;
        }
        return false;
    }


    /**
     * SDCardFile排序，按文件名称
     */
    class FileComparer1 implements Comparator<SDCardFile> {
        public int compare(SDCardFile obj1, SDCardFile obj2) {

            // 排序
            String name1 = obj1.getFilename() != null ? obj1.getFilename() : "";
            String name2 = obj2.getFilename() != null ? obj2.getFilename() : "";

            name1 = Pinyin.toPinyin(name1, "");
            name2 = Pinyin.toPinyin(name2, "");
            Collator cnCollator = Collator
                    .getInstance(java.util.Locale.ENGLISH);

            return cnCollator.compare(name1, name2);
        }
    }

    /**
     * SDCardFile排序，按文件类型（是否是目录）
     */
    class FileComparer2 implements Comparator<SDCardFile> {
        public int compare(SDCardFile obj1, SDCardFile obj2) {

            Boolean boolean1 = Boolean
                    .valueOf(obj1.getFileType() == SDCardFile.TYPE_DIRECTORY);
            Boolean boolean2 = Boolean
                    .valueOf(obj2.getFileType() == SDCardFile.TYPE_DIRECTORY);

            return boolean1.equals(boolean2) ? 0
                    : (boolean1.booleanValue() == true ? -1 : 1);
        }

    }

}
