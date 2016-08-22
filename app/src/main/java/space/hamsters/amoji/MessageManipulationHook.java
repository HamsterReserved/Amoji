package space.hamsters.amoji;

import java.lang.reflect.Field;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by hamster on 16/8/21.
 * <p/>
 * Remove all the useless (sometimes ugly) decorations
 */
public class MessageManipulationHook {
    private static Class<?> MsgClass;
    private static Class<?> PBDecodeContextClass;
    private static Class<?> MessageInfoClass;
    private static Class<?> MessageRecordClass;
    private static Class<?> ChatMessageClass;
    private static Class<?> ExtensionInfoClass;

    private static void findClasses(ClassLoader loader) {
        if (MsgClass == null)
            MsgClass = XposedHelpers.findClass("msf.msgcomm.msg_comm$Msg", loader);
        if (PBDecodeContextClass == null)
            PBDecodeContextClass = XposedHelpers.findClass("com.tencent.mobileqq.service.message.PBDecodeContext", loader);
        if (MessageInfoClass == null)
            MessageInfoClass = XposedHelpers.findClass("com.tencent.mobileqq.troop.data.MessageInfo", loader);
        if (MessageRecordClass == null)
            MessageRecordClass = XposedHelpers.findClass("com.tencent.mobileqq.data.MessageRecord", loader);
        if (ChatMessageClass == null)
            ChatMessageClass = XposedHelpers.findClass("com.tencent.mobileqq.data.ChatMessage", loader);
        if (ExtensionInfoClass == null)
            ExtensionInfoClass = XposedHelpers.findClass("com.tencent.mobileqq.data.ExtensionInfo", loader);
    }

    private static XC_MethodHook sRestoreEmojiHook = new XC_MethodReplacement() {
        @Override
        @SuppressWarnings("unchecked")
        protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
            return -1;
        }
    };

    private static XC_MethodHook sRemoveBubbleInGroupHook = new XC_MethodHook() {
        @Override
        @SuppressWarnings("unchecked")
        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            ArrayList list = (ArrayList) param.args[1];
            if (list.size() > 0) {
                Object msgRecord = list.get(0);
                setField(MessageRecordClass, msgRecord, "vipBubbleID", 0);
            }
        }
    };

    private static XC_MethodHook sRemoveBubbleInPrivateHook = new XC_MethodHook() {
        @Override
        @SuppressWarnings("unchecked")
        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            ArrayList list = (ArrayList) param.getResult();
            if (list.size() > 0) {
                Object msgRecord = list.get(0);
                setField(MessageRecordClass, msgRecord, "vipBubbleID", 0);
            }
        }
    };

    private static XC_MethodHook sRemoveCustomFontHook = new XC_MethodHook() {
        @Override
        @SuppressWarnings("unchecked")
        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            Object extInfo = param.args[1];
            setField(ExtensionInfoClass, extInfo, "uVipFont", 0);
        }
    };

    public static void hook(ClassLoader loader) {
        XposedBridge.log("Amoji hooked on QQ");

        XSharedPreferences preferences = new XSharedPreferences("space.hamsters.amoji");

        findClasses(loader);

        /*
         * This method is mapping every character to emoji resource id.
         * -1 if not emoji or not found in emoji list
         */
        if (preferences.getBoolean("replace_emoji", true))
            XposedHelpers.findAndHookMethod("com.tencent.mobileqq.text.EmotcationConstants", loader,
                    "a", int.class,
                    sRestoreEmojiHook);

        /*
         * Disable custom bubbles in group chat
         */
        if (preferences.getBoolean("disable_bubble_in_group", true))
            XposedHelpers.findAndHookMethod("com.tencent.mobileqq.app.message.BaseMessageProcessorForTroopAndDisc", loader,
                    "a", MsgClass, ArrayList.class, PBDecodeContextClass, boolean.class, MessageInfoClass,
                    sRemoveBubbleInGroupHook);

        /*
         * Disable custom bubbles in private chat
         */
        if (preferences.getBoolean("disable_bubble_in_private", true))
            XposedHelpers.findAndHookMethod("com.tencent.mobileqq.app.message.C2CMessageProcessor", loader,
                    "a", MsgClass, PBDecodeContextClass,
                    sRemoveBubbleInPrivateHook);

        /*
         * Disable custom font
         */
        if (preferences.getBoolean("disable_custom_font", true))
            XposedHelpers.findAndHookMethod("com.etrump.mixlayout.FontManager", loader,
                    "a", ChatMessageClass, ExtensionInfoClass,
                    sRemoveCustomFontHook);
    }

    public static void setField(Class<?> clazz, Object obj, String name, Object value) {
        try {
            Field msgField = clazz.getField(name);
            msgField.setAccessible(true);
            msgField.set(obj, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            /* IllegalAccessException should not happen */
            XposedBridge.log(e);
        }
    }
}
