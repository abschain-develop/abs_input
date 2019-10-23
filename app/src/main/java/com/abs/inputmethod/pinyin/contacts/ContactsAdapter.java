package com.abs.inputmethod.pinyin.contacts;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter<T> extends RecyclerView.Adapter<ContactsViewHolder<T>> {
    private static final String TAG = "ContactsAdapter";

    private List<T> dataList;
    private ContactsAdapterDelegate<T> delegate;
    private List<Class<? extends ContactsViewHolder<T>>> viewHolderClassList;
    private Object tag;
    private ContactsViewHolderListener listener;

    public ContactsAdapter(List<T> dataList, ContactsAdapterDelegate<T> delegate, ContactsViewHolderListener listener) {
        this.dataList = dataList;
        this.delegate = delegate;
        this.listener = listener;
        viewHolderClassList = new ArrayList<>();
    }

    @Override
    public ContactsViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        try {
            Class<? extends ContactsViewHolder<T>> clazz = viewHolderClassList.get(viewType);
            int resId = getLayoutResId(clazz);
            View view = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
            Constructor constructor = clazz.getConstructor(View.class, ContactsViewHolderListener.class);
            ContactsViewHolder<T> viewHolder = (ContactsViewHolder<T>) constructor.newInstance(view, listener);
            viewHolder.setAdapter(this);
            return viewHolder;
        } catch (Exception e) {
            throw new IllegalStateException("create view holder error", e);
        }
    }

    @Override
    public void onBindViewHolder(ContactsViewHolder<T> holder, int position) {
        try {
            holder.setPosition(position);
            holder.setData(dataList.get(position));
            holder.refresh();
        } catch (Exception e) {
            Log.e(TAG, "bind view holder error", e);
        }
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount:" + dataList.size());
        return dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Class<? extends ContactsViewHolder<T>> clazz = delegate.getViewHolderClass(position);
        if (!viewHolderClassList.contains(clazz)) {
            viewHolderClassList.add(clazz);
        }
        return viewHolderClassList.indexOf(clazz);
    }

    public void insertItem(T t) {
        dataList.add(t);
        notifyItemInserted(dataList.size() - 1);
    }

    public void insertItem(T t, int position) {
        dataList.add(position, t);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        dataList.remove(position);
        notifyItemRemoved(position);
    }

    public List<T> getDataList() {
        return dataList;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    private int getLayoutResId(Class<?> clazz) {
        if (clazz == ContactsViewHolder.class) {
            return 0;
        }
        ContactsLayout layout = clazz.getAnnotation(ContactsLayout.class);
        if (layout == null) {
            // 找不到去父类找
            return getLayoutResId(clazz.getSuperclass());
        }
        return layout.value();
    }
}
