package com.jins.aimentor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jins.aimentor.domain.entity.Resource;
import com.jins.aimentor.domain.vo.ResourceVO;
import com.jins.aimentor.mapper.ResourceMapper;
import com.jins.aimentor.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

    private static final Logger log = LogManager.getLogger(ResourceServiceImpl.class);

    @Override
    public List<ResourceVO> getRecommendResources() {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Resource::getRecommendScore);
        List<Resource> list = list(wrapper);
        log.info("[ResourceService] 获取推荐资源，共 {} 条", list.size());
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<ResourceVO> getResourcesBySubject(String subject) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getSubject, subject)
               .orderByDesc(Resource::getRecommendScore);
        List<Resource> list = list(wrapper);
        log.info("[ResourceService] 按科目筛选资源，subject={}，共 {} 条", subject, list.size());
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<ResourceVO> getResourcesByType(String type) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resource::getType, type)
               .orderByDesc(Resource::getRecommendScore);
        List<Resource> list = list(wrapper);
        log.info("[ResourceService] 按类型筛选资源，type={}，共 {} 条", type, list.size());
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ResourceVO getResourceById(Long id) {
        if (id == null) {
            return null;
        }
        Resource entity = getById(id);
        if (entity == null) {
            return null;
        }
        return toVO(entity);
    }

    @Override
    public List<Resource> listAll() {
        return list();
    }

    @Override
    public boolean updateById(Resource resource) {
        return baseMapper.updateById(resource) > 0;
    }

    @Override
    public boolean saveResource(Resource resource) {
        log.info("[ResourceService] 新增资源，title={}", resource.getTitle());
        return save(resource);
    }

    @Override
    public boolean deleteResource(Long id) {
        log.info("[ResourceService] 删除资源，id={}", id);
        return removeById(id);
    }

    private ResourceVO toVO(Resource entity) {
        ResourceVO vo = new ResourceVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setDescription(entity.getDescription());
        vo.setType(entity.getType());
        vo.setUrl(entity.getUrl());
        vo.setCoverImage(entity.getCoverImage());
        vo.setSubject(entity.getSubject());
        vo.setRecommendScore(entity.getRecommendScore());
        return vo;
    }
}
