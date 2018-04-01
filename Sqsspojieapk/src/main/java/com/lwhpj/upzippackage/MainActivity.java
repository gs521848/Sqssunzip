package com.lwhpj.upzippackage;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lwhpj.upzippackage.Util.UnzipFromAssets;
import com.unistrong.yang.zb_permission.ZbPermission;
import com.unistrong.yang.zb_permission.ZbPermissionFail;
import com.unistrong.yang.zb_permission.ZbPermissionSuccess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {


    String SDcardpath = Environment.getExternalStorageDirectory().toString();

    ProgressBar progressBar;
    public int maxprogress;
    public int progress=0;
    private final int REQUEST_STORAGE = 100;
    Button btnunzip;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requirPermission();
        progressBar=findViewById(R.id.progressBar);

        progressBar.setMax(100);


       btnunzip=findViewById(R.id.btn_unzip);
        btnunzip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnunzip.setText("解压ing");

                Toast.makeText(MainActivity.this,"开始解压啦！",Toast.LENGTH_SHORT).show();
                fakeProgress();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        unzip();
                    }
                },5000);







            }
        });

    }

    private void install() {

        if(copyApkFromAssets(this, "sqss.apk", Environment.getExternalStorageDirectory().getAbsolutePath()+"/sqss.apk")){
            AlertDialog.Builder m = new AlertDialog.Builder(this)
                    .setIcon(R.mipmap.ic_launcher).setMessage("是否开始安装游戏？")
                    .setPositiveButton("安装", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= 24)
                            {
                                File file=new File("file://" + Environment.getExternalStorageDirectory().getAbsolutePath()+"/sqss.apk");
                                Uri apkUri = FileProvider.getUriForFile(MainActivity.this, "com.lwhpj.upzippackage", file);
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath()+"/sqss.apk"),
                                        "application/vnd.android.package-archive");
                                startActivity(intent);
                            }else
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath()+"/sqss.apk"),
                                            "application/vnd.android.package-archive");
                                    startActivity(intent);
                                }

                        }
                    });
            m.show();
        }

    }

    public boolean copyApkFromAssets(Context context, String fileName, String path) {
        boolean copyIsFinish = false;
        try {
            InputStream is = context.getAssets().open(fileName);
            File file = new File(path);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();
            copyIsFinish = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return copyIsFinish;
    }


    public void fakeProgress()
    {
        new Thread()
        {
            @Override
            public void run() {
                while (progress<8)
                {

                    try {
                        progress=progress+1;
                        progressBar.incrementProgressBy(10);
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                super.run();
            }
        }.start();

    }



    public  void unzip() {
        try {
            unZip(MainActivity.this, "sqss.zip", SDcardpath, true);

        } catch (IOException e) {
            Toast.makeText(this, "解压失败："+e, Toast.LENGTH_SHORT).show();
            btnunzip.setText("解压失败");

        }

    }

    public void unZip(Context context, String assetName, String outputDirectory, boolean isReWrite) throws IOException {
        // 创建解压目标目录
        File file = new File(outputDirectory);
        // 如果目标目录不存在，则创建
        if (!file.exists()) {
            file.mkdirs();
        }
        // 打开压缩文件
        InputStream inputStream = context.getAssets().open(assetName);
        maxprogress = inputStream.available();
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        Toast.makeText(MainActivity.this,"总大小为："+maxprogress,Toast.LENGTH_SHORT);
        // 读取一个进入点
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        // 使用1Mbuffer
        byte[] buffer = new byte[1024*1024];
        // 解压时字节计数
        int count = 0;
        int allcount=0;
        // 如果进入点为空说明已经遍历完所有压缩包中文件和目录
        while (zipEntry != null) {
            // 如果是一个目录
            if (zipEntry.isDirectory()) {
                file = new File(outputDirectory + File.separator + zipEntry.getName());
                // 文件需要覆盖或者是文件不存在
                if (isReWrite || !file.exists()) {
                    file.mkdir();
                }
            } else {
                // 如果是文件
                file = new File(outputDirectory + File.separator + zipEntry.getName());
                // 文件需要覆盖或者文件不存在，则解压文件


                if (isReWrite || !file.exists()) {
                    file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    while ((count = zipInputStream.read(buffer)) > 0) {

                        fileOutputStream.write(buffer, 0, count);


                    }
                    fileOutputStream.close();
                }
            }


            // 定位到下一个文件入口
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();
        Toast.makeText(this,"解压成功，开始安装游戏", Toast.LENGTH_SHORT).show();
        btnunzip.setText("解压完成");
        progressBar.setProgress(100);
        if (Build.VERSION.SDK_INT >= 26) {
            boolean b = getPackageManager().canRequestPackageInstalls();
            if (b) {
                install();//安装应用的逻辑(写自己的就可以)
            } else {
                //请求安装未知应用来源的权限
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, 10010);
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                install();

            }
        },1000);
    }
    public void requirPermission()
    {
        ZbPermission.needPermission(MainActivity.this, REQUEST_STORAGE, com.unistrong.yang.zb_permission.Permission.STORAGE);

    }
    @ZbPermissionSuccess(requestCode = REQUEST_STORAGE)
    public void permissionSuccess() {
        Toast.makeText(MainActivity.this, "成功授予读写权限注解" , Toast.LENGTH_SHORT).show();
    }

    @ZbPermissionFail(requestCode = REQUEST_STORAGE)
    public void permissionFail() {
        Toast.makeText(MainActivity.this, "授予读写权限失败注解" , Toast.LENGTH_SHORT).show();
    }



}
