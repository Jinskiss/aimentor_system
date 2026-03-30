package com.jins.aimentor.service;

import com.jins.aimentor.domain.vo.ScoreTrendVO;
import com.jins.aimentor.domain.vo.WeakPointVO;

import java.util.List;

public interface AcademicService {

    /**
     * 获取学生成绩趋势
     * <p>按月统计学生的平均成绩，展示成绩变化趋势</p>
     *
     * @param studentId 学生ID，关联user表
     * @return 成绩趋势列表，包含月份和平均分数，按时间升序排列
     */
    List<ScoreTrendVO> getScoreTrend(Long studentId);

    /**
     * 获取学生薄弱知识点
     * <p>查询掌握度低于阈值的知识点，帮助学生针对性复习</p>
     *
     * @param studentId 学生ID，关联user表
     * @return 薄弱知识点列表，按掌握度从低到高排序
     */
    List<WeakPointVO> getWeakPoints(Long studentId);
}