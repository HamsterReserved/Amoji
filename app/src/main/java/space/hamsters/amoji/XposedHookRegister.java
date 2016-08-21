package space.hamsters.amoji;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by hamster on 16/8/21.
 * <p/>
 * Hook QQ to disable emoji replacing
 */
public class XposedHookRegister implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals("com.tencent.mobileqq")) {
            XposedBridge.log("Amoji hooked on QQ.");
            hook(loadPackageParam.classLoader);
        }
    }

    private void hook(ClassLoader loader) {
        /*
         * This method is mapping every character to emoji resource id.
         * -1 if not emoji or not found in emoji list
         */
        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.text.EmotcationConstants", loader, "a", int.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return -1;
            }
        });
    }
}
