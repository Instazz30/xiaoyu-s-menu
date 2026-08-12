package com.xiaxiaoyu.xingbangmenu.template.engine;

import com.xiaxiaoyu.xingbangmenu.template.PosterContext;
import com.xiaxiaoyu.xingbangmenu.template.PosterTemplate;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * 适配器 — 将 JSON 模板包装为 PosterTemplate 接口，无缝接入现有系统。
 */
public class JsonTemplate implements PosterTemplate {

    private final TemplateDefinition definition;
    private final TemplateEngine engine;

    public JsonTemplate(TemplateDefinition definition, TemplateEngine engine) {
        this.definition = definition;
        this.engine = engine;
    }

    @Override
    public String getId() {
        return definition.getId();
    }

    @Override
    public String getName() {
        return definition.getName();
    }

    @Override
    public List<BufferedImage> render(PosterContext ctx) {
        return engine.render(definition, ctx);
    }
}
