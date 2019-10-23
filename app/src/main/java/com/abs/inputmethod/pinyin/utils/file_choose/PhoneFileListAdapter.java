package com.abs.inputmethod.pinyin.utils.file_choose;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tb.inputmethod.pinyin.R;

import java.io.File;
import java.util.List;

/**
 * 手机存储list adapter
 */
public class PhoneFileListAdapter extends ArrayAdapter<SDCardFile> {

    private LayoutInflater inflater;

    private List<SDCardFile> datas;

    private static final int TYPE_DIRECTORY = 0;
    private static final int TYPE_FILE = 1;

    private PhoneFileHolder directoryHolder;
    private PhoneFileHolder fileHolder;

    public PhoneFileListAdapter(Context context, List<SDCardFile> data) {
        super(context, 0, data);

        this.inflater = LayoutInflater.from(context);
        this.datas = data;

    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (datas.get(position).getFileType() == SDCardFile.TYPE_DIRECTORY) {
            return TYPE_DIRECTORY;
        } else {
            return TYPE_FILE;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SDCardFile file = datas.get(position);

        int type = getItemViewType(position);

        if (convertView == null) {
            switch (type) {
                case TYPE_DIRECTORY:
                    convertView = inflater.inflate(
                            R.layout.document_phone_file_item_directory, parent,
                            false);
                    directoryHolder = new PhoneFileHolder();
                    directoryHolder.name = convertView.findViewById(R.id.document_list_item_name_tv);
                    directoryHolder.icon = convertView.findViewById(R.id.document_list_item_icon_iv);
                    convertView.setTag(directoryHolder);
                    break;
                default:
                    convertView = inflater.inflate(
                            R.layout.document_phone_file_item_file, parent,
                            false);
                    fileHolder = new PhoneFileHolder();
                    fileHolder.icon = (ImageView) convertView
                            .findViewById(R.id.phone_file_item_icon);
                    fileHolder.name = (TextView) convertView
                            .findViewById(R.id.phone_file_item_name_tv);
                    convertView.setTag(fileHolder);
            }
        } else {
            switch (type) {
                case TYPE_DIRECTORY:
                    directoryHolder = (PhoneFileHolder) convertView.getTag();
                    break;
                default:
                    fileHolder = (PhoneFileHolder) convertView.getTag();
                    break;
            }
        }

        switch (type) {
            case TYPE_DIRECTORY:
                getDirectoryView(file);
                break;
            default:
                getFileView(file);
                break;
        }

        return convertView;
    }

    /**
     * @param file
     */
    private void getDirectoryView(SDCardFile file) {
        if (directoryHolder != null) {
            if (directoryHolder.name != null)
                directoryHolder.name.setText(file.getFilename());
            if (directoryHolder.icon != null) {
                if (file.getFilePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "morse")) {
                    directoryHolder.icon.setImageResource(R.drawable.document_icon_directory_morse);
                } else {
                    directoryHolder.icon.setImageResource(R.drawable.document_icon_directory);
                }

            }
        }
    }

    /**
     * @param file
     */
    private void getFileView(SDCardFile file) {
        if (fileHolder != null) {
            if (fileHolder.name != null) {
                fileHolder.name.setText(file.getFilename());
            }

            if (fileHolder.icon != null) {
                int type = file.getFileType();
                fileHolder.icon.setImageResource(setTypeIcon(type));
            }

        }
    }

    class PhoneFileHolder {
        ImageView icon;
        TextView name;
    }

    private int setTypeIcon(int type) {
        switch (type) {
            case SDCardFile.TYPE_FILE_AUDIO:
                return R.drawable.document_icon_audio;
            case SDCardFile.TYPE_FILE_COMPRESS:
            case SDCardFile.TYPE_FILE_COMPRESS_RAR:
            case SDCardFile.TYPE_FILE_COMPRESS_ZIP:
                return R.drawable.document_icon_compress;
            case SDCardFile.TYPE_FILE_EXECL:
                return R.drawable.document_icon_execl;
            case SDCardFile.TYPE_FILE_IMAGE:
                return R.drawable.document_icon_image;
            case SDCardFile.TYPE_FILE_PDF:
                return R.drawable.document_icon_pdf;
            case SDCardFile.TYPE_FILE_PPT:
                return R.drawable.document_icon_ppt;
            case SDCardFile.TYPE_FILE_TXT:
                return R.drawable.document_icon_txt;
            case SDCardFile.TYPE_FILE_VIDEO:
                return R.drawable.document_icon_video;
            case SDCardFile.TYPE_FILE_WORD:
                return R.drawable.document_icon_word;
            case SDCardFile.TYPE_FILE_MORSE:
                return R.drawable.document_icon_morse;
        }

        return R.drawable.document_icon_other;
    }

}
