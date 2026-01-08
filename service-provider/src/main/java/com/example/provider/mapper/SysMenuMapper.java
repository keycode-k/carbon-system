package com.example.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.provider.entity.SysMenu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单Mapper接口
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    
    /**
     * 根据角色ID查询菜单列表
     */
    @Select("SELECT m.* FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id = #{roleId} AND m.status = 1 " +
            "ORDER BY m.order_num ASC")
    List<SysMenu> selectMenusByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据用户ID查询菜单列表
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "LEFT JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 " +
            "ORDER BY m.order_num ASC")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);
    
    /**
     * 查询所有菜单（用于管理员）
     */
    @Select("SELECT * FROM sys_menu WHERE status = 1 ORDER BY order_num ASC")
    List<SysMenu> selectAllMenus();
    
    /**
     * 根据用户ID查询权限标识列表
     */
    @Select("SELECT DISTINCT m.permission FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "LEFT JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 AND m.permission IS NOT NULL")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);
}
