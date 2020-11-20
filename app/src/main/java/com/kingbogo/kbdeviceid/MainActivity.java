package com.kingbogo.kbdeviceid;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kingbogo.getdeviceid.AndroidDeviceId;
import com.kingbogo.getdeviceid.util.KbLogUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTipsTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void initView() {
        mTipsTv = findViewById(R.id.main_tips_tv);
        findViewById(R.id.main_get_unque_code_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.main_get_unque_code_btn) {
            AndroidDeviceId.getDeviceId(getApplicationContext(), deviceId -> {
                KbLogUtil.i("deviceId => " + deviceId);
                mTipsTv.setText(deviceId);
            });
        }
    }

}