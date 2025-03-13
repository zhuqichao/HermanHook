package com.herman.frame.hermanhook;

import android.content.Context;
import android.os.IBinder;
import android.util.Log;

import com.herman.frame.hermanhook.servicehook.ServiceHook;
import com.herman.frame.hermanhook.servicehook.ServiceManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Herman on 2020/6/20.
 */
public class Hooks {

    private static final String TAG = "Hooks";

    public static void hookService(String serviceName, String interfaceName, InvocationHandler invocationHandler) {
        hookService(serviceName, interfaceName, true, invocationHandler);
    }

    public static void hookService(String serviceName, String interfaceName, boolean isStub, InvocationHandler invocationHandler) {
        IBinder service = ServiceManager.getService(serviceName);
        if (service != null) {
            IBinder hookService = (IBinder) Proxy.newProxyInstance(IBinder.class.getClassLoader(),
                    new Class[]{IBinder.class},
                    new ServiceHook(service, interfaceName, isStub, invocationHandler));
            ServiceManager.setService(serviceName, hookService);
        } else {
            Log.e(TAG, serviceName + " hook failed!");
        }
    }

    public static void hookToast(final ToastHook toastHook) {
        Hooks.hookService(Context.NOTIFICATION_SERVICE, "android.app.INotificationManager",
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("enqueueTextToast".equals(method.getName())) {
                            try {
                                if (args[2] instanceof CharSequence) {
                                    StringBuilder newText = new StringBuilder((CharSequence) args[2]);
                                    if (!toastHook.onShow(newText)) {
                                        return Boolean.FALSE;
                                    } else {
                                        args[2] = newText.toString();
                                    }
                                    return method.invoke(proxy, args);
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        return method.invoke(proxy, args);
                    }
                });
    }

    public interface ToastHook {
        boolean onShow(StringBuilder text);
    }

}
