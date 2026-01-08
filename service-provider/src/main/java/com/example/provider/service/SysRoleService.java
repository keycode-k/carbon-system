package com.example.provider.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.provider.entity.SysRole;
import com.example.provider.entity.SysRoleMenu;
import com.example.provider.entity.SysUserRole;
import com.example.provider.mapper.SysRoleMapper;
import com.example.provider.mapper.SysRoleMenuMapper;
import com.example.provider.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色管理Service
 */
@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {
    
    @Autowired
    private SysRoleMapper roleMapper;
    
    @Autowired
    private SysRoleMenuMapper roleMenuMapper;
    
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    
    /**
     * 分页查询角色列表
     */
    public Page<SysRole> pageList(String roleName, Integer status, int page, int size) {
        LambdaQueryWrapper<SysRole> query = new LambdaQueryWrapper<>();
        
        if (roleName != null && !roleName.isEmpty()) {
            query.like(SysRole::getRoleName, roleName);
        }
        if (status != null) {
            query.eq(SysRole::getStatus, status);
        }
        query.orderByAsc(SysRole::getId);
        
        return roleMapper.selectPage(new Page<>(page, size), query);
    }
    
    /**
     * 获取所有启用的角色
     */
    public List<SysRole> listAllEnabled() {
        return roleMapper.selectAllEnabledRoles();
    }
    
    /**
     * 根据用户ID获取角色列表
     */
    public List<SysRole> getRolesByUserId(Long userId) {
        return roleMapper.selectRolesByUserId(userId);
    }
    
    /**
     * 获取用户的角色列表（别名方法）
     */
    public List<SysRole> getUserRoles(Long userId) {
        return getRolesByUserId(userId);
    }
    
    /**
     * 创建角色
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createRole(SysRole role) {
        // 检查角色编码是否已存在
        LambdaQueryWrapper<SysRole> query = new LambdaQueryWrapper<>();
        query.eq(SysRole::getRoleCode, role.getRoleCode());
        if (roleMapper.selectCount(query) > 0) {
            return false;
        }
        
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        return roleMapper.insert(role) > 0;
    }
    
    /**
     * 更新角色
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRole role) {
        role.setUpdateTime(LocalDateTime.now());
        return roleMapper.updateById(role) > 0;
    }
    
    /**
     * 删除角色
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long roleId) {
        // 删除角色-菜单关联
        roleMenuMapper.deleteByRoleId(roleId);
        // 删除用户-角色关联
        userRoleMapper.deleteByRoleId(roleId);
        // 删除角色
        return roleMapper.deleteById(roleId) > 0;
    }
    
    /**
     * 为角色分配菜单权限
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMenus(Long roleId, List<Long> menuIds) {
        // 先删除原有关联
        roleMenuMapper.deleteByRoleId(roleId);
        
        // 添加新关联
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }
        return true;
    }
    
    /**
     * 为用户分配角色
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRolesToUser(Long userId, List<Long> roleIds) {
        // 先删除原有关联
        userRoleMapper.deleteByUserId(userId);
        
        // 添加新关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
        return true;
    }
}
