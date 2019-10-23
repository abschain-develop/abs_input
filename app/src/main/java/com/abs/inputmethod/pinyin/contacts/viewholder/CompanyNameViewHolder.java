package com.abs.inputmethod.pinyin.contacts.viewholder;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.abs.inputmethod.pinyin.contacts.ContactDetailsActivity;
import com.abs.inputmethod.pinyin.contacts.ContactsLayout;
import com.abs.inputmethod.pinyin.contacts.ContactsViewHolder;
import com.abs.inputmethod.pinyin.contacts.ContactsViewHolderListener;
import com.abs.inputmethod.pinyin.db.Contacts;
import com.abs.inputmethod.pinyin.utils.RandomUtil;
import com.abs.inputmethod.pinyin.utils.binding.Bind;
import com.tb.inputmethod.pinyin.R;

import java.util.HashMap;

/**
 * Created by wcy on 2018/1/20.
 */
@ContactsLayout(R.layout.fragment_contacts_list_item)
public class CompanyNameViewHolder extends ContactsViewHolder<Contacts> implements View.OnClickListener {
    @Bind(R.id.fragment_contacts_list_item_logo)
    private TextView logoTv;
    @Bind(R.id.tv_name)
    private TextView tvName;
    @Bind(R.id.tv_address)
    private TextView tvAddress;

    private static HashMap<String, Integer> logo = new HashMap<>();
    private ContactsViewHolderListener listener;

    public CompanyNameViewHolder(View itemView, ContactsViewHolderListener listener) {
        super(itemView);
        itemView.setOnClickListener(this);
        this.listener = listener;
    }

    @Override
    public void refresh() {
        if (logo.containsKey(data.getID())) {
            logoTv.setBackgroundResource(logo.get(data.getID()));
        } else {
            int bg = RandomUtil.randomContactLogo();
            logo.put(data.getID(), bg);
            logoTv.setBackgroundResource(bg);
        }
        logoTv.setText(data.getNick().trim().substring(0, 1));
        tvName.setText(data.getNick());
        tvAddress.setText(data.getAddress());
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.itemOnClick(data);
        }
//        Intent intent = new Intent(context, ContactDetailsActivity.class);
//        intent.putExtra(ContactDetailsActivity.CONTACT_ID, data.getID());
//        context.startActivity(intent);
    }
}
