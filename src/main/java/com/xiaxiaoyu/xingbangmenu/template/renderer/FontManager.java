package com.xiaxiaoyu.xingbangmenu.template.renderer;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class FontManager {

    private static final String[] CANDIDATES = {
            "Microsoft YaHei", "SimHei", "SimSun",
            "PingFang SC", "Heiti SC", "WenQuanYi Micro Hei",
            "Noto Sans CJK SC", "SansSerif"
    };

    private final Map<String, Font> cache = new ConcurrentHashMap<>();

    Font getFont(String family, int style, int size) {
        String key = family + "|" + style + "|" + size;
        return cache.computeIfAbsent(key, k -> resolve(family, style, size));
    }

    private Font resolve(String family, int style, int size) {
        if (family != null && !family.isEmpty()) {
            Font f = new Font(family, style, size);
            if (f.canDisplayUpTo("菜谱") < 0) return f;
        }
        for (String name : CANDIDATES) {
            Font f = new Font(name, style, size);
            if (f.canDisplayUpTo("菜谱") < 0) return f;
        }
        return new Font("SansSerif", style, size);
    }
}
