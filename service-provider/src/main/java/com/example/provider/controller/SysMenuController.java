package com.example.provider.controller;

import com.example.common.model.Result;
import com.example.provider.entity.SysMenu;
import com.example.provider.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理Controller
 */
@RestController
@RequestMapping("/api/system/menu")
public class SysMenuController {
    
    @Autowired
    private SysMenuService menuService;
    
    /**
     * 获取菜单树形列表（管理用）
     */
    @GetMapping("/tree")
    public Result<List<SysMenu>> getMenuTree() {
        List<SysMenu> tree = menuService.listAllMenuTree();
        return Result.success("获取菜单树成功", tree);
    }
    
    /**
     * 获取所有菜单（平铺列表）
     */
    @GetMapping("/list")
    public Result<List<SysMenu>> list() {
        List<SysMenu> menus = menuService.listAllMenus();
        return Result.success("获取菜单列表成功", menus);
    }
    
    /**
     * 获取菜单详情
     */
    @GetMapping("/{id}")
    public Result<SysMenu> getById(@PathVariable Long id) {
        SysMenu menu = menuService.getById(id);
        if (menu == null) {
            return Result.error("菜单不存在");
        }
        return Result.success("获取菜单详情成功", menu);
    }
    
    /**
     * 创建菜单
     */
    @PostMapping
    public Result<Boolean> create(@RequestBody SysMenu menu) {
        boolean success = menuService.createMenu(menu);
        if (success) {
            return Result.success("创建菜单成功", true);
        }
        return Result.error("创建失败");
    }
    
    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody SysMenu menu) {
        menu.setId(id);
        boolean success = menuService.updateMenu(menu);
        if (success) {
            return Result.success("更新菜单成功", true);
        }
        return Result.error("更新失败");
    }
    
    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = menuService.deleteMenu(id);
        if (success) {
            return Result.success("删除菜单成功", true);
        }
        return Result.error("删除失败，可能存在子菜单");
    }
    
    /**
     * 获取当前用户的路由菜单（用于前端动态路由）
     */
    @GetMapping("/router/{userId}")
    public Result<List<SysMenu>> getRouterMenus(@PathVariable Long userId) {
        List<SysMenu> menus = menuService.getRouterMenusByUserId(userId);
        return Result.success("获取路由菜单成功", menus);
    }
    
    /**
     * 获取当前用户的权限标识列表
     */
    @GetMapping("/permissions/{userId}")
    public Result<List<String>> getUserPermissions(@PathVariable Long userId) {
        List<String> permissions = menuService.getPermissionsByUserId(userId);
        return Result.success("获取权限标识成功", permissions);
    }
}
