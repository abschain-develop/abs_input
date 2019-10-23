package com.abs.inputmethod.pinyin.utils.file_choose;

import android.text.TextUtils;

import java.io.Serializable;

public class SDCardFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目录
     */
    public static final int TYPE_DIRECTORY = 1;
    /**
     * 音频文件，包含后缀"amr", "acc", "mp3", "wav", "ogg"
     */
    public static final int TYPE_FILE_AUDIO = 2;
    /**
     * 压缩文件，包含后缀"zip", "tar", "rar"
     */
    public static final int TYPE_FILE_COMPRESS = 3;
    /**
     * execl文件，包含后缀"xls", "xlsx"
     */
    public static final int TYPE_FILE_EXECL = 5;
    /**
     * 图片文件，包含后缀
     */
    public static final int TYPE_FILE_IMAGE = 6;
    /**
     * 其他文件
     */
    public static final int TYPE_FILE_OTHER = 7;
    /**
     * pdf文件，包含后缀"pdf"
     */
    public static final int TYPE_FILE_PDF = 8;
    /**
     * ppt文件，包含后缀"ppt", "pptx"
     */
    public static final int TYPE_FILE_PPT = 9;
    /**
     * txt文件，包含后缀txt
     */
    public static final int TYPE_FILE_TXT = 10;
    /**
     * 视频文件，包含后缀
     */
    public static final int TYPE_FILE_VIDEO = 11;
    /**
     * word文件，包含后缀"doc", "docx"
     */
    public static final int TYPE_FILE_WORD = 12;

    /**
     * rar
     */
    public static final int TYPE_FILE_COMPRESS_RAR = 13;
    /**
     * zip
     */
    public static final int TYPE_FILE_COMPRESS_ZIP = 14;
    public static final int TYPE_FILE_MORSE = 15;

    private String filename;
    private String filePath;
    private int fileType;
    private long fileTime;
    private long fileSize;
    private String fatherPath;

    private static final String[] supportVideo = {"mp4", "rm", "rmvb", "avi", "flv",
            "wmv", "m4v", "mpeg", "mov", "3gp", "asf", "mkv"};
    private static final String[] supportImage = {"jpg", "gif", "png", "jpeg", "bmp"};

    private static final String[] supportAudio = {"amr", "acc", "mp3", "wav", "ogg"};

    public SDCardFile(String filename, String filePath, int fileType,
                      long fileTime, long fileSize, String fatherPath) {
        super();
        this.filename = filename;
        this.filePath = filePath;
        this.fileType = fileType;
        this.fileTime = fileTime;
        this.fileSize = fileSize;
        this.fatherPath = fatherPath;
    }

    public String getFilename() {
        return filename;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getFileType() {
        return fileType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SDCardFile other = (SDCardFile) obj;
        if (fatherPath == null) {
            if (other.fatherPath != null)
                return false;
        } else if (!fatherPath.equals(other.fatherPath))
            return false;
        if (filePath == null) {
            if (other.filePath != null)
                return false;
        } else if (!filePath.equals(other.filePath))
            return false;
        if (fileSize != other.fileSize)
            return false;
        if (fileTime != other.fileTime)
            return false;
        if (fileType != other.fileType)
            return false;
        if (filename == null) {
            if (other.filename != null)
                return false;
        } else if (!filename.equals(other.filename))
            return false;
        return true;
    }

    public static int getFileType(String filename) {

        if (TextUtils.isEmpty(filename)) {
            return TYPE_FILE_OTHER;
        }

        int index = filename.lastIndexOf(".");

        if (index > 0) {
            String suffix = filename.substring(index + 1);

            if (isAudioType(filename))
                return TYPE_FILE_AUDIO;
            else if (suffix.equalsIgnoreCase("zip"))
                return TYPE_FILE_COMPRESS_ZIP;
            else if (suffix.equalsIgnoreCase("rar"))
                return TYPE_FILE_COMPRESS_RAR;
            else if (suffix.equalsIgnoreCase("tar"))
                return TYPE_FILE_COMPRESS;
            else if (suffix.equalsIgnoreCase("xls")
                    || suffix.equalsIgnoreCase("xlsx"))
                return TYPE_FILE_EXECL;
            else if (isImageType(filename))
                return TYPE_FILE_IMAGE;
            else if (suffix.equalsIgnoreCase("pdf"))
                return TYPE_FILE_PDF;
            else if (suffix.equalsIgnoreCase("ppt")
                    || suffix.equalsIgnoreCase("pptx"))
                return TYPE_FILE_PPT;
            else if (suffix.equalsIgnoreCase("txt"))
                return TYPE_FILE_TXT;
            else if (isVideoType(filename))
                return TYPE_FILE_VIDEO;
            else if (suffix.equalsIgnoreCase("doc")
                    || suffix.equalsIgnoreCase("docx"))
                return TYPE_FILE_WORD;
            else if (suffix.equalsIgnoreCase("morse"))
                return TYPE_FILE_MORSE;

        }

        return TYPE_FILE_OTHER;
    }

    public static boolean isVideoType(String filename) {
        String suffix = filename
                .substring(filename.lastIndexOf(".") + 1)
                .toLowerCase().trim();
        for (String type : supportVideo) {
            if (suffix.equals(type))
                return true;
        }
        return false;
    }

    public static boolean isImageType(String filename) {
        String suffix = filename
                .substring(filename.lastIndexOf(".") + 1)
                .toLowerCase().trim();
        for (String type : supportImage) {
            if (suffix.equals(type))
                return true;
        }
        return false;
    }

    public static boolean isAudioType(String filename) {
        String suffix = filename
                .substring(filename.lastIndexOf(".") + 1)
                .toLowerCase().trim();
        for (String type : supportAudio) {
            if (suffix.equals(type))
                return true;
        }
        return false;
    }

}
