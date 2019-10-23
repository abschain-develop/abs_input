package com.abs.inputmethod.pinyin.contacts.viewholder;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.abs.inputmethod.pinyin.contacts.ContactsLayout;
import com.abs.inputmethod.pinyin.contacts.ContactsViewHolder;
import com.abs.inputmethod.pinyin.contacts.ContactsViewHolderListener;
import com.abs.inputmethod.pinyin.db.Contacts;
import com.abs.inputmethod.pinyin.utils.binding.Bind;
import com.tb.inputmethod.pinyin.R;

/**
 * Created by wcy on 2018/1/20.
 */
@ContactsLayout(R.layout.fragment_contacts_list_item_index)
public class CompanyIndexViewHolder extends ContactsViewHolder<Contacts> {
    private static final String THIS_FILE = "CompanyIndexViewHolder";
    @Bind(R.id.tv_index)
    private TextView tvIndex;

    public CompanyIndexViewHolder(View itemView, ContactsViewHolderListener listener) {
        super(itemView);
        tvIndex = itemView.findViewById(R.id.tv_index);
    }

    @Override
    public void refresh() {
        Log.d(THIS_FILE, "data:" + data);
        tvIndex.setText(data.getNick());
    }
}
