package com.jins.aimentor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jins.aimentor.domain.entity.ScoreRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 成绩记录Mapper接口
 *
 * <p>继承BaseMapper获得基础CRUD能力</p>
 * <p>复杂统计查询使用自定义SQL</p>
 *
 * @author Education Team
 * @since 1.0.0
 */
@Mapper
public interface ScoreRecordMapper extends BaseMapper<ScoreRecord> {

    /**
     * 查询学生的成绩趋势
     *
     * <p>按月份分组统计平均分</p>
     * <p>返回结果使用@TableField(exist=false)的临时字段接收</p>
     *
     * @param studentId 学生ID
     * @return 成绩趋势列表（包含dateMonth和avgScore字段）
     */
    @Select("SELECT " +
            "  DATE_FORMAT(exam_date, '%Y-%m') as date_month, " +
            "  ROUND(AVG(score), 0) as avg_score, " +
            "  0 as id, " +  // 填充必要字段避免映射错误
            "  #{studentId} as student_id " +
            "FROM score_record " +
            "WHERE student_id = #{studentId} " +
            "GROUP BY DATE_FORMAT(exam_date, '%Y-%m') " +
            "ORDER BY date_month")
    List<ScoreRecord> selectTrendByStudentId(@Param("studentId") Long studentId);
}