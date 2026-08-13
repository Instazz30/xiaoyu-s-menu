package com.xiaxiaoyu.xingbangmenu.template.engine;

import com.xiaxiaoyu.xingbangmenu.template.PosterTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 模板注册中心 — 加载所有 JSON 模板，提供按 ID 查找。
 * 替代原来 Spring 注入的 List<PosterTemplate>。
 */
public class TemplateRegistry {

    private static final Logger log = LoggerFactory.getLogger(TemplateRegistry.class);

    private final List<PosterTemplate> templates;

    public TemplateRegistry(TemplateLoader loader, TemplateEngine engine) {
        List<TemplateDefinition> defs = loader.loadAll();
        List<PosterTemplate> list = new ArrayList<>();
        for (TemplateDefinition def : defs) {
            list.add(new JsonTemplate(def, engine));
        }
        this.templates = List.copyOf(list);
        log.info("已注册 {} 个海报模板", templates.size());
    }

    public List<PosterTemplate> getAll() {
        return templates;
    }

    public PosterTemplate getById(String id) {
        if (id == null) return templates.isEmpty() ? null : templates.get(0);
        for (PosterTemplate t : templates) {
            if (t.getId().equals(id)) return t;
        }
        log.warn("模板 {} 不存在，使用默认模板", id);
        return templates.isEmpty() ? null : templates.get(0);
    }

    public boolean exists(String id) {
        if (id == null) return false;
        return templates.stream().anyMatch(t -> t.getId().equals(id));
    }
}
