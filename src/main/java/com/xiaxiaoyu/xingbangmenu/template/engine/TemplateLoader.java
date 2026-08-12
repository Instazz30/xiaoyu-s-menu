package com.xiaxiaoyu.xingbangmenu.template.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TemplateLoader {

    private static final Logger log = LoggerFactory.getLogger(TemplateLoader.class);
    private static final String TEMPLATE_LOCATION = "classpath:templates/*.json";

    private final ObjectMapper mapper = new ObjectMapper();

    public TemplateDefinition load(String templateId) throws IOException {
        Resource resource = new PathMatchingResourcePatternResolver()
                .getResource("classpath:templates/" + templateId + ".json");
        if (!resource.exists()) {
            throw new IllegalArgumentException("模板不存在: " + templateId);
        }
        return mapper.readValue(resource.getInputStream(), TemplateDefinition.class);
    }

    public List<TemplateDefinition> loadAll() {
        List<TemplateDefinition> result = new ArrayList<>();
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(TEMPLATE_LOCATION);
            Arrays.sort(resources, (a, b) -> a.getFilename().compareTo(b.getFilename()));
            for (Resource r : resources) {
                try {
                    TemplateDefinition def = mapper.readValue(r.getInputStream(), TemplateDefinition.class);
                    result.add(def);
                    log.info("加载模板: {} ({})", def.getName(), def.getId());
                } catch (IOException e) {
                    log.error("模板解析失败: {}", r.getFilename(), e);
                }
            }
        } catch (IOException e) {
            log.error("扫描模板目录失败", e);
        }
        return result;
    }
}
