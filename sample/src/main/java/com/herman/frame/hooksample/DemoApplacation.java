package com.herman.frame.hooksample;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.herman.frame.hermanhook.Hooks;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by Herman on 2020/6/19.
 */
public class DemoApplacation extends Application {

    private static final String TAG = "DemoApplacation";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {//早于onCreate执行
        Log.d(TAG, "attachBaseContext: ");
        super.attachBaseContext(base);
        Hooks.hookService(Context.WIFI_SERVICE, "android.net.wifi.IWifiManager", mInvocationHandler);
        Hooks.hookService(Context.NOTIFICATION_SERVICE, "android.app.INotificationManager", mInvocationHandler);
        Hooks.hookToast(new Hooks.ToastHook() {
            @Override
            public boolean onShow(TextView view) {
                view.setText(view.getText() + ": HHHHHH");
                return true;
            }
        });
    }

    private InvocationHandler mInvocationHandler = new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Log.d(TAG, "invoke: methodName=" + methodName + ", args=" + Arrays.toString(args) + ", proxy=" + proxy);
            return method.invoke(proxy, args);
        }
    };
}
