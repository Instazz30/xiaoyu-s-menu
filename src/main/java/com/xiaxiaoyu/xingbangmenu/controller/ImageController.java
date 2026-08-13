package com.xiaxiaoyu.xingbangmenu.controller;

import com.xiaxiaoyu.xingbangmenu.common.Result;
import com.xiaxiaoyu.xingbangmenu.entity.ImageAsset;
import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.service.ImageService;
import com.xiaxiaoyu.xingbangmenu.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ImageController {

    private final ImageService imageService;
    private final PermissionService permissionService;

    public ImageController(ImageService imageService, PermissionService permissionService) {
        this.imageService = imageService;
        this.permissionService = permissionService;
    }

    /** 上传菜品图片（进入待审核） */
    @PostMapping("/recipes/{recipeId}/items/{itemId}/images")
    public Result<ImageAsset> upload(HttpServletRequest request,
                                     @PathVariable Long recipeId,
                                     @PathVariable Long itemId,
                                     @RequestParam("file") MultipartFile file,
                                     @RequestParam(required = false) String source) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(imageService.upload(recipeId, itemId, file, user.getId(), source));
    }

    /** 上传小碗菜自由图（不对应具体菜品） */
    @PostMapping("/recipes/{recipeId}/images")
    public Result<ImageAsset> uploadXiaowan(HttpServletRequest request,
                                            @PathVariable Long recipeId,
                                            @RequestParam("file") MultipartFile file,
                                            @RequestParam(required = false) String source) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(imageService.upload(recipeId, null, file, user.getId(), source));
    }

    /** 删除小碗菜自由图 */
    @DeleteMapping("/recipes/{recipeId}/images/{imageId}")
    public Result<Void> deleteXiaowan(HttpServletRequest request,
                                      @PathVariable Long recipeId,
                                      @PathVariable Long imageId) {
        SysUser user = permissionService.currentUser(request);
        imageService.deleteXiaowan(recipeId, imageId, user.getId());
        return Result.ok();
    }

    /** 替换菜品图片（仅管理员，新图进入待审核） */
    @PutMapping("/recipes/{recipeId}/items/{itemId}/images")
    public Result<ImageAsset> replace(HttpServletRequest request,
                                      @PathVariable Long recipeId,
                                      @PathVariable Long itemId,
                                      @RequestParam("file") MultipartFile file) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(imageService.replace(recipeId, itemId, file, user.getId()));
    }

    /** 下架菜品当前展示图（仅管理员） */
    @DeleteMapping("/recipes/{recipeId}/items/{itemId}/images")
    public Result<Void> unbind(HttpServletRequest request,
                               @PathVariable Long recipeId,
                               @PathVariable Long itemId) {
        SysUser user = permissionService.currentUser(request);
        imageService.unbind(recipeId, itemId, user.getId());
        return Result.ok();
    }

    /** 图片完整性检查 */
    @GetMapping("/recipes/{recipeId}/images/status")
    public Result<Map<String, Object>> status(HttpServletRequest request,
                                              @PathVariable Long recipeId) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(imageService.getStatus(recipeId, user.getId()));
    }

    /** 小组待审核图片列表（仅管理员） */
    @GetMapping("/groups/{groupId}/images/pending")
    public Result<List<Map<String, Object>>> pending(HttpServletRequest request,
                                                     @PathVariable Long groupId) {
        SysUser user = permissionService.currentUser(request);
        return Result.ok(imageService.pendingImages(groupId, user.getId()));
    }

    /** 审核图片（仅管理员） */
    @PostMapping("/images/{imageId}/review")
    public Result<Void> review(HttpServletRequest request,
                               @PathVariable Long imageId,
                               @RequestBody Map<String, Object> body) {
        SysUser user = permissionService.currentUser(request);
        Boolean approve = body.get("approve") instanceof Boolean b ? b : Boolean.valueOf(String.valueOf(body.get("approve")));
        String note = body.get("note") != null ? body.get("note").toString() : null;
        imageService.review(imageId, Boolean.TRUE.equals(approve), note, user.getId());
        return Result.ok();
    }
}
