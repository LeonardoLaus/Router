package cherry.android.router.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cherry.android.router.api.intercept.IInterceptor;
import cherry.android.router.api.utils.Utils;

/**
 * Created by Administrator on 2017/5/25.
 */

public final class RouterManager {

    private static final String TAG = "RouterManager";
    private static final String PACKAGE_NAME = "cherry.android.router";

    private static RouterManager sInstance;

    private Map<String, RouteMeta> mRouterTable = new RouteMap();
    private Map<String, Class<? extends IInterceptor>> mIntercptorMap = new LinkedHashMap<>();
    private boolean mInitialized;
    private Context mContext;

    private RouteMeta mRouteMeta;

    static RouterManager instance() {
        if (sInstance == null)
            synchronized (RouterManager.class) {
                if (sInstance == null)
                    sInstance = new RouterManager();
            }
        return sInstance;
    }

    private RouterManager() {
    }

    synchronized void init(@NonNull Context context) {
        if (!mInitialized) {
            mContext = context;
            List<String> classNames = Utils.getFileNameByPackage(context, PACKAGE_NAME);
            for (String className : classNames) {
                if (className.endsWith("_RoutePicker")) {
                    pickRouteTable(className);
                } else if (className.endsWith("_InterceptorPicker")) {
                    pickInterceptor(className);
                }
            }
            mInitialized = true;
        }
    }

    void addRoutePicker(@NonNull IRoutePicker picker) {
        picker.pick(mRouterTable);
    }

    void addInterceptor(@NonNull InterceptorPicker picker) {
        picker.pick(mIntercptorMap);
    }

    RouterManager build(@NonNull String uri) {
        if (mRouteMeta != null) {
            if (!uri.equals(mRouteMeta.getUri())) {
                mRouteMeta = mRouterTable.get(uri);
                mRouteMeta.setUri(uri);
            }
        } else {
            mRouteMeta = mRouterTable.get(uri);
            mRouteMeta.setUri(uri);
        }
        return this;
    }

    public void open() {
        open(mContext);
    }

    public void open(Context context) {
        if (intercept(mRouteMeta)) {
            return;
        }
        Intent intent = new Intent(context, mRouteMeta.getDestination());
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtras(mRouteMeta.getQueryArgument());
        ContextCompat.startActivity(context, intent, null);
    }

    public boolean intercept(RouteMeta routeMeta) {
        Log.e(TAG, "intercept");
        return false;
    }

    private void pickRouteTable(@NonNull String className) {
        try {
            Class<?> cls = Class.forName(className);
            Constructor constructor = cls.getConstructor();
            IRoutePicker routePicker = (IRoutePicker) constructor.newInstance();
            addRoutePicker(routePicker);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFound", e);
            throw new IllegalStateException("Class Not Found", e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NoSuchMethodException", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException", e);
        } catch (InstantiationException e) {
            Log.e(TAG, "InstantiationException", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "InvocationTargetException", e);
        }
    }

    private void pickInterceptor(@NonNull String className) {
        try {
            Class<?> cls = Class.forName(className);
            Constructor constructor = cls.getConstructor();
            InterceptorPicker picker = (InterceptorPicker) constructor.newInstance();
            addInterceptor(picker);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFound", e);
            throw new IllegalStateException("Class Not Found", e);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NoSuchMethodException", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException", e);
        } catch (InstantiationException e) {
            Log.e(TAG, "InstantiationException", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "InvocationTargetException", e);
        }
    }
}
