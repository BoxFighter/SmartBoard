package com.example.smartboard.Activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.smartboard.Adapter.MyPagerAdapter;
import com.example.smartboard.R;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends Activity implements View.OnClickListener {
    private List<View> viewList;
    private ViewPager pager;
    private ImageButton down_meeting;
    private ImageButton down_my;
    private ImageButton launch_meeting;
    private ImageButton join_meeting;
    private TextView tv_nickname;
    private TextView tv_personalized_signature;
    private TextView tv_history;
    private TextView tv_about_we;
    String meeting_name_string;
    String meeting_password_string;
    String meeting_num_string;
    MyPagerAdapter adapter;
    SharedPreferences tepreferences;
    private SharedPreferences.Editor editor;
    String nickname;
    String signature;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_start);

        initView();
        initViewPage();
        initEvent();
    }

    private void initView() {
        viewList = new ArrayList<View>();
        pager = (ViewPager) this.findViewById(R.id.pager);
        down_meeting = (ImageButton) findViewById(R.id.view_meeting);
        down_my = (ImageButton) findViewById(R.id.view_mydata);
        tepreferences = getSharedPreferences("MyInfo", MODE_PRIVATE);
        editor = tepreferences.edit();
        nickname= tepreferences.getString("NickName","");
        signature= tepreferences.getString("PersonalizedSignature","");
    }

    private void initViewPage() {
        LayoutInflater mLayoutInflater = LayoutInflater.from(StartActivity.this);
        View view1 = View.inflate(StartActivity.this, R.layout.view_main1, null);
        View view2 = View.inflate(StartActivity.this, R.layout.view_main2, null);
        viewList.add(view1);
        viewList.add(view2);
        adapter = new MyPagerAdapter(viewList);
        pager.setAdapter(adapter);
        launch_meeting = (ImageButton) view1.findViewById(R.id.launch_meeting);
        join_meeting = (ImageButton) view1.findViewById(R.id.join_meeting);
        tv_nickname=(TextView)view2.findViewById(R.id.tv_nickname);
        tv_personalized_signature=(TextView)view2.findViewById(R.id.tv_personalized_signature);
        tv_history=(TextView)view2.findViewById(R.id.tv_history);
        tv_about_we=(TextView)view2.findViewById(R.id.tv_about_we);
        launch_meeting.setOnClickListener(this);
        join_meeting.setOnClickListener(this);
        tv_nickname.setOnClickListener(this);
        tv_personalized_signature.setOnClickListener(this);
        tv_history.setOnClickListener(this);
        tv_about_we.setOnClickListener(this);
        if(!TextUtils.isEmpty(nickname)){
            tv_nickname.setText(nickname);
        }
        if(!TextUtils.isEmpty(signature)){
            tv_personalized_signature.setText(signature);
        }
    }

    private void initEvent() {
        down_meeting.setOnClickListener(this);
        down_my.setOnClickListener(this);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageSelected(int arg0) {
                int currentItem = pager.getCurrentItem();
                switch (currentItem) {
                    case 0:
                        down_meeting.setImageResource(R.drawable.down_meeting_pressed);
                        down_my.setImageResource(R.drawable.down_my);
                        break;
                    case 1:
                        down_my.setImageResource(R.drawable.down_my_pressed);
                        down_meeting.setImageResource(R.drawable.down_meeting);
                        break;
                    default:
                        break;
                }
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.launch_meeting:
                Launch();
                break;
            case R.id.join_meeting:
                Intent in=new Intent(this,JoinMeetingActivity.class);
                startActivity(in);
                break;
            case R.id.view_meeting:
                pager.setCurrentItem(0);
                down_meeting.setImageResource(R.drawable.down_meeting_pressed);
                down_my.setImageResource(R.drawable.down_my);
                break;
            case R.id.view_mydata:
                pager.setCurrentItem(1);
                down_my.setImageResource(R.drawable.down_my_pressed);
                down_meeting.setImageResource(R.drawable.down_meeting);
                break;
            case R.id.tv_nickname:
                onBackPressed(arg0);
                break;
            case R.id.tv_personalized_signature:
                onBackPressed(arg0);
                break;
            case R.id.tv_history:
                Intent intent=new Intent(this,HistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_about_we:
                final LinearLayout vi = (LinearLayout) getLayoutInflater().inflate(R.layout.view_about_we, null);
                new android.app.AlertDialog.Builder(StartActivity.this)
                        .setIcon(R.mipmap.logo)
                        .setTitle("关于我们")
                        .setView(vi)
                        .setPositiveButton("赞一个", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create()
                        .show();
                break;
            default:
                break;
        }
    }

    public void Launch() {
        final LinearLayout vi = (LinearLayout) getLayoutInflater().inflate(R.layout.view_start_meeting, null);
        new AlertDialog.Builder(StartActivity.this)
                .setTitle("创建会议")
                .setView(vi)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText meeting_name = (EditText) vi.findViewById(R.id.meeting_name);
                        EditText meeting_password = (EditText) vi.findViewById(R.id.meeting_password);
                        EditText meeting_num = (EditText) vi.findViewById(R.id.meeting_num);
                        meeting_name_string = meeting_name.getText().toString();
                        meeting_password_string = meeting_password.getText().toString();
                        meeting_num_string=meeting_num.getText().toString();
                        if ((!TextUtils.isEmpty(meeting_name_string)) &&(!TextUtils.isEmpty(meeting_password_string ))
                                &&(!TextUtils.isEmpty(meeting_num_string))) {
                            Intent intent = new Intent();
                            intent.putExtra("UserNum",meeting_num_string);
                            intent.putExtra("MeetingName",meeting_name_string);
                            intent.putExtra("Password",meeting_password_string);
                            intent.setAction("SmartpadServer");
                            intent.addCategory("android.intent.category.DEFAULT");
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }
    public void onBackPressed(final View v) {
        final LinearLayout vi = (LinearLayout) getLayoutInflater().inflate(R.layout.view_info, null);
        new android.app.AlertDialog.Builder(StartActivity.this)
                .setIcon(R.mipmap.logo)
                .setTitle("修改信息")
                .setView(vi)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText modify = (EditText) vi.findViewById(R.id.student_name_rename);
                        switch (v.getId()) {
                            case R.id.tv_nickname:
                                if(!TextUtils.isEmpty(modify.getText().toString())){
                                    editor.putString("NickName",modify.getText().toString());
                                    editor.commit();
                                    tv_nickname.setText(modify.getText().toString());
                                }
                                break;
                            case R.id.tv_personalized_signature:
                                if(!TextUtils.isEmpty(modify.getText().toString())){
                                    editor.putString("PersonalizedSignature",modify.getText().toString());
                                    editor.commit();
                                    tv_personalized_signature.setText(modify.getText().toString());
                                }
                                break;
                            default:break;
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }
}
