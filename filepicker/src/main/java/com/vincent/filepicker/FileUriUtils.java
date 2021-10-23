package com.vincent.filepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;

import com.vincent.filepicker.filter.callback.FilterResultCallback;
import com.vincent.filepicker.filter.entity.Directory;
import com.vincent.filepicker.filter.entity.NormalFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUriUtils {
    public static String root = Environment.getExternalStorageDirectory().getPath() + "/";

    public static String treeToPath(String path) {
        String path2 = "";
        if (path.contains("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary")) {
            path2 = path.replace("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A", root);
            path2 = path2.replace("%2F", "/");
        } else {
//            path2 = root + TextUtils.getSubString(path + "测试", "document/primary%3A", "测试").replace("%2F", "/");
        }
        return path2;
    }


    //判断是否已经获取了Data权限，改改逻辑就能判断其他目录，懂得都懂
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean isGrant(Context context) {
        for (UriPermission persistedUriPermission : context.getContentResolver().getPersistedUriPermissions()) {
            if (persistedUriPermission.isReadPermission() && persistedUriPermission.getUri().toString().equals("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")) {
                return true;
            }
        }
        return false;
    }

    //直接返回DocumentFile
    public static DocumentFile getDocumentFilePath(Context context, String path, String sdCardUri) {
        DocumentFile document = DocumentFile.fromTreeUri(context, Uri.parse(sdCardUri));
        String[] parts = path.split("/");
        for (int i = 3; i < parts.length; i++) {
            document = document.findFile(parts[i]);
        }
        return document;
    }

    //转换至uriTree的路径
    public static String changeToUri(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String path2 = path.replace("/storage/emulated/0/", "").replace("/", "%2F");
        return "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A" + path2;
    }

    //转换至uriTree的路径
    public static DocumentFile getDoucmentFile(Context context, String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String path2 = path.replace("/storage/emulated/0/", "").replace("/", "%2F");
        return DocumentFile.fromSingleUri(context, Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3A" + path2));
    }


    //转换至uriTree的路径
    public static String changeToUri2(String path) {
        String[] paths = path.replaceAll("/storage/emulated/0/Android/data", "").split("/");
        StringBuilder stringBuilder = new StringBuilder("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata");
        for (String p : paths) {
            if (p.length() == 0) continue;
            stringBuilder.append("%2F").append(p);
        }
        return stringBuilder.toString();

    }


    //转换至uriTree的路径
    public static String changeToUri3(String path) {
        path = path.replace("/storage/emulated/0/", "").replace("/", "%2F");
        return ("content://com.android.externalstorage.documents/tree/primary%3A" + path);

    }

    //获取指定目录的权限
    public static void startFor(String path, Activity context, int REQUEST_CODE_FOR_DIR) {
//        statusHolder.path = path;
        String uri = changeToUri(path);
        Uri parse = Uri.parse(uri);
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT_TREE");
        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, parse);
        }
        context.startActivityForResult(intent, REQUEST_CODE_FOR_DIR);

    }

    //直接获取data权限，推荐使用这种方案
    public static void startForRoot(Activity context, int REQUEST_CODE_FOR_DIR) {
        Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata%2Fcom.tencent.mm%2FMicroMsg%2FDownload");
//        DocumentFile documentFile = DocumentFile.fromTreeUri(context, uri1);
        String uri = changeToUri(Environment.getExternalStorageDirectory().getPath());
        uri = uri + "/document/primary%3A" + Environment.getExternalStorageDirectory().getPath().replace("/storage/emulated/0/", "").replace("/", "%2F");
        Uri parse = Uri.parse(uri);
        DocumentFile documentFile = DocumentFile.fromTreeUri(context, uri1);
        Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        intent1.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentFile.getUri());
        context.startActivityForResult(intent1, REQUEST_CODE_FOR_DIR);
    }

    public static void getFiles(final FragmentActivity activity,
                                final DocumentFile file, final FilterResultCallback<NormalFile> callback,
                                final String[] suffix, final boolean isRecursion) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Directory<NormalFile>> directories = new ArrayList<>();
                getAllFiles(file, suffix, isRecursion, directories);
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
     * @param documentFile 需要查询的文件目录
     * @param suffix       查询类型，比如mp3什么的
     */
    public static List<Directory<NormalFile>> getAllFiles(DocumentFile documentFile, String[] suffix,
                                                          boolean isRecursion, List<Directory<NormalFile>> directories) {
        Log.d("文件:", documentFile.getName());
        if (documentFile.isDirectory()) {
            for (DocumentFile _file : documentFile.listFiles()) {
                Log.d("子文件", _file.getName());
                if (_file.isDirectory() && isRecursion) {
                    getAllFiles(_file, suffix, isRecursion, directories);//递归调用
                } else {
                    for (String _type : suffix) {
                        if (_file.getName().endsWith(_type)) {
                            String _name = _file.getName();
                            String filePath = _file.getUri().getPath();//获取文件路径
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
                }
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
