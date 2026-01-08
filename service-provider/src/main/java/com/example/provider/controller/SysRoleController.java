package com.example.provider.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.annotation.OperationLog;
import com.example.common.annotation.OperationLog.OperationType;
import com.example.common.model.Result;
import com.example.provider.entity.SysRole;
import com.example.provider.service.SysMenuService;
import com.example.provider.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理Controller
 */
@RestController
@RequestMapping("/api/system/role")
public class SysRoleController {
    
    @Autowired
    private SysRoleService roleService;
    
    @Autowired
    private SysMenuService menuService;
    
    /**
     * 分页查询角色列表
     */
    @GetMapping("/list")
    public Result<Page<SysRole>> list(
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysRole> result = roleService.pageList(roleName, status, page, size);
        return Result.success("获取角色列表成功", result);
    }
    
    /**
     * 获取所有启用的角色（用于下拉选择）
     */
    @GetMapping("/all")
    public Result<List<SysRole>> listAll() {
        List<SysRole> roles = roleService.listAllEnabled();
        return Result.success("获取角色列表成功", roles);
    }
    
    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        SysRole role = roleService.getById(id);
        if (role == null) {
            return Result.error("角色不存在");
        }
        return Result.success("获取角色详情成功", role);
    }
    
    /**
     * 创建角色
     */
    @PostMapping
    @OperationLog(module = "角色管理", type = OperationType.CREATE, description = "创建角色")
    public Result<Boolean> create(@RequestBody SysRole role) {
        boolean success = roleService.createRole(role);
        if (success) {
            return Result.success("创建角色成功", true);
        }
        return Result.error("创建失败，角色编码可能已存在");
    }
    
    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @OperationLog(module = "角色管理", type = OperationType.UPDATE, description = "更新角色")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        boolean success = roleService.updateRole(role);
        if (success) {
            return Result.success("更新角色成功", true);
        }
        return Result.error("更新失败");
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "角色管理", type = OperationType.DELETE, description = "删除角色")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = roleService.deleteRole(id);
        if (success) {
            return Result.success("删除角色成功", true);
        }
        return Result.error("删除失败");
    }
    
    /**
     * 获取角色的菜单权限ID列表
     */
    @GetMapping("/{id}/menus")
    public Result<List<Long>> getRoleMenus(@PathVariable Long id) {
        List<Long> menuIds = menuService.getMenuIdsByRoleId(id);
        return Result.success("获取角色菜单成功", menuIds);
    }
    
    /**
     * 为角色分配菜单权限
     */
    @PostMapping("/{id}/menus")
    @OperationLog(module = "角色管理", type = OperationType.UPDATE, description = "分配角色权限")
    public Result<Boolean> assignMenus(
            @PathVariable Long id,
            @RequestBody List<Long> menuIds) {
        boolean success = roleService.assignMenus(id, menuIds);
        if (success) {
            return Result.success("分配权限成功", true);
        }
        return Result.error("分配失败");
    }
    
    /**
     * 获取用户的角色列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<SysRole>> getUserRoles(@PathVariable Long userId) {
        List<SysRole> roles = roleService.getRolesByUserId(userId);
        return Result.success("获取用户角色成功", roles);
    }
    
    /**
     * 为用户分配角色
     */
    @PostMapping("/user/{userId}")
    @OperationLog(module = "角色管理", type = OperationType.UPDATE, description = "为用户分配角色")
    public Result<Boolean> assignUserRoles(
            @PathVariable Long userId,
            @RequestBody List<Long> roleIds) {
        boolean success = roleService.assignRolesToUser(userId, roleIds);
        if (success) {
            return Result.success("分配角色成功", true);
        }
        return Result.error("分配失败");
    }
}
