package com.kingbogo.getdeviceid.util;

import android.util.Log;

import androidx.annotation.IntDef;

import com.kingbogo.getdeviceid.BuildConfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;


/**
 * <p>
 * 日志工具类 <br/>
 * 1、自动显示tag、类名、行号；<br/>
 * 2、兼容显示超过4000的问题；
 * </p>
 *
 * @author Kingbo
 * @date 2018/10/20
 */
public final class KbLogUtil {

    public static boolean LOG_DEBUG = BuildConfig.DEBUG;
    private static final String TAG_PREFIX = "Kingbo:";
    private static final int MAX_LEN = 4000;

    private static final int V = Log.VERBOSE;
    private static final int D = Log.DEBUG;
    private static final int I = Log.INFO;
    private static final int W = Log.WARN;
    private static final int E = Log.ERROR;

    @IntDef({V, D, I, W, E})
    @Retention(RetentionPolicy.SOURCE)
    @interface TYPE {
    }

    private KbLogUtil() {
    }

    private static String generateTag(StackTraceElement caller) {
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(Locale.CHINA, tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        tag = getFullTag(tag);
        return tag;
    }

    private static StackTraceElement getCallerStackTraceElement() {
        return Thread.currentThread().getStackTrace()[4];
    }

    public static void v(String content) {
        if (LOG_DEBUG) {
            String tag = generateTag(getCallerStackTraceElement());
            showLog(V, tag, content, null);
        }
    }

    public static void v(String tag, String content) {
        if (LOG_DEBUG) {
            if (tag == null) {
                tag = generateTag(getCallerStackTraceElement());
            } else {
                tag = getFullTag(tag);
            }
            showLog(V, tag, content, null);
        }
    }

    public static void v(String content, Throwable tr) {
        if (LOG_DEBUG) {
            String tag = generateTag(getCallerStackTraceElement());
            showLog(V, tag, content, tr);
        }
    }

    public static void d(String content) {
        if (LOG_DEBUG) {
            String tag = generateTag(getCallerStackTraceElement());
            showLog(D, tag, content, null);
        }
    }

    public static void d(String tag, String content) {
        if (LOG_DEBUG) {
            if (tag == null) {
                tag = generateTag(getCallerStackTraceElement());
            } else {
                tag = getFullTag(tag);
            }
            showLog(D, tag, content, null);
        }
    }

    public static void d(String content, Throwable tr) {
        if (LOG_DEBUG) {
            String tag = generateTag(getCallerStackTraceElement());
            showLog(D, tag, content, tr);
        }
    }

    public static void i(String content) {
        if (LOG_DEBUG) {
            String tag = generateTag(getCallerStackTraceElement());
            showLog(I, tag, content, null);
        }
    }

    public static void i(String tag, String content) {
        if (LOG_DEBUG) {
            if (tag == null) {
                tag = generateTag(getCallerStackTraceElement());
            } else {
                tag = getFullTag(tag);
            }
            showLog(I, tag, content, null);
        }
    }

    public static void i(String content, Throwable tr) {
        if (LOG_DEBUG) {
            String tag = generateTag(getCallerStackTraceElement());
            showLog(I, tag, content, tr);
        }
    }

    public static void w(String content) {
        if (LOG_DEBUG) {
            String tag = generateTag(getCallerStackTraceElement());
            showLog(W, tag, content, null);
        }
    }

    public static void w(String tag, String content) {
        if (LOG_DEBUG) {
            if (tag == null) {
                tag = generateTag(getCallerStackTraceElement());
            } else {
                tag = getFullTag(tag);
            }
            showLog(W, tag, content, null);
        }
    }

    public static void w(String content, Throwable tr) {
        if (LOG_DEBUG) {
            String tag = generateTag(getCallerStackTraceElement());
            showLog(W, tag, content, tr);
        }
    }

    public static void w(Throwable tr) {
        w("", tr);
    }

    public static void e(String content) {
        if (LOG_DEBUG) {
            String tag = generateTag(getCallerStackTraceElement());
            showLog(E, tag, content, null);
        }
    }

    public static void e(String tag, String content) {
        if (LOG_DEBUG) {
            if (tag == null) {
                tag = generateTag(getCallerStackTraceElement());
            } else {
                tag = getFullTag(tag);
            }
            showLog(E, tag, content, null);
        }
    }

    public static void e(String content, Throwable tr) {
        if (LOG_DEBUG) {
            String tag = generateTag(getCallerStackTraceElement());
            showLog(E, tag, content, tr);
        }
    }

    public static void e(Throwable tr) {
        e("", tr);
    }


    public static String getStackTraceString(Throwable tr) {
        if (tr != null) {
            return Log.getStackTraceString(tr);
        } else {
            return "";
        }
    }

    /**
     * 打印当前堆栈信息
     */
    public static void printStackTraceString() {
        e(new RuntimeException("打印当前堆栈信息==>"));
    }

    // ================================================================ @ private

    private static void showLog(@TYPE int type, final String tag, final String msg, final Throwable tr) {
        try {
            int len = msg.length();
            int countOfSub = len / MAX_LEN;
            if (countOfSub > 0) {
                int index = 0;
                for (int i = 0; i < countOfSub; i++) {
                    if (tr != null) {
                        printLog(type, tag, msg.substring(index, index + MAX_LEN), tr);
                    } else {
                        printLog(type, tag, msg.substring(index, index + MAX_LEN));
                    }
                    index += MAX_LEN;
                }

                if (index != len) {
                    if (tr != null) {
                        printLog(type, tag, msg.substring(index, len), tr);
                    } else {
                        printLog(type, tag, msg.substring(index, len));
                    }
                }
            } else {
                if (tr != null) {
                    printLog(type, tag, msg, tr);
                } else {
                    printLog(type, tag, msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printLog(@TYPE int type, final String tag, final String msg) {
        if (type == Log.VERBOSE) {
            Log.v(tag, msg);
        } else if (type == Log.DEBUG) {
            Log.d(tag, msg);
        } else if (type == Log.INFO) {
            Log.i(tag, msg);
        } else if (type == Log.WARN) {
            Log.w(tag, msg);
        } else if (type == Log.ERROR) {
            Log.e(tag, msg);
        }
    }

    private static void printLog(@TYPE int type, final String tag, final String msg, final Throwable tr) {
        if (type == Log.VERBOSE) {
            Log.v(tag, msg, tr);
        } else if (type == Log.DEBUG) {
            Log.d(tag, msg, tr);
        } else if (type == Log.INFO) {
            Log.i(tag, msg, tr);
        } else if (type == Log.WARN) {
            Log.w(tag, msg, tr);
        } else if (type == Log.ERROR) {
            Log.e(tag, msg, tr);
        }
    }

    private static String getFullTag(String tag) {
        if (tag != null && tag.contains(TAG_PREFIX)) {
            return tag;
        } else {
            return TAG_PREFIX + Thread.currentThread().getId() + ":" + tag;
        }
    }

    private static String getContentAndStackMessage(String content, Throwable tr) {
        if (tr == null) {
            return content;
        } else {
            return content + " ==> " + getStackTraceString(tr);
        }
    }

}
