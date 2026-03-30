package com.jins.aimentor.service;

import com.jins.aimentor.domain.dto.KnowledgeMasteryDto;
import com.jins.aimentor.domain.dto.ScoreRecordDto;
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

    /**
     * 添加成绩记录
     *
     * @param studentId 学生ID
     * @param dto 成绩记录信息
     * @return 是否添加成功
     */
    boolean addScoreRecord(Long studentId, ScoreRecordDto dto);

    /**
     * 添加知识点掌握度记录
     *
     * @param studentId 学生ID
     * @param dto 知识点掌握度信息
     * @return 是否添加成功
     */
    boolean addKnowledgeMastery(Long studentId, KnowledgeMasteryDto dto);

    /**
     * 获取学生的所有成绩记录
     *
     * @param studentId 学生ID
     * @return 成绩记录列表
     */
    List<com.jins.aimentor.domain.entity.ScoreRecord> getScoreRecords(Long studentId);

    /**
     * 获取学生的所有知识点掌握度记录
     *
     * @param studentId 学生ID
     * @return 知识点掌握度列表
     */
    List<com.jins.aimentor.domain.entity.KnowledgeMastery> getKnowledgeMasteries(Long studentId);
}