package com.jins.aimentor.service;

import com.jins.aimentor.domain.dto.KnowledgeMasteryDto;
import com.jins.aimentor.domain.dto.ScoreRecordDto;
import com.jins.aimentor.domain.entity.KnowledgeMastery;
import com.jins.aimentor.domain.entity.ScoreRecord;
import com.jins.aimentor.domain.vo.ScoreTrendVO;
import com.jins.aimentor.domain.vo.WeakPointVO;

import java.util.List;

/**
 * AcademicService - 学情分析服务接口
 *
 * <p>服务职责：
 * <ul>
 *   <li>成绩趋势统计：按月聚合学生平均成绩，供 Dashboard 折线图展示</li>
 *   <li>薄弱知识点识别：返回掌握度低于 60% 的知识点，帮助学生针对性复习</li>
 *   <li>成绩记录管理（CRUD）：学生可添加、修改、删除自己的成绩记录</li>
 *   <li>知识点掌握度管理（CRUD）：学生可添加、修改、删除自己的知识点掌握度</li>
 * </ul>
 *
 * <p>安全约束：
 * 所有接口均要求传入 studentId（从 UserHolder 获取当前登录用户），且在执行前校验归属关系，
 * 防止学生越权访问/修改/删除他人的数据。
 */
public interface AcademicService {

    // ==================== 成绩趋势查询 ====================

    /**
     * 获取指定学生的成绩趋势
     *
     * @param studentId 学生ID（关联 user 表）
     * @return 成绩趋势列表，含月份(dateMonth)和平均分(avgScore)，按时间升序排列
     */
    List<ScoreTrendVO> getScoreTrend(Long studentId);

    /**
     * 获取指定学生的薄弱知识点（掌握度低于 60%）
     *
     * @param studentId 学生ID
     * @return 薄弱知识点列表，按掌握度从低到高排序
     */
    List<WeakPointVO> getWeakPoints(Long studentId);

    // ==================== 成绩记录 CRUD ====================

    /**
     * 添加或更新成绩记录
     * <p>判断逻辑：dto.id != null 时执行更新，否则执行插入。</p>
     *
     * @param studentId 学生ID（归属到当前登录用户，编辑时也用于越权校验）
     * @param dto 成绩记录数据（id/examDate/subject/score/fullScore）
     * @return 操作是否成功
     */
    boolean addScoreRecord(Long studentId, ScoreRecordDto dto);

    /**
     * 删除指定的成绩记录
     *
     * @param recordId  成绩记录ID
     * @param studentId 当前登录学生ID（用于归属校验）
     * @return 操作是否成功
     */
    boolean deleteScoreRecord(Long recordId, Long studentId);

    /**
     * 获取指定学生的所有成绩记录（用于 RecordManage 页面表格）
     *
     * @param studentId 学生ID
     * @return 成绩记录列表，按考试日期降序
     */
    List<ScoreRecord> getScoreRecords(Long studentId);

    // ==================== 知识点掌握度 CRUD ====================

    /**
     * 添加或更新知识点掌握度记录
     * <p>判断逻辑：dto.id != null 时执行更新，否则执行插入。</p>
     *
     * @param studentId 学生ID
     * @param dto 知识点掌握度数据（id/knowledge/mastery/subject）
     * @return 操作是否成功
     */
    boolean addKnowledgeMastery(Long studentId, KnowledgeMasteryDto dto);

    /**
     * 删除指定的知识点掌握度记录
     *
     * @param recordId  知识点记录ID
     * @param studentId 当前登录学生ID（用于归属校验）
     * @return 操作是否成功
     */
    boolean deleteKnowledgeMastery(Long recordId, Long studentId);

    /**
     * 获取指定学生的所有知识点掌握度记录（用于 RecordManage 页面表格）
     *
     * @param studentId 学生ID
     * @return 知识点掌握度列表，按掌握度升序（薄弱优先）
     */
    List<KnowledgeMastery> getKnowledgeMasteries(Long studentId);
}
