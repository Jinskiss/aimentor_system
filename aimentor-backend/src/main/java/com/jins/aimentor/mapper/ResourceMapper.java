package com.jins.aimentor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jins.aimentor.domain.entity.Resource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习资源Mapper接口
 *
 * <p>继承BaseMapper获得基础CRUD和分页能力</p>
 * <p>分页查询使用BaseMapper的selectPage方法配合Page对象</p>
 *
 * <p>使用示例：</p>
 * <pre>
 * Page<Resource> page = new Page<>(current, size);
 * QueryWrapper<Resource> wrapper = new QueryWrapper<>();
 * wrapper.orderByDesc("recommend_score");
 * Page<Resource> result = resourceMapper.selectPage(page, wrapper);
 * </pre>
 *
 * @author Education Team
 * @since 1.0.0
 */
@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {
    // 基础CRUD和分页由BaseMapper提供
    // 如需复杂查询可在此添加自定义方法
}