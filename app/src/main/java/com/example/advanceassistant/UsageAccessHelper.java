package com.example.advanceassistant;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;
import android.os.Process;

public class UsageAccessHelper {

    public static Boolean hasUsageAccess(Context context) {
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        if (appOpsManager == null) return false;

        int mode = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mode = appOpsManager.unsafeCheckOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        Process.myUid(),
                        context.getPackageName()
                );
            }
        } else {
            mode = appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.getPackageName()
            );
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}
