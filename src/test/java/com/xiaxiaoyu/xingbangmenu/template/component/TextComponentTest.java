package com.xiaxiaoyu.xingbangmenu.template.component;

import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateDefinition.ComponentDef;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 文本占位符绑定测试：支持 "第{{issue}}期食堂菜单" 这类内嵌占位符。
 */
class TextComponentTest {

    @Test
    void bindsInlinePlaceholder() {
        ComponentDef def = new ComponentDef();
        def.setText("第{{issue}}期食堂菜单");
        def.setDefaultText("第1期食堂菜单");

        assertEquals("第3期食堂菜单", TextComponent.resolveText(Map.of("issue", 3), def));
    }

    @Test
    void fallsBackToDefaultWhenValueMissing() {
        ComponentDef def = new ComponentDef();
        def.setText("第{{issue}}期食堂菜单");
        def.setDefaultText("第1期食堂菜单");

        assertEquals("第1期食堂菜单", TextComponent.resolveText(Map.of(), def));
    }
}
