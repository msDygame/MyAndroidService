package com.dygame.myandroidservice;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
/**
  * @Description: �M�ΤU��APK���Service�u����, �q������ܶi��,�U�������_�ʴ���,�æ۰ʥ��}�w�ˬɭ�(�t�XxUtils�ֳt�}�o�ج[)
 * �ݭn�K�[�v���G
 * <uses-permission android:name="android.permission.INTERNET" />
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.VIBRATE" /> //�_��
 * �ݭn�b<application></application>���ҤU���U�A��
 */
public class MainActivity extends ActionBarActivity
{
    protected MyService mBoundService;
    protected boolean mIsBound = false;
    protected final int NotificationID = 0x10000;
    protected NotificationManager mNotificationManager = null;
    protected NotificationCompat.Builder builder;
    MyReceiver pReceiver;
    protected static String TAG = "";
    // ���U�����|
    protected String APK_url = "";
    // ���O�s���|(�p�G��SD�d�N�O�sSD�d,�p�G�S��SD�d�N�O�s�����]�W�U�����|)
    protected String APK_dir = "";
    //
    protected ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            // Tell the user about this.
            Toast.makeText(MainActivity.this, "Service Disconnected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((MyService.LocalBinder) service).getService();
            // Tell the user about this.
            Toast.makeText(MainActivity.this, "Service Connected", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        MyCrashHandler pCrashHandler = MyCrashHandler.getInstance();
        pCrashHandler.init(getApplicationContext());
        TAG = MyCrashHandler.getTag();
        //�b���U�s������:
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.dygame.myandroidservice.broadcast");//��BroadcastReceiver���waction�A�Ϥ��Ω󱵦��Paction���s��
        pReceiver = new MyReceiver();
        registerReceiver(pReceiver, intentFilter);
        //
        Button pButton1 = (Button) this.findViewById(R.id.button1);
        Button pButton2 = (Button) this.findViewById(R.id.button2);
        Button pButton3 = (Button) this.findViewById(R.id.button3);
        Button pButton4 = (Button) this.findViewById(R.id.button4);
        //
        pButton1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                throw new NullPointerException();
            }
        });
        pButton2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                double i = 12 / 0;// �ߥXArithmeticException��RuntimeException�����`
            }
        });
        pButton3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                doBindService();
            }
        });
        pButton4.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                doUnbindService();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        doUnbindService();
        //���P
        unregisterReceiver(pReceiver);
    }

    void doBindService()
    {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(MainActivity.this, MyService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        // Tell the user about this.
        Toast.makeText(MainActivity.this, "Bind Service", Toast.LENGTH_SHORT).show();
    }

    void doUnbindService()
    {
        if (mIsBound)
        {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            // Tell the user about this.
            Toast.makeText(MainActivity.this, "Unbinding Service", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * �o��s��
     */
    protected void SendBroadcastIntent()
    {
        //create a intent with an action
        String sActionString = "com.dygame.nonuiandroidservice.broadcast";
        Intent broadcastIntent = new Intent(sActionString);
        broadcastIntent.putExtra(TAG, "Burning Love! Poi!");
        sendBroadcast(broadcastIntent);
    }

    /**
     * �����s��
     */
    public class MyReceiver extends BroadcastReceiver
    {
        protected boolean IsCommonTag = false;//it is a Tag , Log it and debug

        @Override
        public void onReceive(Context context, Intent intent)
        {
            String sAction = intent.getAction();
            if ((sAction.equals("android.intent.action.BOOT_COMPLETED")) || (sAction.equals("Hello poi")))
            {
                Log.i(TAG, "You've got mail");
            }
            // analyze broadcast by packagename
            if (sAction.equals("com.dygame.myandroidservice.broadcast"))
            {
                IsCommonTag = true;
            }
            if (sAction.equals("com.dygame.nonuiandroidservice.broadcast"))
            {
                IsCommonTag = true;
            }
            //
            if (IsCommonTag == true)
            {
                Bundle bundle = intent.getExtras();
                if (bundle != null)
                {
                    String sMessage = bundle.getString(TAG);
                    Log.i(TAG, "broadcast receiver action:" + sAction + "=" + sMessage);
                }
            }
        }
    }

    /**
     * @param x     ��e��
     * @param total �`��
     *              [url=home.php?mod=space&uid=7300]@return[/url] ��e�ʤ���
     * @Description:��^�ʤ����
     */
    private String getPercent(int x, int total)
    {
        String result = "";// �����ʤ��񪺭�
        double x_double = x * 1.0;
        double tempresult = x_double / total;
        // �ʤ���榡�A�᭱����2�쪺��0�ɻ� ##.00%
        DecimalFormat df1 = new DecimalFormat("0.00%");
        result = df1.format(tempresult);
        return result;
    }

    /**
     * �Ыظ��|���ɭԤ@�w�n��[/],����ϥ�[\],���O�Ыؤ�󧨥[��󪺮ɭԥi�H�ϥ�[\].
     * [/]�Ÿ��OLinux�t�θ��|���j��,��[\]�Owindows�t�θ��|���j�� Android���֬OLinux.
     */
    private void initAPKDir()
    {
        if (isHasSdcard())// �P�_�O�_���JSD�d
            APK_dir = getApplicationContext().getFilesDir().getAbsolutePath() + "/download/";// �O�s��app���]�W���|�U
        else
            APK_dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/";// �O�s��SD�d���|�U
        File destDir = new File(APK_dir);
        if (!destDir.exists())// �P�_��󧨬O�_�s�b
        {
            destDir.mkdirs();
        }
    }


    /**
     * @Description:�P�_�O�_���JSD�d
     */
    private boolean isHasSdcard()
    {
        String status = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (status.equals(Environment.MEDIA_MOUNTED))
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * @return
     * @Description:�����e���Ϊ��W��
     */
    private String getApplicationName()
    {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try
        {
            packageManager = getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e)
        {
            applicationInfo = null;
        }
        String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }

    /**
     * Ū���H�U�ƾ�:
     * Appliction ��Meta-data
     * Activity����Meta-data
     */
    public void getApplicationInfomation()
    {
        //activity MetaDataŪ��
        ActivityInfo pActivityInfo;
        try
        {
            pActivityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
            String msg = pActivityInfo.metaData.getString("tel");
            Log.i(TAG, "tel:" + msg);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //appliction MetaDataŪ��
        ApplicationInfo pAppInfo;
        try
        {
            pAppInfo = this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            String msg = pAppInfo.metaData.getString("CHANNEL");
            Log.i(TAG,"Channel:" + msg);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}