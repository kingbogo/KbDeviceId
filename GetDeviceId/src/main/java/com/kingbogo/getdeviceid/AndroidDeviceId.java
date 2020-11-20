package com.kingbogo.getdeviceid;

import android.content.Context;

import androidx.annotation.NonNull;

import com.github.gzuliyujiang.oaid.DeviceID;
import com.github.gzuliyujiang.oaid.IDeviceId;
import com.github.gzuliyujiang.oaid.IOAIDGetter;
import com.kingbogo.getdeviceid.callback.GetDeviceIdCallback;
import com.kingbogo.getdeviceid.util.KbAndroidUtil;
import com.kingbogo.getdeviceid.util.KbCheckUtil;
import com.kingbogo.getdeviceid.util.KbConstants;
import com.kingbogo.getdeviceid.util.KbFileUtil;
import com.kingbogo.getdeviceid.util.KbLogUtil;
import com.kingbogo.getdeviceid.util.KbSpUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * 先获取 OAID，如果获取不到，则自己生成。
 * </p>
 *
 * @author Kingbo
 * @date 2020/11/19
 */
public class AndroidDeviceId {

    private static String mDeviceId;
    private static String mUniquePath;

    /**
     * @param applicationContext 应用上下文
     * @param callback           回调
     */
    public static void getDeviceId(Context applicationContext, GetDeviceIdCallback callback) {
        // 1、先从全局变量
        if (!KbCheckUtil.isEmpty(mDeviceId)) {
            KbLogUtil.d("_getDeviceId(), 从全局变量取mDeviceId");
            notifyCallback(callback, mDeviceId);
            return;
        }

        // 2、从 PS 中取
        String spDeviceId = getDeviceFromSp(applicationContext);
        if (!KbCheckUtil.isEmpty(spDeviceId)) {
            KbLogUtil.d("_getDeviceId(), 从SP中取到了, spDeviceId: " + spDeviceId);
            mDeviceId = spDeviceId;
            notifyCallback(callback, mDeviceId);
            return;
        }

        // 3、获取一次
        IDeviceId deviceIdObj = DeviceID.with(applicationContext);
        if (deviceIdObj.supportOAID()) {
            long startTime = System.currentTimeMillis();
            deviceIdObj.doGet(new IOAIDGetter() {
                @Override
                public void onOAIDGetComplete(@NonNull String oaid) {
                    KbLogUtil.d("_onOAIDGetComplete(), oaid: " + oaid + "，耗时：" + (System.currentTimeMillis() - startTime) + "ms");
                    if (!KbCheckUtil.isEmpty(oaid)) {
                        mDeviceId = oaid;
                        setDeviceIdToSp(applicationContext, mDeviceId);
                        notifyCallback(callback, mDeviceId);
                    } else {
                        // 未获取到，则自己生成
                        getSelfId(applicationContext, callback);
                    }
                }

                @Override
                public void onOAIDGetError(@NonNull Exception exception) {
                    KbLogUtil.e("获取OAID出错了 -> ", exception);
                    // 获取出错，则自己生成
                    getSelfId(applicationContext, callback);
                }
            });
        } else {
            // 不支持，则自己生成
            getSelfId(applicationContext, callback);
        }
    }

    // --------------------------------------------------------------- @ private

    private static void notifyCallback(GetDeviceIdCallback callback, String deviceId) {
        if (callback != null) {
            callback.onGetIdComplete(deviceId);
        }
    }

    private static String getUniquePath() {
        if (mUniquePath == null) {
            mUniquePath = KbFileUtil.getExternalStorageDirectoryPath() + KbConstants.SELF_FILE_PATH;
        }
        return mUniquePath;
    }

    private static void setDeviceIdToSp(Context context, String deviceId) {
        KbSpUtil.setSP(context, KbConstants.SP_DEVICE_ID, deviceId);
    }

    private static String getDeviceFromSp(Context context) {
        return (String) KbSpUtil.getSp(context, KbConstants.SP_DEVICE_ID, "");
    }

    /**
     * 自己生成
     */
    private static void getSelfId(Context applicationContext, GetDeviceIdCallback callback) {
        KbLogUtil.w("_getSelfId(), 未获取到oaid，自己生成。。。");
        String selfId = readSelfId(applicationContext);
        if (!KbCheckUtil.isEmpty(selfId)) {
            // 取到了 selfId : 同步，并回调出去
            mDeviceId = selfId;
            setDeviceIdToSp(applicationContext, mDeviceId);
            notifyCallback(callback, mDeviceId);
        } else {
            // 没取到 selfId : 生成
            selfId = KbAndroidUtil.getUniqueIdCode(applicationContext);
            // 同步，并回调出去
            mDeviceId = selfId;
            setDeviceIdToSp(applicationContext, mDeviceId);
            notifyCallback(callback, mDeviceId);
            // 存储
            saveSelfToSp(applicationContext, selfId);
            saveSelfToFile(selfId);
        }
    }

    private static String readSelfId(Context context) {
        // 先从sp中取，没取到的话则从file中取
        String selfId = readSelfIdFromSp(context);
        if (KbCheckUtil.isEmpty(selfId)) {
            KbLogUtil.d("_readSelfId(), sp未取到，则从file中取");
            selfId = readSelfIdFromFile();
            if (!KbCheckUtil.isEmpty(selfId)) {
                // 保存到SP
                saveSelfToSp(context, selfId);
            }
        } else {
            KbLogUtil.d("_readSelfId(), sp取到了，selfId：" + selfId);
        }
        return selfId;
    }

    private static String readSelfIdFromSp(Context context) {
        return (String) KbSpUtil.getSp(context, KbConstants.SP_SELF_ID, "");
    }

    private static String readSelfIdFromFile() {
        if (KbFileUtil.isExternalStorageReadable()) {
            File file = new File(getUniquePath(), KbConstants.SELF_FILE_NAME);
            if (file.exists()) {
                byte[] bytes = KbFileUtil.file2byte(file.getAbsolutePath());
                if (bytes != null) {
                    String selfId = new String(bytes, StandardCharsets.UTF_8);
                    KbLogUtil.d("_readSelfIdFromFile(), file取到了，selfId：" + selfId);
                    return selfId;
                }
            }
        }
        KbLogUtil.d("_readSelfIdFromFile(), file未取到");
        return "";
    }

    private static void saveSelfToSp(Context context, String selfId) {
        KbSpUtil.setSP(context, KbConstants.SP_SELF_ID, selfId);
    }

    private static void saveSelfToFile(String selfId) {
        if (!KbCheckUtil.isEmpty(selfId) && KbFileUtil.isExternalStorageWritable()) {
            new Thread(() -> {
                File file = new File(getUniquePath(), KbConstants.SELF_FILE_NAME);
                if (!file.exists()) {
                    // 先创建文件
                    KbFileUtil.createNewFile(file);
                }
                KbFileUtil.byte2File(selfId.getBytes(StandardCharsets.UTF_8), getUniquePath(), KbConstants.SELF_FILE_NAME);
            }).start();
        }
    }

}
