package com.xiaxiaoyu.xingbangmenu.controller;

import com.xiaxiaoyu.xingbangmenu.common.Result;
import com.xiaxiaoyu.xingbangmenu.service.PublicPosterBackgroundService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/poster-backgrounds")
public class PosterBackgroundController {

    private final PublicPosterBackgroundService backgroundService;

    public PosterBackgroundController(PublicPosterBackgroundService backgroundService) {
        this.backgroundService = backgroundService;
    }

    @GetMapping
    public Result<List<Map<String, String>>> list() {
        return Result.ok(backgroundService.list());
    }
}
