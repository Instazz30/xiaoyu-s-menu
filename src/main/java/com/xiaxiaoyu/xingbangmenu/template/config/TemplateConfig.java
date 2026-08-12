package com.xiaxiaoyu.xingbangmenu.template.config;

import com.xiaxiaoyu.xingbangmenu.template.component.*;
import com.xiaxiaoyu.xingbangmenu.template.engine.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemplateConfig {

    @Bean
    public TemplateLoader templateLoader() {
        return new TemplateLoader();
    }

    @Bean
    public DataBinder dataBinder() {
        return new DataBinder();
    }

    @Bean
    public ComponentRegistry componentRegistry() {
        ComponentRegistry registry = new ComponentRegistry();
        registry.register(new TextComponent());
        registry.register(new ImageComponent());
        registry.register(new RectComponent());
        registry.register(new LineComponent());
        registry.register(new PriceTagComponent());
        // Composite/ForEach/Grid 需要 registry 自身，先注册占位，下面用 setter 注入
        registry.register(new CompositeComponent(registry));
        registry.register(new ForEachComponent(registry));
        registry.register(new GridComponent(registry));
        return registry;
    }

    @Bean
    public TemplateEngine templateEngine(DataBinder dataBinder, ComponentRegistry registry) {
        return new TemplateEngine(dataBinder, registry);
    }

    @Bean
    public TemplateRegistry templateRegistry(TemplateLoader loader, TemplateEngine engine) {
        return new TemplateRegistry(loader, engine);
    }
}
