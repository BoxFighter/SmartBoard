package com.example.smartboard.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.smartboard.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.smartboard.R.id.file_name;

public class HistoryActivity extends AppCompatActivity {

    String rootpath;
    ListView listView;
    ImageButton returnStu;
    // 记录当前的父文件夹
    File currentParent;
    // 记录当前路径下的所有文件的文件数组
    File[] currentFiles;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        initview();
    }
    private void initview(){
        returnStu=(ImageButton)findViewById(R.id.return_btn);
        returnStu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("StartActivity");
                intent.addCategory("android.intent.category.DEFAULT");
                startActivity(intent);
            }
        });
        // 获取列出全部文件的ListView
        listView = (ListView) findViewById(R.id.list);
        // 获取系统的SD卡的目录
        rootpath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/SmartboardHistory";
        File root = new File(rootpath);
        // 如果 SD卡存在
        if (root.exists())
        {
            currentParent = root;
            currentFiles = root.listFiles();
            // 使用当前目录下的全部文件、文件夹来填充ListView
            inflateListView(currentFiles);
        }
        // 为ListView的列表项的单击事件绑定监听器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                // 用户单击了文件，直接返回，不做任何处理
                if (currentFiles[position].isFile()) {
                }
                // 获取用户点击的文件夹下的所有文件
                File[] tmp = currentFiles[position].listFiles();
                if (tmp == null || tmp.length == 0)
                {
                    Toast.makeText(HistoryActivity.this
                            , "当前路径不可访问或该路径下没有文件",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    // 获取用户单击的列表项对应的文件夹，设为当前的父文件夹
                    currentParent = currentFiles[position]; // ②
                    // 保存当前的父文件夹内的全部文件和文件夹
                    currentFiles = tmp;
                    // 再次更新ListView
                    inflateListView(currentFiles);
                }
            }
        });
        // 获取上一级目录的按钮
        Button parent = (Button) findViewById(R.id.parent);
        parent.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View source)
            {
                try
                {
                    if (!currentParent.getCanonicalPath().equals(rootpath)) {
                        // 获取上一级目录
                        currentParent = currentParent.getParentFile();
                        // 列出当前目录下所有文件
                        currentFiles = currentParent.listFiles();
                        // 再次更新ListView
                        inflateListView(currentFiles);
                    }else {
                        File root = new File(rootpath);
                        currentParent = root;
                        currentFiles = root.listFiles();
                        // 使用当前目录下的全部文件、文件夹来填充ListView
                        inflateListView(currentFiles);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void inflateListView(File[] files) {
        List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < files.length; i++)
        {
            Map<String, Object> listItem = new HashMap<String, Object>();
            // 如果当前File是文件夹，使用folder图标；否则使用file图标
            if (files[i].isDirectory())
            {
                listItem.put("icon", R.drawable.folder);
            }
            else
            {
                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inSampleSize = 1;
                op.inJustDecodeBounds = false;
                Bitmap bitmap=BitmapFactory.decodeFile(files[i].getAbsolutePath(),op);
                listItem.put("icon", bitmap);
                /*
                final LinearLayout vi = (LinearLayout) getLayoutInflater().inflate(R.layout.line, null);
                ImageView icon=(ImageView)vi.findViewById(R.id.icon);
                icon.setAdjustViewBounds(true);
                LinearLayout.LayoutParams p= (LinearLayout.LayoutParams) icon.getLayoutParams();
                p.height=100;
                p.width=150;
                icon.setLayoutParams(p);*/
            }
            listItem.put("fileName", files[i].getName());
            listItems.add(listItem);
        }
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItems, R.layout.line
                , new String[]{ "icon", "fileName" }, new int[]{R.id.icon, file_name });
        listView.setAdapter(simpleAdapter);
        simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            public boolean setViewValue(View arg0, Object arg1, String textRepresentation) {
                if ((arg0 instanceof ImageView) & (arg1 instanceof Bitmap)) {
                    ImageView imageView = (ImageView) arg0;
                    Bitmap bitmap = (Bitmap) arg1;
                    imageView.setImageBitmap(bitmap);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }
    public boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }
}
