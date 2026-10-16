package com.mall.module.assistant.service.guard;

import java.util.Locale;

/**
 * 宠物话题判定工具（硬性约束：只答宠物相关内容）
 */
public final class PetTopicGuard {

    private PetTopicGuard() {
    }

    public static final String REFUSE =
            "我是宠物专属小助手🐾，只回答养宠相关的问题哦～\n你可以问我：怎么科学换粮、多久给猫洗一次澡、怎么给狗狗驱虫、猫咪挑食怎么办、狗粮怎么选等。";

    private static final String[] PET_KEYWORDS = {
            "猫", "狗", "宠物", "毛孩子", "猫咪", "狗狗", "粮", "猫粮", "狗粮", "零食", "罐头",
            "洗澡", "沐浴", "驱虫", "疫苗", "绝育", "遛", "玩具", "猫砂", "牵引", "项圈", "窝",
            "呕吐", "腹泻", "拉稀", "毛发", "掉毛", "磨牙", "指甲", "耳螨", "泪痕", "口臭",
            "关节", "骨头", "老年", "幼猫", "幼犬", "皮肤病", "体内外", "免疫"
    };

    public static boolean isPetTopic(String q) {
        if (q == null) {
            return false;
        }
        String lower = q.toLowerCase(Locale.ROOT);
        for (String kw : PET_KEYWORDS) {
            if (lower.contains(kw)) {
                return true;
            }
        }
        return false;
    }
}