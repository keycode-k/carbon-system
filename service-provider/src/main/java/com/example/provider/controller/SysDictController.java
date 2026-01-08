package com.example.provider.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.model.Result;
import com.example.provider.entity.SysDictData;
import com.example.provider.entity.SysDictType;
import com.example.provider.service.SysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理Controller
 */
@RestController
@RequestMapping("/api/system/dict")
public class SysDictController {
    
    @Autowired
    private SysDictService dictService;
    
    // ========== 字典类型接口 ==========
    
    /**
     * 分页查询字典类型
     */
    @GetMapping("/type/list")
    public Result<Page<SysDictType>> listDictType(
            @RequestParam(required = false) String dictName,
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysDictType> result = dictService.pageDictType(dictName, dictType, status, page, size);
        return Result.success("获取字典类型列表成功", result);
    }
    
    /**
     * 获取所有字典类型（用于下拉选择）
     */
    @GetMapping("/type/all")
    public Result<List<SysDictType>> listAllDictTypes() {
        List<SysDictType> types = dictService.listAllDictTypes();
        return Result.success("获取字典类型成功", types);
    }
    
    /**
     * 获取字典类型详情
     */
    @GetMapping("/type/{id}")
    public Result<SysDictType> getDictType(@PathVariable Long id) {
        SysDictType dictType = dictService.getById(id);
        if (dictType == null) {
            return Result.error("字典类型不存在");
        }
        return Result.success("获取字典类型成功", dictType);
    }
    
    /**
     * 创建字典类型
     */
    @PostMapping("/type")
    public Result<Boolean> createDictType(@RequestBody SysDictType dictType) {
        boolean success = dictService.createDictType(dictType);
        if (success) {
            return Result.success("创建字典类型成功", true);
        }
        return Result.error("创建失败，字典类型可能已存在");
    }
    
    /**
     * 更新字典类型
     */
    @PutMapping("/type/{id}")
    public Result<Boolean> updateDictType(@PathVariable Long id, @RequestBody SysDictType dictType) {
        dictType.setId(id);
        boolean success = dictService.updateDictType(dictType);
        if (success) {
            return Result.success("更新字典类型成功", true);
        }
        return Result.error("更新失败");
    }
    
    /**
     * 删除字典类型
     */
    @DeleteMapping("/type/{id}")
    public Result<Boolean> deleteDictType(@PathVariable Long id) {
        boolean success = dictService.deleteDictType(id);
        if (success) {
            return Result.success("删除字典类型成功", true);
        }
        return Result.error("删除失败");
    }
    
    // ========== 字典数据接口 ==========
    
    /**
     * 根据字典类型获取字典数据（常用接口，用于前端下拉框）
     */
    @GetMapping("/data/type/{dictType}")
    public Result<List<SysDictData>> getDictDataByType(@PathVariable String dictType) {
        List<SysDictData> dataList = dictService.listDictDataByType(dictType);
        return Result.success("获取字典数据成功", dataList);
    }
    
    /**
     * 分页查询字典数据
     */
    @GetMapping("/data/list")
    public Result<Page<SysDictData>> listDictData(
            @RequestParam(required = false) String dictType,
            @RequestParam(required = false) String dictLabel,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SysDictData> result = dictService.pageDictData(dictType, dictLabel, status, page, size);
        return Result.success("获取字典数据列表成功", result);
    }
    
    /**
     * 获取字典数据详情
     */
    @GetMapping("/data/{id}")
    public Result<SysDictData> getDictData(@PathVariable Long id) {
        SysDictData dictData = dictService.getDictDataById(id);
        if (dictData == null) {
            return Result.error("字典数据不存在");
        }
        return Result.success("获取字典数据成功", dictData);
    }
    
    /**
     * 创建字典数据
     */
    @PostMapping("/data")
    public Result<Boolean> createDictData(@RequestBody SysDictData dictData) {
        boolean success = dictService.createDictData(dictData);
        if (success) {
            return Result.success("创建字典数据成功", true);
        }
        return Result.error("创建失败");
    }
    
    /**
     * 更新字典数据
     */
    @PutMapping("/data/{id}")
    public Result<Boolean> updateDictData(@PathVariable Long id, @RequestBody SysDictData dictData) {
        dictData.setId(id);
        boolean success = dictService.updateDictData(dictData);
        if (success) {
            return Result.success("更新字典数据成功", true);
        }
        return Result.error("更新失败");
    }
    
    /**
     * 删除字典数据
     */
    @DeleteMapping("/data/{id}")
    public Result<Boolean> deleteDictData(@PathVariable Long id) {
        boolean success = dictService.deleteDictData(id);
        if (success) {
            return Result.success("删除字典数据成功", true);
        }
        return Result.error("删除失败");
    }
}
