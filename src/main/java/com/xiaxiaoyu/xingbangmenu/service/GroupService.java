package com.xiaxiaoyu.xingbangmenu.service;

import com.xiaxiaoyu.xingbangmenu.entity.GroupMember;
import com.xiaxiaoyu.xingbangmenu.entity.SysGroup;
import com.xiaxiaoyu.xingbangmenu.exception.BusinessException;
import com.xiaxiaoyu.xingbangmenu.mapper.GroupMemberMapper;
import com.xiaxiaoyu.xingbangmenu.mapper.SysGroupMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SysGroupMapper groupMapper;
    private final GroupMemberMapper memberMapper;
    private final PermissionService permissionService;

    public GroupService(SysGroupMapper groupMapper,
                        GroupMemberMapper memberMapper,
                        PermissionService permissionService) {
        this.groupMapper = groupMapper;
        this.memberMapper = memberMapper;
        this.permissionService = permissionService;
    }

    @Transactional
    public Map<String, Object> create(Long userId, String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(400, "小组名称不能为空");
        }
        SysGroup group = new SysGroup();
        group.setName(name.trim());
        group.setGroupCode(generateUniqueCode());
        group.setOwnerId(userId);
        groupMapper.insert(group);

        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(userId);
        member.setRole("admin");
        memberMapper.insert(member);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", group.getId());
        result.put("name", group.getName());
        result.put("groupCode", group.getGroupCode());
        result.put("ownerId", group.getOwnerId());
        result.put("createdAt", group.getCreatedAt());
        result.put("role", "admin");
        result.put("memberCount", 1);
        return result;
    }

    public List<Map<String, Object>> myGroups(Long userId) {
        List<SysGroup> groups = groupMapper.selectByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysGroup g : groups) {
            GroupMember member = memberMapper.selectByGroupAndUser(g.getId(), userId);
            boolean admin = member != null && "admin".equals(member.getRole());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", g.getId());
            item.put("name", g.getName());
            item.put("ownerId", g.getOwnerId());
            item.put("createdAt", g.getCreatedAt());
            item.put("role", member != null ? member.getRole() : "member");
            item.put("albumPermission",
                    member != null && member.getAlbumPermission() != null
                            ? member.getAlbumPermission() : 0);
            item.put("memberCount", memberMapper.countByGroupId(g.getId()));
            if (admin) {
                item.put("groupCode", g.getGroupCode());
            }
            result.add(item);
        }
        return result;
    }

    @Transactional
    public Map<String, Object> join(Long userId, String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(400, "请输入小组码");
        }
        SysGroup group = groupMapper.selectByCode(code.trim().toUpperCase());
        if (group == null) {
            throw new BusinessException(400, "小组码不存在或小组已解散");
        }
        if (memberMapper.selectByGroupAndUser(group.getId(), userId) != null) {
            throw new BusinessException(400, "你已加入该小组");
        }
        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(userId);
        member.setRole("member");
        memberMapper.insert(member);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", group.getId());
        result.put("name", group.getName());
        result.put("role", "member");
        return result;
    }

    public Map<String, Object> detail(Long groupId, Long userId) {
        GroupMember my = permissionService.requireMember(groupId, userId);
        SysGroup group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(404, "小组不存在或已解散");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", group.getId());
        result.put("name", group.getName());
        result.put("ownerId", group.getOwnerId());
        result.put("createdAt", group.getCreatedAt());
        result.put("myRole", my.getRole());
        result.put("myAlbumPermission",
                my.getAlbumPermission() != null ? my.getAlbumPermission() : 0);
        result.put("memberCount", memberMapper.countByGroupId(groupId));
        if ("admin".equals(my.getRole())) {
            result.put("groupCode", group.getGroupCode());
        }
        result.put("members", memberMapper.selectByGroupId(groupId));
        return result;
    }

    /** 成员申请相册上传权限 */
    @Transactional
    public void applyAlbumPermission(Long groupId, Long userId) {
        GroupMember member = permissionService.requireMember(groupId, userId);
        int p = member.getAlbumPermission() != null ? member.getAlbumPermission() : 0;
        if (p == 2) {
            throw new BusinessException(400, "你已开通相册上传权限");
        }
        if (p == 1) {
            throw new BusinessException(400, "申请已在审核中，请耐心等待");
        }
        member.setAlbumPermission(1);
        memberMapper.update(member);
    }

    /** 待审核的相册权限申请列表（仅管理员），附申请人信息 */
    public List<Map<String, Object>> albumApplications(Long groupId, Long adminId) {
        permissionService.requireAdmin(groupId, adminId);
        return memberMapper.selectByGroupId(groupId).stream()
                .filter(m -> {
                    Object p = m.get("albumPermission");
                    return p instanceof Number n && n.intValue() == 1;
                })
                .collect(Collectors.toList());
    }

    /** 小组名称（用于导出文件名等场景） */
    public String groupName(Long groupId) {
        SysGroup group = groupMapper.selectById(groupId);
        return group != null ? group.getName() : "修改记录";
    }

    /** 审批相册权限申请（仅管理员）：通过=2，拒绝=0 */
    @Transactional
    public void reviewAlbumPermission(Long groupId, Long adminId, Long targetUserId, boolean approve) {
        permissionService.requireAdmin(groupId, adminId);
        GroupMember target = memberMapper.selectByGroupAndUser(groupId, targetUserId);
        if (target == null) {
            throw new BusinessException(404, "该成员不在小组中");
        }
        target.setAlbumPermission(approve ? 2 : 0);
        memberMapper.update(target);
    }

    @Transactional
    public void updateName(Long groupId, Long userId, String name) {
        permissionService.requireAdmin(groupId, userId);
        if (name == null || name.isBlank()) {
            throw new BusinessException(400, "小组名称不能为空");
        }
        SysGroup group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(404, "小组不存在或已解散");
        }
        group.setName(name.trim());
        groupMapper.update(group);
    }

    @Transactional
    public void removeMember(Long groupId, Long adminId, Long targetUserId) {
        permissionService.requireAdmin(groupId, adminId);
        if (adminId.equals(targetUserId)) {
            throw new BusinessException(400, "不能移除自己，请使用退出小组");
        }
        GroupMember target = memberMapper.selectByGroupAndUser(groupId, targetUserId);
        if (target == null) {
            throw new BusinessException(404, "该成员不在小组中");
        }
        if ("admin".equals(target.getRole()) && countAdmins(groupId) <= 1) {
            throw new BusinessException(400, "小组至少需要保留一名管理员");
        }
        memberMapper.leave(target.getId());
    }

    @Transactional
    public void leave(Long groupId, Long userId) {
        GroupMember member = permissionService.requireMember(groupId, userId);
        if ("admin".equals(member.getRole()) && countAdmins(groupId) <= 1) {
            throw new BusinessException(400, "你是小组唯一管理员，请先转让管理员或解散小组");
        }
        memberMapper.leave(member.getId());
    }

    @Transactional
    public void dissolve(Long groupId, Long userId) {
        permissionService.requireAdmin(groupId, userId);
        groupMapper.deleteById(groupId);
        memberMapper.deleteByGroupId(groupId);
    }

    @Transactional
    public void addAdmin(Long groupId, Long adminId, Long targetUserId) {
        permissionService.requireAdmin(groupId, adminId);
        GroupMember target = memberMapper.selectByGroupAndUser(groupId, targetUserId);
        if (target == null) {
            throw new BusinessException(404, "该成员不在小组中");
        }
        target.setRole("admin");
        memberMapper.update(target);
    }

    @Transactional
    public void transfer(Long groupId, Long adminId, Long targetUserId) {
        GroupMember admin = permissionService.requireAdmin(groupId, adminId);
        GroupMember target = memberMapper.selectByGroupAndUser(groupId, targetUserId);
        if (target == null) {
            throw new BusinessException(404, "该成员不在小组中");
        }
        if ("admin".equals(admin.getRole()) && adminId.equals(groupMapper.selectById(groupId).getOwnerId())) {
            groupMapper.updateOwner(groupId, targetUserId);
        }
        target.setRole("admin");
        memberMapper.update(target);
    }

    private long countAdmins(Long groupId) {
        return memberMapper.selectByGroupId(groupId).stream()
                .filter(m -> "admin".equals(m.get("role")))
                .count();
    }

    private String generateUniqueCode() {
        for (int i = 0; i < 20; i++) {
            StringBuilder sb = new StringBuilder(6);
            for (int j = 0; j < 6; j++) {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (groupMapper.selectByCode(code) == null) {
                return code;
            }
        }
        throw new BusinessException(500, "小组码生成失败，请重试");
    }
}
