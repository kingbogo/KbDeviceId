package com.kingbogo.getdeviceid.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * <p>
 * </p>
 *
 * @author Kingbo
 * @date 2020/11/19
 */
public final class KbSpUtil {

    public static void setSP(Context context, String key, Object object) {
        String type = object.getClass().getSimpleName();
        String packageName = context.getPackageName();
        SharedPreferences sp = context.getSharedPreferences(packageName, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        switch (type) {
            case "String":
                edit.putString(key, (String) object);
                break;
            case "Integer":
                edit.putInt(key, (Integer) object);
                break;
            case "Boolean":
                edit.putBoolean(key, (Boolean) object);
                break;
            case "Float":
                edit.putFloat(key, (Float) object);
                break;
            case "Long":
                edit.putLong(key, (Long) object);
                break;
        }
        edit.apply();
    }

    public static Object getSp(Context context, String key, Object defaultObject) {
        String type = defaultObject.getClass().getSimpleName();
        String packageName = context.getPackageName();
        SharedPreferences sp = context.getSharedPreferences(packageName, Context.MODE_PRIVATE);
        switch (type) {
            case "String":
                return sp.getString(key, (String) defaultObject);
            case "Integer":
                return sp.getInt(key, (Integer) defaultObject);
            case "Boolean":
                return sp.getBoolean(key, (Boolean) defaultObject);
            case "Float":
                return sp.getFloat(key, (Float) defaultObject);
            case "Long":
                return sp.getLong(key, (Long) defaultObject);
        }
        return null;
    }

    public static void cleanAllSP(Context context) {
        String packageName = context.getPackageName();
        SharedPreferences sp = context.getSharedPreferences(packageName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }

}
