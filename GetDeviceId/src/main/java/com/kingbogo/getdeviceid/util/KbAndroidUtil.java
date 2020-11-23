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

    private static final String INVALID_IMEI_ID = "0000000000000";
    private static final String INVALID_ANDROID_ID = "9774d56d682e549c";
    private static final String MARSHMALLOW_MAC_ADDRESS = "02:00:00:00:00:00";
    private static final String INVALID_MAC_ADDRESS = "00:11:22:33:44:55";

    /**
     * 获取唯一码
     */
    public static String getUniqueIdCode(Context context) {
        String imei = getImei(context);
        String androidId = getAndroidId(context);
        String serial = getSerial();
        String macAddress = getMacAddress(context);
        String pesudoId = getPesudoUniqueID();
        KbLogUtil.w("_getUniqueIdCode(), imei: " + imei + ", androidId: " + androidId
                + ", serial: " + serial + ", macAddress: " + macAddress + ", pesudoId: " + pesudoId);

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
        // pesudoId
        if (!KbCheckUtil.isEmpty(pesudoId)) {
            uniqueIdSb.append(pesudoId);
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

    /**
     * The IMEI: 仅仅只对Android手机有效
     * 采用此种方法，需要在AndroidManifest.xml中加入一个许可：android.permission.READ_PHONE_STATE，并且用
     * 户应当允许安装此应用。作为手机来讲，IMEI是唯一的，它应该类似于 359881030314356（除非你有一个没有量产的手
     * 机（水货）它可能有无效的IMEI，如：0000000000000）。
     */
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
        if (INVALID_IMEI_ID.equalsIgnoreCase(imei)) {
            imei = "";
        }
        return imei;
    }

    /**
     * 获得设备的AndroidId
     * * 通常被认为不可信，因为它有时为null。开发文档中说明了：这个ID会改变如果进行了出厂设置。并且，如果某个
     * * Andorid手机被Root过的话，这个ID也可以被任意改变。无需任何许可。
     *
     * @param context 上下文
     * @return 设备的AndroidId
     */
    private static String getAndroidId(Context context) {
        String androidId = "";
        try {
            androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception ex) {
            //ignore
        }
        if (INVALID_ANDROID_ID.equalsIgnoreCase(androidId)) {
            androidId = "";
        }
        return androidId;
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
     * * 是另一个唯一ID。但是你需要为你的工程加入android.permission.ACCESS_WIFI_STATE 权限，否则这个地址会为
     * * null。Returns: 00:11:22:33:44:55 (这不是一个真实的地址。而且这个地址能轻易地被伪造。).WLan不必打开，
     * * 就可读取些值。
     *
     * @param context Context
     * @return String 当前手机的 Mac 地址
     */
    private static String getMacAddress(Context context) {
        String macAddress = "";
        try {
            if (!checkHasPermission(context, "android.permission.ACCESS_WIFI_STATE")) {
                return "";
            }
            WifiManager wifiMan = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMan.getConnectionInfo();
            if (wifiInfo != null) {
                macAddress = wifiInfo.getMacAddress();
                if (MARSHMALLOW_MAC_ADDRESS.equalsIgnoreCase(macAddress)) {
                    macAddress = getMacAddressByInterface();
                } else if (INVALID_MAC_ADDRESS.equalsIgnoreCase(macAddress)) {
                    macAddress = "";
                }
            }
        } catch (Exception e) {
            //ignore
        }
        return macAddress;
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

    /**
     * Pseudo-Unique ID, 这个在任何Android手机中都有效
     * 有一些特殊的情况，一些如平板电脑的设置没有通话功能，或者你不愿加入READ_PHONE_STATE许可。而你仍然想获得唯
     * 一序列号之类的东西。这时你可以通过取出ROM版本、制造商、CPU型号、以及其他硬件信息来实现这一点。这样计算出
     * 来的ID不是唯一的（因为如果两个手机应用了同样的硬件以及Rom 镜像）。但应当明白的是，出现类似情况的可能性基
     * 本可以忽略。大多数的Build成员都是字符串形式的，我们只取他们的长度信息。我们取到13个数字，并在前面加上“35
     * ”。这样这个ID看起来就和15位IMEI一样了。
     *
     * @return PesudoUniqueID
     */
    private static String getPesudoUniqueID() {
        String idShort = "35" + //we make this look like a valid IMEI
                Build.BOARD.length() % 10 +
                Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 +
                Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 +
                Build.HOST.length() % 10 +
                Build.ID.length() % 10 +
                Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 +
                Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 +
                Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 digits
        return idShort;
    }

}
