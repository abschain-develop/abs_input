package com.abs.inputmethod.pinyin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.abs.inputmethod.pinyin.dialog.CustomDialog;
import com.abs.inputmethod.pinyin.utils.FileUtil;
import com.tb.inputmethod.pinyin.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private static final String THIS_FILE = "MainActivity";
    private TabLayout tabLayout;
    private ViewPager contentVp;

    private List<String> tabIndicators;
    private List<Integer> tabImgs;
    private ContactsFragment contactsFragment;
    private MineFragment mineFragment;
    private ContentPagerAdapter contentPagerAdapter;
    private CustomDialog requestPermissionDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tab_layout);
        contentVp = findViewById(R.id.content_vp);

        initContent();
        initTab();
        Log.d(THIS_FILE, "onCreate");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestPermissionDialog != null && requestPermissionDialog.isShowing()) {
            requestPermissionDialog.dismiss();
        }
    }

    private void initContent() {
        tabIndicators = new ArrayList<>();
        tabImgs = new ArrayList<>();
        tabIndicators.add(getString(R.string.fragment_contacts_title));
        tabImgs.add(R.drawable.tab_menu_icon_contact);
        contactsFragment = ContactsFragment.newInstance(getString(R.string.fragment_contacts_title));
        tabIndicators.add("我的");
        tabImgs.add(R.drawable.tab_menu_icon_mine);
        mineFragment = MineFragment.newInstance("我的");

        contentPagerAdapter = new ContentPagerAdapter(getSupportFragmentManager());
        contentVp.setAdapter(contentPagerAdapter);
    }

    private void initTab() {
        //让底部tab处于可以适配的状态（类似wrap_content），占满屏幕
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        //让指示器高度为0
        tabLayout.setSelectedTabIndicatorHeight(0);
        //设置底部阴影为10px,位置在于viewpager相交的地方
        ViewCompat.setElevation(tabLayout, 10);
        //管理viewpager
        tabLayout.setupWithViewPager(contentVp);
        for (int i = 0; i < tabIndicators.size(); i++) {
            TabLayout.Tab itemTab = tabLayout.getTabAt(i);
            if (itemTab != null) {
                //设置自定义底部布局
                itemTab.setCustomView(R.layout.item_tab_layout_custom);
                TextView itemTv = itemTab.getCustomView().findViewById(R.id.tv_menu_item);
                itemTv.setText(tabIndicators.get(i));
                ImageView itemIv = itemTab.getCustomView().findViewById(R.id.iv_menu_item);
                itemIv.setImageResource(tabImgs.get(i));
            }
        }
        // default show selected tab
        tabLayout.getTabAt(1).select(); //默认选中某项放在加载viewpager之后
        tabLayout.getTabAt(1).getCustomView().setSelected(true);
    }

    class ContentPagerAdapter extends FragmentPagerAdapter {

        public ContentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return contactsFragment;
                default:
                    return mineFragment;
            }
        }

        @Override
        public int getCount() {
            return tabIndicators.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabIndicators.get(position);
        }
    }

    /**
     * 获取应用详情页面intent
     *
     * @return
     */
    public static Intent getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        return localIntent;
    }

    /**
     * 处理权限被拒接逻辑
     */
    private void dealPermissionDenied() {
        if (requestPermissionDialog == null) {
            requestPermissionDialog = new CustomDialog(this, R.style.noTitleDialog);
            requestPermissionDialog.setMessage("需要开启权限后才能使用此功能");
            requestPermissionDialog.setYesOnclickListener("去设置", new CustomDialog.OnConfirmOnClickListener() {
                @Override
                public void onConfirmClick() {
                    requestPermissionDialog.dismiss();
                    Intent intent = getAppDetailSettingIntent(MainActivity.this);
                    startActivity(intent);
                }
            });
            requestPermissionDialog.setCancelOnClickListener(getString(R.string.cancel), new CustomDialog.OnCancelOnClickListener() {
                @Override
                public void onCancelClick() {
                    requestPermissionDialog.dismiss();
                }
            });
        }
        if (!requestPermissionDialog.isShowing()) {
            requestPermissionDialog.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ContactsFragment.REQUEST_PERMISSION_CODE_IMPORT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //已获取权限
                    Log.d(THIS_FILE, "读取外部存储数据权限申请成功");
                    if (contactsFragment != null) {
                        contactsFragment.importContacts();
                    }
                } else {
                    dealPermissionDenied();
                }
                break;
            case ContactsFragment.REQUEST_PERMISSION_CODE_EXPORT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //已获取权限
                    Log.d(THIS_FILE, "写入外部存储数据权限申请成功");
                    if (contactsFragment != null) {
                        contactsFragment.exportContacts();
                    }
                } else {
                    dealPermissionDenied();
                }
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(THIS_FILE, "onActivityResult requestCode:" + requestCode);
        if (requestCode == ContactsFragment.REQUEST_CHOOSE_FILE) {
            Log.d(THIS_FILE, "onActivityResult REQUEST_CHOOSE_FILE:");
            if (resultCode == RESULT_OK && data != null) {
                String path = data.getStringExtra("path");
                Log.d(THIS_FILE, "path:" + path);
                if (contactsFragment != null) {
                    contactsFragment.importContactsToSql(path);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }

}
