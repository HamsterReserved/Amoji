package space.hamsters.amoji;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by hamster on 16/8/21.
 * <p>
 * Hook QQ to disable emoji replacing
 */
public class XposedHookRegister implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals("com.tencent.mobileqq")) {
            MessageManipulationHook.hook(loadPackageParam.classLoader);
        }
    }


}
