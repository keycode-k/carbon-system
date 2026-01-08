package com.example.provider.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.provider.entity.SysDictData;
import com.example.provider.entity.SysDictType;
import com.example.provider.mapper.SysDictDataMapper;
import com.example.provider.mapper.SysDictTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 字典管理Service
 */
@Service
public class SysDictService extends ServiceImpl<SysDictTypeMapper, SysDictType> {
    
    @Autowired
    private SysDictTypeMapper dictTypeMapper;
    
    @Autowired
    private SysDictDataMapper dictDataMapper;
    
    // ========== 字典类型管理 ==========
    
    /**
     * 分页查询字典类型
     */
    public Page<SysDictType> pageDictType(String dictName, String dictType, Integer status, int page, int size) {
        LambdaQueryWrapper<SysDictType> query = new LambdaQueryWrapper<>();
        
        if (dictName != null && !dictName.isEmpty()) {
            query.like(SysDictType::getDictName, dictName);
        }
        if (dictType != null && !dictType.isEmpty()) {
            query.like(SysDictType::getDictType, dictType);
        }
        if (status != null) {
            query.eq(SysDictType::getStatus, status);
        }
        query.orderByDesc(SysDictType::getCreateTime);
        
        return dictTypeMapper.selectPage(new Page<>(page, size), query);
    }
    
    /**
     * 获取所有字典类型
     */
    public List<SysDictType> listAllDictTypes() {
        LambdaQueryWrapper<SysDictType> query = new LambdaQueryWrapper<>();
        query.eq(SysDictType::getStatus, 1);
        query.orderByAsc(SysDictType::getId);
        return dictTypeMapper.selectList(query);
    }
    
    /**
     * 创建字典类型
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createDictType(SysDictType dictType) {
        // 检查字典类型是否已存在
        LambdaQueryWrapper<SysDictType> query = new LambdaQueryWrapper<>();
        query.eq(SysDictType::getDictType, dictType.getDictType());
        if (dictTypeMapper.selectCount(query) > 0) {
            return false;
        }
        
        dictType.setCreateTime(LocalDateTime.now());
        dictType.setUpdateTime(LocalDateTime.now());
        if (dictType.getStatus() == null) {
            dictType.setStatus(1);
        }
        return dictTypeMapper.insert(dictType) > 0;
    }
    
    /**
     * 更新字典类型
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictType(SysDictType dictType) {
        dictType.setUpdateTime(LocalDateTime.now());
        return dictTypeMapper.updateById(dictType) > 0;
    }
    
    /**
     * 删除字典类型（同时删除关联的字典数据）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDictType(Long id) {
        SysDictType dictType = dictTypeMapper.selectById(id);
        if (dictType == null) {
            return false;
        }
        
        // 删除关联的字典数据
        LambdaQueryWrapper<SysDictData> dataQuery = new LambdaQueryWrapper<>();
        dataQuery.eq(SysDictData::getDictType, dictType.getDictType());
        dictDataMapper.delete(dataQuery);
        
        // 删除字典类型
        return dictTypeMapper.deleteById(id) > 0;
    }
    
    // ========== 字典数据管理 ==========
    
    /**
     * 根据字典类型获取字典数据列表
     */
    public List<SysDictData> listDictDataByType(String dictType) {
        return dictDataMapper.selectByDictType(dictType);
    }
    
    /**
     * 分页查询字典数据
     */
    public Page<SysDictData> pageDictData(String dictType, String dictLabel, Integer status, int page, int size) {
        LambdaQueryWrapper<SysDictData> query = new LambdaQueryWrapper<>();
        
        if (dictType != null && !dictType.isEmpty()) {
            query.eq(SysDictData::getDictType, dictType);
        }
        if (dictLabel != null && !dictLabel.isEmpty()) {
            query.like(SysDictData::getDictLabel, dictLabel);
        }
        if (status != null) {
            query.eq(SysDictData::getStatus, status);
        }
        query.orderByAsc(SysDictData::getDictSort);
        
        return dictDataMapper.selectPage(new Page<>(page, size), query);
    }
    
    /**
     * 创建字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createDictData(SysDictData dictData) {
        dictData.setCreateTime(LocalDateTime.now());
        dictData.setUpdateTime(LocalDateTime.now());
        if (dictData.getStatus() == null) {
            dictData.setStatus(1);
        }
        if (dictData.getDictSort() == null) {
            dictData.setDictSort(0);
        }
        if (dictData.getIsDefault() == null) {
            dictData.setIsDefault(0);
        }
        return dictDataMapper.insert(dictData) > 0;
    }
    
    /**
     * 更新字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictData(SysDictData dictData) {
        dictData.setUpdateTime(LocalDateTime.now());
        return dictDataMapper.updateById(dictData) > 0;
    }
    
    /**
     * 删除字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDictData(Long id) {
        return dictDataMapper.deleteById(id) > 0;
    }
    
    /**
     * 获取字典数据详情
     */
    public SysDictData getDictDataById(Long id) {
        return dictDataMapper.selectById(id);
    }
}
