package com.jins.aimentor.service;

import com.jins.aimentor.domain.entity.Resource;
import com.jins.aimentor.domain.vo.ResourceVO;

import java.util.List;

public interface ResourceService {

    /**
     * 获取推荐资源列表
     *
     * @return 按推荐分数降序排列的资源列表
     */
    List<ResourceVO> getRecommendResources();

    /**
     * 根据科目筛选资源
     *
     * @param subject 科目名称
     * @return 该科目下的资源列表
     */
    List<ResourceVO> getResourcesBySubject(String subject);

    /**
     * 根据类型筛选资源
     *
     * @param type 资源类型（视频/文档/练习/音频）
     * @return 该类型的资源列表
     */
    List<ResourceVO> getResourcesByType(String type);

    /**
     * 按 ID 获取单条资源（详情页）
     */
    ResourceVO getResourceById(Long id);

    /**
     * 获取所有资源列表
     */
    List<Resource> listAll();

    /**
     * 更新资源
     */
    boolean updateById(Resource resource);

    /**
     * 新增资源
     */
    boolean saveResource(Resource resource);

    /**
     * 删除资源
     */
    boolean deleteResource(Long id);
}
