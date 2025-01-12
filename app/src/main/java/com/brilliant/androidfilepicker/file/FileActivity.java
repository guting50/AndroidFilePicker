package com.brilliant.androidfilepicker.file;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.brilliant.androidfilepicker.R;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.NormalFilePickActivity;
import com.vincent.filepicker.filter.entity.NormalFile;

import java.util.ArrayList;

/**
 * description:
 * Date: 2017/4/10 11:09
 * User: Administrator
 */
public class FileActivity extends AppCompatActivity {

    private ImageButton minus;

    private ImageButton plus;

    private EditText selectNumText;

    private int maxSelectNum = 9;

    private Button selectPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        initView();
        registerListener();
    }

    public void initView() {
        minus = (ImageButton) findViewById(R.id.minus);
        plus = (ImageButton) findViewById(R.id.plus);
        selectNumText = (EditText) findViewById(R.id.select_num);

        selectPicture = (Button) findViewById(R.id.video_selector);
    }

    public void registerListener() {
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maxSelectNum--;
                selectNumText.setText(maxSelectNum + "");
            }
        });
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maxSelectNum++;
                selectNumText.setText(maxSelectNum + "");
            }
        });
        selectPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] mSuffix = {"doc", "docx", "xls", "xlsx", "ppt", "pptx", "pdf"};
                EditText editText = (EditText) findViewById(R.id.path_et);
                String path = editText.getText().toString();
                if (!TextUtils.isEmpty(path)) {
                    String[] paths = {path};
                    NormalFilePickActivity.start(FileActivity.this, maxSelectNum, paths, mSuffix, NormalFilePickActivity.REQUEST_FILE);
                    return;
                }
                NormalFilePickActivity.start(FileActivity.this, maxSelectNum, mSuffix, NormalFilePickActivity.REQUEST_FILE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == NormalFilePickActivity.REQUEST_FILE) {
            ArrayList<NormalFile> normalFiles = data.getParcelableArrayListExtra(Constant.RESULT_PICK_FILE);
            // do something...
        }
    }
}
