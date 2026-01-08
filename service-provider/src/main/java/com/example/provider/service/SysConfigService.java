package com.example.provider.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.provider.entity.SysConfig;
import com.example.provider.mapper.SysConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 系统配置服务类
 */
@Service
public class SysConfigService {
    
    private static final String CONFIG_CACHE_PREFIX = "sys_config:";
    private static final long CACHE_EXPIRE_HOURS = 24;
    
    @Autowired
    private SysConfigMapper configMapper;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 分页查询配置列表
     */
    public Page<SysConfig> pageList(String configName, String configKey, String configGroup,
                                     Integer configType, int page, int size) {
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        
        if (configName != null && !configName.isEmpty()) {
            queryWrapper.like(SysConfig::getConfigName, configName);
        }
        if (configKey != null && !configKey.isEmpty()) {
            queryWrapper.like(SysConfig::getConfigKey, configKey);
        }
        if (configGroup != null && !configGroup.isEmpty()) {
            queryWrapper.eq(SysConfig::getConfigGroup, configGroup);
        }
        if (configType != null) {
            queryWrapper.eq(SysConfig::getConfigType, configType);
        }
        
        queryWrapper.orderByAsc(SysConfig::getConfigGroup)
                   .orderByAsc(SysConfig::getId);
        
        return configMapper.selectPage(new Page<>(page, size), queryWrapper);
    }
    
    /**
     * 根据ID查询配置
     */
    public SysConfig getById(Long id) {
        return configMapper.selectById(id);
    }
    
    /**
     * 根据配置键名查询配置值
     * 优先从缓存获取
     */
    public String getConfigValue(String configKey) {
        String cacheKey = CONFIG_CACHE_PREFIX + configKey;
        
        // 尝试从缓存获取
        String value = redisTemplate.opsForValue().get(cacheKey);
        if (value != null) {
            return value;
        }
        
        // 从数据库查询
        value = configMapper.selectConfigValueByKey(configKey);
        if (value != null) {
            // 写入缓存
            redisTemplate.opsForValue().set(cacheKey, value, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }
        
        return value;
    }
    
    /**
     * 根据配置键名查询配置值（带默认值）
     */
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 根据配置键名查询配置（布尔值）
     */
    public boolean getConfigBoolean(String configKey, boolean defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }
    
    /**
     * 根据配置键名查询配置（整数值）
     */
    public int getConfigInt(String configKey, int defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 按分组获取配置列表
     */
    public List<SysConfig> listByGroup(String configGroup) {
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysConfig::getConfigGroup, configGroup)
                   .eq(SysConfig::getStatus, 1)
                   .orderByAsc(SysConfig::getId);
        return configMapper.selectList(queryWrapper);
    }
    
    /**
     * 获取所有配置分组
     */
    public List<String> listGroups() {
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SysConfig::getConfigGroup)
                   .groupBy(SysConfig::getConfigGroup)
                   .orderByAsc(SysConfig::getConfigGroup);
        return configMapper.selectList(queryWrapper)
                .stream()
                .map(SysConfig::getConfigGroup)
                .distinct()
                .toList();
    }
    
    /**
     * 新增配置
     */
    public boolean create(SysConfig config) {
        // 检查配置键是否已存在
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysConfig::getConfigKey, config.getConfigKey());
        if (configMapper.selectCount(queryWrapper) > 0) {
            return false;
        }
        
        config.setCreateTime(LocalDateTime.now());
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        if (config.getConfigType() == null) {
            config.setConfigType(2); // 默认自定义类型
        }
        
        return configMapper.insert(config) > 0;
    }
    
    /**
     * 更新配置
     */
    public boolean update(SysConfig config) {
        SysConfig existing = configMapper.selectById(config.getId());
        if (existing == null) {
            return false;
        }
        
        // 系统内置配置只能修改值，不能修改键名
        if (existing.getConfigType() == 1 && config.getConfigKey() != null 
                && !existing.getConfigKey().equals(config.getConfigKey())) {
            return false;
        }
        
        config.setUpdateTime(LocalDateTime.now());
        int result = configMapper.updateById(config);
        
        if (result > 0) {
            // 清除缓存
            clearCache(existing.getConfigKey());
            if (config.getConfigKey() != null && !config.getConfigKey().equals(existing.getConfigKey())) {
                clearCache(config.getConfigKey());
            }
        }
        
        return result > 0;
    }
    
    /**
     * 删除配置
     */
    public boolean delete(Long id) {
        SysConfig config = configMapper.selectById(id);
        if (config == null) {
            return false;
        }
        
        // 系统内置配置不允许删除
        if (config.getConfigType() == 1) {
            return false;
        }
        
        int result = configMapper.deleteById(id);
        if (result > 0) {
            clearCache(config.getConfigKey());
        }
        return result > 0;
    }
    
    /**
     * 批量删除配置
     */
    public int batchDelete(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            if (delete(id)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 刷新缓存
     */
    public void refreshCache() {
        // 获取所有配置键
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysConfig::getStatus, 1);
        List<SysConfig> configs = configMapper.selectList(queryWrapper);
        
        // 重新加载到缓存
        for (SysConfig config : configs) {
            String cacheKey = CONFIG_CACHE_PREFIX + config.getConfigKey();
            redisTemplate.opsForValue().set(cacheKey, config.getConfigValue(), 
                    CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }
    }
    
    /**
     * 清除指定配置的缓存
     */
    private void clearCache(String configKey) {
        redisTemplate.delete(CONFIG_CACHE_PREFIX + configKey);
    }
}
