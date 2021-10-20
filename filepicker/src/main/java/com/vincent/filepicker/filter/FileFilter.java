package com.vincent.filepicker.filter;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.vincent.filepicker.filter.callback.FileLoaderCallbacks;
import com.vincent.filepicker.filter.callback.FilterResultCallback;
import com.vincent.filepicker.filter.entity.AudioFile;
import com.vincent.filepicker.filter.entity.Directory;
import com.vincent.filepicker.filter.entity.ImageFile;
import com.vincent.filepicker.filter.entity.NormalFile;
import com.vincent.filepicker.filter.entity.VideoFile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Files.FileColumns.MIME_TYPE;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.TITLE;
import static com.vincent.filepicker.filter.callback.FileLoaderCallbacks.TYPE_AUDIO;
import static com.vincent.filepicker.filter.callback.FileLoaderCallbacks.TYPE_FILE;
import static com.vincent.filepicker.filter.callback.FileLoaderCallbacks.TYPE_IMAGE;
import static com.vincent.filepicker.filter.callback.FileLoaderCallbacks.TYPE_VIDEO;

/**
 * Created by Vincent Woo
 * Date: 2016/10/11
 * Time: 10:19
 */

public class FileFilter {
    public static void getImages(FragmentActivity activity, FilterResultCallback<ImageFile> callback) {
        activity.getSupportLoaderManager().restartLoader(0, null,
                new FileLoaderCallbacks(activity, callback, TYPE_IMAGE));
    }

    public static void getVideos(FragmentActivity activity, FilterResultCallback<VideoFile> callback) {
        activity.getSupportLoaderManager().restartLoader(1, null,
                new FileLoaderCallbacks(activity, callback, TYPE_VIDEO));
    }

    public static void getAudios(FragmentActivity activity, FilterResultCallback<AudioFile> callback) {
        activity.getSupportLoaderManager().restartLoader(2, null,
                new FileLoaderCallbacks(activity, callback, TYPE_AUDIO));
    }

    public static void getFiles(FragmentActivity activity,
                                FilterResultCallback<NormalFile> callback, String[] suffix) {
        activity.getSupportLoaderManager().restartLoader(3, null,
                new FileLoaderCallbacks(activity, callback, TYPE_FILE, suffix));
    }


    public static void getFiles(final FragmentActivity activity,
                                final String[] paths, final FilterResultCallback<NormalFile> callback, final String[] suffix) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Directory<NormalFile>> directories = new ArrayList<>();
                for (String path : paths) {
                    getAllFiles(path, suffix, directories);
                }
                if (callback != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResult(directories);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 获取指定目录内所有文件路径
     *
     * @param dirPath 需要查询的文件目录
     * @param suffix  查询类型，比如mp3什么的
     */
    public static List<Directory<NormalFile>> getAllFiles(String dirPath, String[] suffix,
                                                          List<Directory<NormalFile>> directories) {
        File f = new File(dirPath);
        if (!f.exists()) {//判断路径是否存在
            return null;
        }

        File[] files = f.listFiles();

        if (files == null) {//判断权限
            return null;
        }

        for (File _file : files) {//遍历目录
            if (_file.isFile()) {
                for (String _type : suffix) {
                    if (_file.getName().endsWith(_type)) {
                        String _name = _file.getName();
                        String filePath = _file.getAbsolutePath();//获取文件路径
                        String fileName = _file.getName().substring(0, _name.length() - 4);//获取文件名
                        Log.d("LOGCAT", "fileName:" + fileName);
                        Log.d("LOGCAT", "filePath:" + filePath);

                        NormalFile file = new NormalFile();
//                        file.setId(data.getLong(data.getColumnIndexOrThrow(_ID)));
                        file.setName(_name);
                        file.setPath(filePath);
//                        file.setSize(data.getLong(data.getColumnIndexOrThrow(SIZE)));
//                        file.setDate(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
//                        file.setMimeType(data.getString(data.getColumnIndexOrThrow(MIME_TYPE)));

                        //Create a Directory
                        Directory<NormalFile> directory = new Directory<>();
                        directory.setName(extractName(extractDirectory(file.getPath())));
                        directory.setPath(extractDirectory(file.getPath()));

                        if (!directories.contains(directory)) {
                            directory.addFile(file);
                            directories.add(directory);
                        } else {
                            directories.get(directories.indexOf(directory)).addFile(file);
                        }
                        break;
                    }
                }
            } else if (_file.isDirectory()) {//查询子目录
                getAllFiles(_file.getAbsolutePath(), suffix, directories);
            }
        }
        return directories;
    }


    private static String extractDirectory(String path) {
        return path.substring(0, path.lastIndexOf("/"));
    }

    private static String extractName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }
}
