package com.abs.inputmethod.pinyin.contacts;

/**
 * Created by wcy on 2017/11/26.
 */
public interface ContactsAdapterDelegate<T> {
    Class<? extends ContactsViewHolder<T>> getViewHolderClass(int position);
}
