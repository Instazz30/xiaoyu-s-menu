package com.xiaxiaoyu.xingbangmenu.template;

import java.awt.image.BufferedImage;
import java.util.List;

public interface PosterTemplate {

    /** 模板唯一标识 */
    String getId();

    /** 模板显示名称 */
    String getName();

    /**
     * 渲染海报，返回每页的 BufferedImage 列表。
     * @param ctx 海报上下文数据
     * @return 每页的图片
     */
    List<BufferedImage> render(PosterContext ctx);
}
