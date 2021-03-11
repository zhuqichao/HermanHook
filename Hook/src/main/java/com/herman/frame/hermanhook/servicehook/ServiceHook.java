package com.herman.frame.hermanhook.servicehook;

import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ServiceHook implements InvocationHandler {
    private static final String TAG = "ServiceHook";

    private final IBinder mBase;
    private Class<?> mStub;
    private Class<?> mInterface;
    private final InvocationHandler mInvocationHandler;

    public ServiceHook(IBinder mBase, String iInterfaceName, boolean isStub, InvocationHandler InvocationHandler) {
        this.mBase = mBase; //传进来的原始服务，后面替换为自己创建的服务
        this.mInvocationHandler = InvocationHandler;

        try {
            this.mInterface = Class.forName(iInterfaceName);
            this.mStub = Class.forName(String.format("%s%s", iInterfaceName, isStub ? "$Stub" : ""));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.d(TAG, "invoke: ####" + method.getName());
        if ("queryLocalInterface".equals(method.getName())) {
            return Proxy.newProxyInstance(proxy.getClass().getClassLoader(), new Class[]{mInterface},
                    new HookHandler(mBase, mStub, mInvocationHandler));
        }

        Log.e(TAG, "ERROR!!!!! method:name = " + method.getName());
        // 其他方法调用原始服务
        return method.invoke(mBase, args);
    }

    private static class HookHandler implements InvocationHandler {
        private Object mBase;
        private final InvocationHandler mInvocationHandler;

        public HookHandler(IBinder base, Class<?> stubClass, InvocationHandler InvocationHandler) {
            mInvocationHandler = InvocationHandler;

            try {
                Method asInterface = stubClass.getDeclaredMethod("asInterface", IBinder.class);
                this.mBase = asInterface.invoke(null, base);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Log.d(TAG, "invoke: method name=" + method.getName());
            if (mInvocationHandler != null) {
                try {
                    return mInvocationHandler.invoke(mBase, method, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
            return method.invoke(mBase, args);
        }
    }
}
