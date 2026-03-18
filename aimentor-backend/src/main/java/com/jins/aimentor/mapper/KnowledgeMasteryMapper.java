package com.jins.aimentor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jins.aimentor.domain.entity.KnowledgeMastery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 知识点掌握度Mapper接口
 *
 * <p>继承BaseMapper获得基础CRUD能力</p>
 *
 * @author Education Team
 * @since 1.0.0
 */
@Mapper
public interface KnowledgeMasteryMapper extends BaseMapper<KnowledgeMastery> {

    /**
     * 查询学生的薄弱知识点
     *
     * <p>筛选掌握度低于60%的知识点</p>
     * <p>按掌握度升序排列（最薄弱的在前）</p>
     * <p>限制返回10条</p>
     *
     * @param studentId 学生ID
     * @return 薄弱知识点列表
     */
    @Select("SELECT * FROM knowledge_mastery " +
            "WHERE student_id = #{studentId} AND mastery < 60 " +
            "ORDER BY mastery ASC " +
            "LIMIT 10")
    List<KnowledgeMastery> selectWeakPointsByStudentId(@Param("studentId") Long studentId);
}