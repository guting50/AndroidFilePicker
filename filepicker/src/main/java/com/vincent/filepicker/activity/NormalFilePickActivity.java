package com.vincent.filepicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.vincent.filepicker.Constant;
import com.vincent.filepicker.DividerListItemDecoration;
import com.vincent.filepicker.FileUriUtils;
import com.vincent.filepicker.R;
import com.vincent.filepicker.adapter.NormalFilePickAdapter;
import com.vincent.filepicker.adapter.OnSelectStateListener;
import com.vincent.filepicker.filter.FileFilter;
import com.vincent.filepicker.filter.callback.FilterResultCallback;
import com.vincent.filepicker.filter.entity.Directory;
import com.vincent.filepicker.filter.entity.NormalFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vincent Woo
 * Date: 2016/10/26
 * Time: 10:14
 */

public class NormalFilePickActivity extends BaseActivity {

    //=== request code
    public final static int REQUEST_FILE = 100;

    public static final int DEFAULT_MAX_NUMBER = 9;
    public static final String SUFFIX = "Suffix";
    private int mMaxNumber;
    private int mCurrentNumber = 0;
    private Toolbar mTbImagePick;
    private RecyclerView mRecyclerView;
    private NormalFilePickAdapter mAdapter;
    private ArrayList<NormalFile> mSelectedList = new ArrayList<>();
    private ProgressBar mProgressBar;
    private String[] mSuffix, mPaths;
    public static boolean isRecursion = true;

    public final static String EXTRA_MAX_SELECT_NUM = "MaxSelectNum";

    public final static String EXTRA_STUFFIX = "mSuffix";

    public final static String EXTRA_PATHS = "PATHS";

    @Override
    void permissionGranted() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    loadData1();
                } else {
                    loadData();
                }
            }
        }, 1000);
    }

    /**
     * 启动文件选择
     *
     * @param activity
     * @param maxSelectNum 最大选择数量
     * @param mSuffix      文件格式集合
     * @param requestCode  请求码
     */
    public static void start(Activity activity, int maxSelectNum, String[] mSuffix, int requestCode) {
        Intent intent = new Intent(activity, NormalFilePickActivity.class);
        intent.putExtra(EXTRA_MAX_SELECT_NUM, maxSelectNum);
        intent.putExtra(EXTRA_STUFFIX, mSuffix);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void start(Activity activity, int maxSelectNum, String[] paths, String[] mSuffix, int requestCode) {
        Intent intent = new Intent(activity, NormalFilePickActivity.class);
        intent.putExtra(EXTRA_MAX_SELECT_NUM, maxSelectNum);
        intent.putExtra(EXTRA_STUFFIX, mSuffix);
        intent.putExtra(EXTRA_PATHS, paths);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        setContentView(R.layout.activity_file_pick);

        mMaxNumber = getIntent().getIntExtra(EXTRA_MAX_SELECT_NUM, DEFAULT_MAX_NUMBER);
        mSuffix = getIntent().getStringArrayExtra(EXTRA_STUFFIX);
        mPaths = getIntent().getStringArrayExtra(EXTRA_PATHS);
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initView();

    }

    private void initView() {
        mTbImagePick = (Toolbar) findViewById(R.id.tb_file_pick);
        mTbImagePick.setTitle(mCurrentNumber + "/" + mMaxNumber);
        setSupportActionBar(mTbImagePick);
        mTbImagePick.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_file_pick);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerListItemDecoration(this,
                LinearLayoutManager.VERTICAL, R.drawable.divider_rv_file));
        mAdapter = new NormalFilePickAdapter(this, mMaxNumber);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnSelectStateListener(new OnSelectStateListener<NormalFile>() {
            @Override
            public void OnSelectStateChanged(boolean state, NormalFile file) {
                if (state) {
                    mSelectedList.add(file);
                    mCurrentNumber++;
                } else {
                    mSelectedList.remove(file);
                    mCurrentNumber--;
                }
                mTbImagePick.setTitle(mCurrentNumber + "/" + mMaxNumber);
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.pb_file_pick);
    }

    private void loadData() {
        FilterResultCallback callback = new FilterResultCallback<NormalFile>() {
            @Override
            public void onResult(List<Directory<NormalFile>> directories) {
                mProgressBar.setVisibility(View.GONE);
                List<NormalFile> list = new ArrayList<>();
                for (Directory<NormalFile> directory : directories) {
                    list.addAll(directory.getFiles());
                }
                for (NormalFile file : mSelectedList) {
                    int index = list.indexOf(file);
                    if (index != -1) {
                        list.get(index).setSelected(true);
                    }
                }

                mAdapter.refresh(list);
            }
        };
        if (mPaths != null && mPaths.length > 0) {
            FileFilter.getFiles(this, mPaths, callback, mSuffix);
        } else {
            FileFilter.getFiles(this, callback, mSuffix);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_pick, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(Constant.RESULT_PICK_FILE, mSelectedList);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void loadData1() {
        FileUriUtils.startForRoot(this, 222);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 222) {
            DocumentFile documentFile = DocumentFile.fromTreeUri(this, data.getData());
            FilterResultCallback callback = new FilterResultCallback<NormalFile>() {
                @Override
                public void onResult(List<Directory<NormalFile>> directories) {
                    mProgressBar.setVisibility(View.GONE);
                    List<NormalFile> list = new ArrayList<>();
                    for (Directory<NormalFile> directory : directories) {
                        list.addAll(directory.getFiles());
                    }
                    for (NormalFile file : mSelectedList) {
                        int index = list.indexOf(file);
                        if (index != -1) {
                            list.get(index).setSelected(true);
                        }
                    }

                    mAdapter.refresh(list);
                }
            };

            FileUriUtils.getFiles(this, documentFile, callback, mSuffix, isRecursion);
        }
    }
}
