package com.kingbogo.getdeviceid.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * </p>
 *
 * @author Kingbo
 * @date 2020/11/19
 */
public final class KbAndroidUtil {

    private static final String MARSHMALLOW_MAC_ADDRESS = "02:00:00:00:00:00";

    /**
     * 获取唯一码
     */
    public static String getUniqueIdCode(Context context) {
        String imei = getImei(context);
        String androidId = getAndroidId(context);
        String serial = getSerial();
        String macAddress = getMacAddress(context);
        KbLogUtil.w("_getUniqueIdCode(), imei: " + imei + ", androidId: " + androidId
                + ", serial: " + serial + ", macAddress: " + macAddress);

        StringBuilder uniqueIdSb = new StringBuilder();
        // imei
        if (!KbCheckUtil.isEmpty(imei)) {
            uniqueIdSb.append(imei);
        }
        // androidId
        if (!KbCheckUtil.isEmpty(androidId)) {
            uniqueIdSb.append(androidId);
        }
        // serial
        if (!KbCheckUtil.isEmpty(serial)) {
            uniqueIdSb.append(serial);
        }
        // macAddress
        if (!KbCheckUtil.isEmpty(macAddress)) {
            uniqueIdSb.append(macAddress);
        }

        // 唯一标识
        String unqueId = uniqueIdSb.toString();
        if (!KbCheckUtil.isEmpty(unqueId)) {
            String md5Str = md5(unqueId);
            if (!KbCheckUtil.isEmpty(md5Str)) {
                KbLogUtil.i("_getUniqueIdCode, md5Str: " + md5Str);
                return md5Str;
            }
        }
        // 兜底逻辑
        String uuid = getUuid();
        KbLogUtil.d("_getUniqueIdCode(), 所有标识都为空，则生成UUID: " + uuid);
        return uuid;
    }

    // ------------------------------------------------- @ private

    /**
     * 检测权限
     *
     * @param context    Context
     * @param permission 权限名称
     * @return true:已允许该权限; false:没有允许该权限
     */
    private static boolean checkHasPermission(Context context, String permission) {
        try {
            Class<?> contextCompat = null;
            try {
                contextCompat = Class.forName("androidx.core.content.ContextCompat");
            } catch (Exception e) {
                //ignored
            }

            if (contextCompat == null) {
                try {
                    contextCompat = Class.forName("androidx.core.content.ContextCompat");
                } catch (Exception e) {
                    //ignored
                }
            }

            if (contextCompat == null) {
                return true;
            }

            Method checkSelfPermissionMethod = contextCompat.getMethod("checkSelfPermission", new Class[]{Context.class, String.class});
            int result = (int) checkSelfPermissionMethod.invoke(null, new Object[]{context, permission});
            if (result != PackageManager.PERMISSION_GRANTED) {
                KbLogUtil.w("You can fix this by adding the following to your AndroidManifest.xml file:\n"
                        + "<uses-permission android:name=\"" + permission + "\" />");
                return false;
            }
            return true;
        } catch (Exception e) {
            //ignore
            return true;
        }
    }

    private static String getUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    private static String md5(String input) {
        try {
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(input.getBytes());
            byte[] md = mdInst.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte aMd : md) {
                String shaHex = Integer.toHexString(aMd & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();
        } catch (Exception e) {
            //ignore
        }
        return "";
    }

    private static String getImei(Context context) {
        String imei = "";
        try {
            if (!checkHasPermission(context, "android.permission.READ_PHONE_STATE")) {
                return imei;
            }
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                imei = tm.getDeviceId();
            }
        } catch (Exception e) {
            //ignore
        }
        return imei;
    }

    /**
     * 获得设备的AndroidId
     *
     * @param context 上下文
     * @return 设备的AndroidId
     */
    private static String getAndroidId(Context context) {
        try {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception ex) {
            //ignore
        }
        return "";
    }

    /**
     * 获得设备序列号（如：WTK7N16923005607）, 个别设备无法获取
     *
     * @return 设备序列号
     */
    private static String getSerial() {
        String serial = "";
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                serial = Build.getSerial();
            } else {
                serial = Build.SERIAL;
            }
            return serial;
        } catch (Exception ex) {
            //ignore
        }
        return serial;
    }

    /**
     * 获取手机的 Mac 地址
     *
     * @param context Context
     * @return String 当前手机的 Mac 地址
     */
    private static String getMacAddress(Context context) {
        try {
            if (!checkHasPermission(context, "android.permission.ACCESS_WIFI_STATE")) {
                return "";
            }
            WifiManager wifiMan = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMan.getConnectionInfo();

            if (wifiInfo != null && MARSHMALLOW_MAC_ADDRESS.equals(wifiInfo.getMacAddress())) {
                String result = null;
                try {
                    wifiInfo.getBSSID();
                    result = getMacAddressByInterface();
                    if (result != null) {
                        return result;
                    }
                } catch (Exception e) {
                    //ignore
                }
            } else {
                if (wifiInfo != null && wifiInfo.getMacAddress() != null) {
                    return wifiInfo.getMacAddress();
                } else {
                    return "";
                }
            }
        } catch (Exception e) {
            //ignore
        }
        return "";
    }

    private static String getMacAddressByInterface() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (nif.getName().equalsIgnoreCase("wlan0")) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }

                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:", b));
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }

        } catch (Exception e) {
            //ignore
        }
        return null;
    }

}
