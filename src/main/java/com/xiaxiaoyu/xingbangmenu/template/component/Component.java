package com.xiaxiaoyu.xingbangmenu.template.component;

import com.xiaxiaoyu.xingbangmenu.template.renderer.Renderer;

public interface Component {
    String type();
    int render(Renderer r, ComponentContext ctx);
}
