package com.example.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.provider.entity.CompanyInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业信息Mapper接口
 */
@Mapper
public interface CompanyInfoMapper extends BaseMapper<CompanyInfo> {
}
