package com.example.provider.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.provider.entity.SysMenu;
import com.example.provider.mapper.SysMenuMapper;
import com.example.provider.mapper.SysRoleMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单管理Service
 */
@Service
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> {
    
    @Autowired
    private SysMenuMapper menuMapper;
    
    @Autowired
    private SysRoleMenuMapper roleMenuMapper;
    
    /**
     * 获取所有菜单（树形结构）
     */
    public List<SysMenu> listAllMenuTree() {
        List<SysMenu> allMenus = menuMapper.selectAllMenus();
        return buildMenuTree(allMenus, 0L);
    }
    
    /**
     * 获取所有菜单（平铺结构）
     */
    public List<SysMenu> listAllMenus() {
        return menuMapper.selectAllMenus();
    }
    
    /**
     * 根据用户ID获取菜单列表（树形结构）
     */
    public List<SysMenu> getMenuTreeByUserId(Long userId) {
        List<SysMenu> userMenus = menuMapper.selectMenusByUserId(userId);
        return buildMenuTree(userMenus, 0L);
    }
    
    /**
     * 根据角色ID获取菜单ID列表
     */
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        List<SysMenu> menus = menuMapper.selectMenusByRoleId(roleId);
        return menus.stream().map(SysMenu::getId).collect(Collectors.toList());
    }
    
    /**
     * 根据用户ID获取权限标识列表
     */
    public List<String> getPermissionsByUserId(Long userId) {
        return menuMapper.selectPermissionsByUserId(userId);
    }
    
    /**
     * 根据用户ID获取权限标识集合（别名方法，返回Set）
     */
    public Set<String> getUserPermissions(Long userId) {
        List<String> permissions = getPermissionsByUserId(userId);
        return new HashSet<>(permissions);
    }
    
    /**
     * 创建菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createMenu(SysMenu menu) {
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
        if (menu.getVisible() == null) {
            menu.setVisible(1);
        }
        if (menu.getOrderNum() == null) {
            menu.setOrderNum(0);
        }
        return menuMapper.insert(menu) > 0;
    }
    
    /**
     * 更新菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMenu(SysMenu menu) {
        menu.setUpdateTime(LocalDateTime.now());
        return menuMapper.updateById(menu) > 0;
    }
    
    /**
     * 删除菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMenu(Long menuId) {
        // 检查是否有子菜单
        LambdaQueryWrapper<SysMenu> query = new LambdaQueryWrapper<>();
        query.eq(SysMenu::getParentId, menuId);
        if (menuMapper.selectCount(query) > 0) {
            return false; // 有子菜单不能删除
        }
        
        // 删除角色-菜单关联
        roleMenuMapper.deleteByMenuId(menuId);
        // 删除菜单
        return menuMapper.deleteById(menuId) > 0;
    }
    
    /**
     * 构建菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus, Long parentId) {
        List<SysMenu> tree = new ArrayList<>();
        
        for (SysMenu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                List<SysMenu> children = buildMenuTree(menus, menu.getId());
                menu.setChildren(children.isEmpty() ? null : children);
                tree.add(menu);
            }
        }
        
        return tree;
    }
    
    /**
     * 获取用户的路由菜单（仅目录和菜单类型，不含按钮）
     */
    public List<SysMenu> getRouterMenusByUserId(Long userId) {
        List<SysMenu> userMenus = menuMapper.selectMenusByUserId(userId);
        // 过滤掉按钮类型
        List<SysMenu> routerMenus = userMenus.stream()
                .filter(m -> !"F".equals(m.getMenuType()))
                .collect(Collectors.toList());
        return buildMenuTree(routerMenus, 0L);
    }
}
