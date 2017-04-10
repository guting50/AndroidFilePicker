package com.brilliant.androidfilepicker.audio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import com.brilliant.androidfilepicker.R;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.AudioPickActivity;
import com.vincent.filepicker.filter.entity.AudioFile;

import java.util.ArrayList;

/**
 * description:
 * Date: 2017/4/10 11:17
 * User: Administrator
 */
public class AudioActivity extends AppCompatActivity {

    private ImageButton minus;

    private ImageButton plus;

    private EditText selectNumText;

    private RadioGroup showCamera;

    private int maxSelectNum = 9;

    private Button selectPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        initView();
        registerListener();
    }

    public void initView() {
        minus = (ImageButton) findViewById(R.id.minus);
        plus = (ImageButton) findViewById(R.id.plus);
        selectNumText = (EditText) findViewById(R.id.select_num);

        showCamera = (RadioGroup) findViewById(R.id.show_camera);

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
                boolean isNeedRecorder = showCamera.getCheckedRadioButtonId() == R.id.camera_yes ? true : false;

                AudioPickActivity.start(AudioActivity.this, maxSelectNum, isNeedRecorder, AudioPickActivity.REQUEST_AUDIO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == AudioPickActivity.REQUEST_AUDIO) {
            ArrayList<AudioFile> audioFiles = data.getParcelableArrayListExtra(Constant.RESULT_PICK_VIDEO);
            // do something...
        }
    }
}
