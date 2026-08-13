package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.entity.GroupMember;
import com.xiaxiaoyu.xingbangmenu.entity.MenuRecipe;
import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.exception.BusinessException;
import com.xiaxiaoyu.xingbangmenu.mapper.GroupMemberMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.MenuRecipeMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PermissionService {

    private static final Logger log = LoggerFactory.getLogger(PermissionService.class);

    private final GroupMemberMapper memberMapper;
    private final MenuRecipeMapper recipeMapper;

    public PermissionService(GroupMemberMapper memberMapper,
                             MenuRecipeMapper recipeMapper) {
        this.memberMapper = memberMapper;
        this.recipeMapper = recipeMapper;
    }

    public SysUser currentUser(HttpServletRequest request) {
        SysUser user = (SysUser) request.getAttribute("currentUser");
        if (user == null) {
            //若未登录则抛异常
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return user;
    }

    public GroupMember requireMember(Long groupId, Long userId) {
        if (groupId == null) {
            throw new BusinessException(400, "缺少小组信息");
        }
        GroupMember member = memberMapper.selectByGroupAndUser(groupId, userId);
        if (member == null) {
            log.warn("小组成员权限校验失败: groupId={}, userId={}", groupId, userId);
            throw new BusinessException(403, "你不是该小组成员，无权访问");
        }
        return member;
    }

    public GroupMember requireAdmin(Long groupId, Long userId) {
        GroupMember member = requireMember(groupId, userId);
        if (!"admin".equals(member.getRole())) {
            throw new BusinessException(403, "该操作需要小组管理员权限");
        }
        return member;
    }

    /** 校验用户是菜谱所属小组的成员，并返回菜谱 */
    public MenuRecipe requireMemberOfRecipe(Long recipeId, Long userId) {
        MenuRecipe recipe = recipeMapper.selectById(recipeId);
        if (recipe == null) {
            throw new BusinessException(10001, "菜谱不存在或已删除");
        }
        requireMember(recipe.getGroupId(), userId);
        return recipe;
    }

    /** 校验用户是菜谱所属小组的管理员，并返回菜谱 */
    public MenuRecipe requireAdminOfRecipe(Long recipeId, Long userId) {
        MenuRecipe recipe = requireMemberOfRecipe(recipeId, userId);
        requireAdmin(recipe.getGroupId(), userId);
        return recipe;
    }
}
