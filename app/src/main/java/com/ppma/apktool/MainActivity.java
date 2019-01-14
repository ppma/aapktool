package com.ppma.apktool;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ppma.utils.QQUtile;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import brut.apktool.Main;
import brut.common.BrutException;


public class MainActivity extends Activity {

    PrintStream printStream, oldStream = System.out;
    TerminalInputStream terminalInputStream = new TerminalInputStream();
    ScrollView scrollView;
    private MyRecesiver cmdRecesiver;

    public class MyRecesiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cmd = intent.getStringExtra("apktool");
            Toast.makeText(MainActivity.this, cmd, Toast.LENGTH_SHORT).show();
            runCmd(cmd);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scrollView = findViewById(R.id.scrollView);
        editText = findViewById(R.id.code);
        textView = findViewById(R.id.infoOutput);
        button = findViewById(R.id.run);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runCmd(editText.getText().toString());
            }
        });
        // 先判断是否有权限。
        if (AndPermission.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // 有权限，直接do anything.
            //getFile(rootPath);
            button.setClickable(true);


        } else {
            //申请权限。
            AndPermission.with(this)
                    .requestCode(PERMISSION_CODE_WRITE_EXTERNAL_STORAGE)
                    .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .send();
        }


        final TextPrintStream textPrintStream;
        textPrintStream = new TextPrintStream(textView);
        printStream = new PrintStream(textPrintStream);
        System.setOut(printStream);
        System.setErr(printStream);
        System.setIn(terminalInputStream);

        cmdRecesiver = new MyRecesiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("apktool");
        registerReceiver(cmdRecesiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(cmdRecesiver);
        System.setOut(oldStream);
        System.setErr(oldStream);
        printStream.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 只需要调用这一句，其它的交给AndPermission吧，最后一个参数是PermissionListener。
        AndPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, listener);
    }

    private PermissionListener listener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, List<String> grantedPermissions) {
            // 权限申请成功回调。
            if (requestCode == PERMISSION_CODE_WRITE_EXTERNAL_STORAGE) {
                //getFile(rootPath);
                button.setClickable(true);
            }
        }

        @Override
        public void onFailed(int requestCode, List<String> deniedPermissions) {
            // 权限申请失败回调。
            AndPermission.defaultSettingDialog(MainActivity.this, PERMISSION_CODE_WRITE_EXTERNAL_STORAGE)
                    .setTitle("权限申请失败")
                    .setMessage("我们需要的一些权限被您拒绝或者系统发生错误申请失败，请您到设置页面手动授权，否则功能无法正常使用！")
                    .setPositiveButton("好，去设置")
                    .show();
        }
    };

    private int PERMISSION_CODE_WRITE_EXTERNAL_STORAGE = 100;


    private void runCmd(final String str) {
        button.setClickable(false);
        button.setText("apktool运行中(●—●)");
        new Thread() {
            @Override
            public void run() {
                String[] strs = str.split("\\s+");
                String[] strs1 = new String[]{"d", "-f", Environment.getExternalStorageDirectory().getAbsolutePath() + "/app.apk", "-o", Environment.getExternalStorageDirectory().getAbsolutePath() + "/app-src"};
                try {

                    Main.main(strs);
                    join();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrutException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button.setText("运行结束╯▂╰");
                        button.setClickable(true);
                    }
                });
                try {
                    sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Apktool: " + str + "\n", Toast.LENGTH_SHORT).show();
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        }.start();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, 0, 0, "清除信息");
        menu.add(Menu.NONE, 1, 0, "加入QQ群");
        menu.add(Menu.NONE, 2, 0, "退出软件");
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        super.onMenuItemSelected(featureId, item);
        switch (item.getItemId()) {
            case 0:
                textView.setText(null);
                break;
            case 1:
                QQUtile.addGroup(this, "344306161");
                break;
            case 2:
                finish();
                break;

        }
        return true;
    }

    private SharedPreferences sharedPreferences;
    private TextView textView;
    private EditText editText;
    private Button button;


    class TextPrintStream extends ByteArrayOutputStream {
        TextView textView;

        public TextPrintStream(TextView textView) {
            this.textView = textView;
        }

        public void freshTextView() {
            final String text = new String(this.toByteArray());

            textView.post(new Runnable() {
                @Override
                public void run() {
                    textView.setText(text);
                }
            });
        }

        @Override
        public synchronized void write(byte[] buffer, int offset, int len) {
            super.write(buffer, offset, len);
            freshTextView();
        }

        @Override
        public void write(@NonNull byte[] buffer) throws IOException {
            super.write(buffer);
            freshTextView();
        }

        @Override
        public synchronized void write(int oneByte) {
            super.write(oneByte);
            freshTextView();
        }
    }

    class TerminalInputStream extends InputStream {
        final Queue<Integer> tempRead = new LinkedList<>();
        CharBuffer charBuffer = CharBuffer.allocate(1);
        ByteBuffer byteBuffer = ByteBuffer.allocate(5);
        CharsetEncoder charsetEncoder = Charset.defaultCharset().newEncoder();

        public void putToRead(String str) {
            synchronized (tempRead) {
                for (int i = 0; i < str.length(); i++) {
                    charBuffer.rewind();
                    charBuffer.append(str.charAt(i));
                    charBuffer.rewind();
                    byteBuffer.rewind();
                    charsetEncoder.encode(charBuffer, byteBuffer, false);
                    int pos = byteBuffer.position();
                    byteBuffer.rewind();
                    for (int j = 0; j < pos; j++)
                        tempRead.add((int) byteBuffer.get());
                }
                tempRead.notifyAll();
            }
        }

        boolean close = false;

        @Override
        public int read() {
            if (close) return -1;
            synchronized (tempRead) {
                try {
                    if (tempRead.size() == 0) tempRead.wait();
                } catch (InterruptedException e) {
                }
            }
            return tempRead.poll();
        }

        @Override
        public int read(byte[] b, int off, int len) {
            if (close) return -1;
            synchronized (tempRead) {
                try {
                    if (tempRead.size() == 0) tempRead.wait();
                } catch (InterruptedException e) {
                }
            }
            int i = 0;
            while (!tempRead.isEmpty() && i < len) {
                b[off + i] = (byte) tempRead.poll().intValue();
                i++;
            }
            return i;
        }

        @Override
        public void close() {
            close = true;
        }

    }
}
