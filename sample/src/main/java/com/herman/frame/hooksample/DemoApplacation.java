package com.herman.frame.hooksample;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.herman.frame.hermanhook.Hooks;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by Herman on 2020/6/19.
 */
public class DemoApplacation extends Application {

    private static final String TAG = "DemoApplacation";

    private static DemoApplacation mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static DemoApplacation getInstance() {
        return mInstance;
    }

    public static Context getContext() {
        return mInstance.getBaseContext();
    }

    @Override
    protected void attachBaseContext(Context base) {//早于onCreate执行
        Log.d(TAG, "attachBaseContext: ");
        super.attachBaseContext(base);
//        Hooks.hookService(Context.WIFI_SERVICE, "android.net.wifi.IWifiManager", mInvocationHandler);
        Hooks.hookService(Context.NOTIFICATION_SERVICE, "android.app.INotificationManager", mInvocationHandler);

        Hooks.hookToast(new Hooks.ToastHook() {
            @Override
            public boolean onShow(StringBuilder text) {
                text.insert(0, "温馨提示：");
                return true;
            }
        });

    }

    private final InvocationHandler mInvocationHandler = new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Log.d(TAG, "invoke: methodName=" + methodName + ", args=" + Arrays.toString(args) + ", proxy=" + proxy);
            return method.invoke(proxy, args);
        }
    };

    private String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo appProcess :
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
}
