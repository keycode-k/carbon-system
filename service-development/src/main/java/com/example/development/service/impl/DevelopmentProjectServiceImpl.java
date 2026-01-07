package com.example.development.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.development.entity.DevelopmentProject;
import com.example.development.mapper.DevelopmentProjectMapper;
import com.example.development.service.DevelopmentProjectService;
import org.springframework.stereotype.Service;

@Service
public class DevelopmentProjectServiceImpl extends ServiceImpl<DevelopmentProjectMapper, DevelopmentProject> implements DevelopmentProjectService {
}
