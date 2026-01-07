package com.example.development.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.development.entity.Methodology;
import com.example.development.mapper.MethodologyMapper;
import com.example.development.service.MethodologyService;
import org.springframework.stereotype.Service;

/**
 * 方法学服务实现类
 */
@Service
public class MethodologyServiceImpl extends ServiceImpl<MethodologyMapper, Methodology> implements MethodologyService {
}
