package com.abs.inputmethod.pinyin.contacts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.abs.inputmethod.pinyin.utils.binding.ViewBinder;

/**
 * Created by wcy on 2017/11/26.
 */
public abstract class ContactsViewHolder<T> extends RecyclerView.ViewHolder {
    protected Context context;
    protected View itemView;
    protected ContactsAdapter<T> adapter;
    protected T data;
    protected int position;

    public ContactsViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        context = itemView.getContext();
        ViewBinder.bind(this, itemView);
    }

    public void setAdapter(ContactsAdapter<T> adapter) {
        this.adapter = adapter;
    }

    public void setData(T t) {
        this.data = t;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public abstract void refresh();
}
