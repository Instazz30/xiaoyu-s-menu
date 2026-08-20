package com.xiaxiaoyu.xingbangmenu.template.component;

import com.xiaxiaoyu.xingbangmenu.template.engine.TemplateDefinition.ComponentDef;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 文本占位符绑定测试：支持 "{{issueText}}期食堂菜单" 这类内嵌占位符。
 */
class TextComponentTest {

    @Test
    void bindsInlinePlaceholder() {
        ComponentDef def = new ComponentDef();
        def.setText("{{issueText}}期食堂菜单");
        def.setDefaultText("一期食堂菜单");

        assertEquals("三期食堂菜单", TextComponent.resolveText(Map.of("issueText", "三"), def));
    }

    @Test
    void fallsBackToDefaultWhenValueMissing() {
        ComponentDef def = new ComponentDef();
        def.setText("{{issueText}}期食堂菜单");
        def.setDefaultText("一期食堂菜单");

        assertEquals("一期食堂菜单", TextComponent.resolveText(Map.of(), def));
    }
}
