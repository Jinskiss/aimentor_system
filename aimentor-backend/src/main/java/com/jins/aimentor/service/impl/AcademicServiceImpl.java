package com.jins.aimentor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.jins.aimentor.domain.dto.KnowledgeMasteryDto;
import com.jins.aimentor.domain.dto.ScoreRecordDto;
import com.jins.aimentor.domain.entity.KnowledgeMastery;
import com.jins.aimentor.domain.entity.ScoreRecord;
import com.jins.aimentor.domain.vo.ScoreTrendVO;
import com.jins.aimentor.domain.vo.WeakPointVO;
import com.jins.aimentor.mapper.KnowledgeMasteryMapper;
import com.jins.aimentor.mapper.ScoreRecordMapper;
import com.jins.aimentor.service.AcademicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AcademicServiceImpl - 学情分析服务实现
 *
 * <p>职责说明：
 * <ul>
 *   <li>成绩趋势统计：按月聚合学生的平均成绩，生成折线图数据</li>
 *   <li>薄弱知识点识别：查询掌握度低于 60% 的知识点，按掌握度升序展示</li>
 *   <li>成绩记录管理：增删改查学生的考试成绩记录</li>
 *   <li>知识点掌握度管理：增删改查学生对各知识点的掌握程度</li>
 * </ul>
 *
 * <p>安全说明：
 * 所有涉及学生数据的接口均需校验 studentId，确保学生只能操作自己的记录。
 * Mapper 层通过 @Param("studentId") 显式传参，防止 SQL 注入。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AcademicServiceImpl implements AcademicService {

    /** 成绩记录 Mapper，提供 CRUD 操作 */
    @Autowired
    private ScoreRecordMapper scoreRecordMapper;

    /** 知识点掌握度 Mapper，提供 CRUD 操作 */
    @Autowired
    private KnowledgeMasteryMapper knowledgeMasteryMapper;

    // ==================== 成绩趋势查询 ====================

    /**
     * 获取指定学生的成绩趋势
     * <p>按月统计该学生的平均成绩，用于 Dashboard 折线图展示。</p>
     *
     * @param studentId 学生ID（从 UserHolder 获取当前登录用户）
     * @return 成绩趋势列表，含月份(dateMonth)和平均分(avgScore)，按时间升序
     */
    @Override
    public List<ScoreTrendVO> getScoreTrend(Long studentId) {
        log.info("[AcademicService] 获取成绩趋势，studentId={}", studentId);

        // 调用 Mapper 中自定义的聚合查询 SQL（按月 GROUP BY）
        List<ScoreRecord> records = scoreRecordMapper.selectTrendByStudentId(studentId);

        if (CollectionUtils.isEmpty(records)) {
            log.info("[AcademicService] 该学生暂无成绩记录，studentId={}", studentId);
            return Collections.emptyList();
        }

        // 将实体列表映射为 VO 列表（过滤掉非聚合字段的脏数据）
        List<ScoreTrendVO> result = records.stream()
                .map(this::convertToScoreTrendVO)
                .collect(Collectors.toList());

        log.info("[AcademicService] 成绩趋势查询完成，共 {} 个月份数据", result.size());
        return result;
    }

    /**
     * 将 ScoreRecord（聚合查询结果）转换为 ScoreTrendVO
     * <p>注意：dateMonth 和 avgScore 是 SQL 聚合计算出来的字段（非数据库原始字段）。</p>
     *
     * @param record 聚合查询结果（仅 dateMonth、avgScore 有意义）
     * @return 成绩趋势 VO
     */
    private ScoreTrendVO convertToScoreTrendVO(ScoreRecord record) {
        ScoreTrendVO vo = new ScoreTrendVO();
        vo.setDateMonth(record.getDateMonth());  // 格式：yyyy-MM
        vo.setAvgScore(record.getAvgScore());    // 该月所有成绩的平均分
        return vo;
    }

    // ==================== 薄弱知识点查询 ====================

    /**
     * 查询指定学生的薄弱知识点
     * <p>查询条件：掌握度 < 60%，用于 Dashboard 展示需要重点复习的内容。</p>
     *
     * @param studentId 学生ID
     * @return 薄弱知识点列表，按掌握度升序（最薄弱的排在最前面）
     */
    @Override
    public List<WeakPointVO> getWeakPoints(Long studentId) {
        log.info("[AcademicService] 查询薄弱知识点，studentId={}", studentId);

        // 调用 Mapper 自定义查询，仅返回 mastery < 60 的记录
        List<KnowledgeMastery> list = knowledgeMasteryMapper.selectWeakPointsByStudentId(studentId);

        if (CollectionUtils.isEmpty(list)) {
            log.info("[AcademicService] 该学生暂无薄弱知识点（全部掌握度 >= 60%），studentId={}", studentId);
            return Collections.emptyList();
        }

        List<WeakPointVO> result = list.stream()
                .map(this::convertToWeakPointVO)
                .collect(Collectors.toList());

        log.info("[AcademicService] 薄弱知识点查询完成，共 {} 个薄弱点", result.size());
        return result;
    }

    /**
     * 将 KnowledgeMastery 实体转换为 WeakPointVO
     *
     * @param mastery 知识点掌握度实体
     * @return 薄弱知识点 VO
     */
    private WeakPointVO convertToWeakPointVO(KnowledgeMastery mastery) {
        WeakPointVO vo = new WeakPointVO();
        vo.setKnowledge(mastery.getKnowledge());
        vo.setMastery(mastery.getMastery());
        vo.setSubject(mastery.getSubject());
        return vo;
    }

    // ==================== 成绩记录 CRUD ====================

    /**
     * 添加或更新成绩记录
     * <p>判断逻辑：dto.id 不为 null 时执行更新（防篡改：必须校验归属），否则执行插入。</p>
     *
     * @param studentId 学生ID（从 UserHolder 获取，用于归属校验）
     * @param dto       成绩记录数据，含 id/examDate/subject/score/fullScore
     * @return 操作是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addScoreRecord(Long studentId, ScoreRecordDto dto) {
        log.info("[AcademicService] 添加/更新成绩记录，studentId={}, dto={}", studentId, dto);

        if (dto.getId() != null) {
            // ==================== 编辑模式 ====================
            // 安全校验：确保该记录属于当前登录学生，防止横向越权
            ScoreRecord existing = scoreRecordMapper.selectById(dto.getId());
            if (existing == null) {
                log.error("[AcademicService] 成绩记录不存在，id={}", dto.getId());
                throw new RuntimeException("记录不存在，id=" + dto.getId());
            }
            if (!existing.getStudentId().equals(studentId)) {
                log.error("[AcademicService] 越权修改：尝试修改他人的成绩记录，recordStudentId={}, currentStudentId={}",
                        existing.getStudentId(), studentId);
                throw new RuntimeException("无权修改他人的成绩记录");
            }

            // 仅更新可变字段（createTime/createBy 由 MyMetaObjectHandler 自动维护）
            existing.setExamDate(dto.getExamDate());
            existing.setSubject(dto.getSubject());
            existing.setScore(dto.getScore());
            existing.setFullScore(dto.getFullScore());

            int rows = scoreRecordMapper.updateById(existing);
            log.info("[AcademicService] 成绩记录更新成功，id={}, affectedRows={}", dto.getId(), rows);
            return rows > 0;

        } else {
            // ==================== 新增模式 ====================
            ScoreRecord record = new ScoreRecord();
            record.setStudentId(studentId);       // 归属到当前登录用户
            record.setExamDate(dto.getExamDate());
            record.setSubject(dto.getSubject());
            record.setScore(dto.getScore());
            record.setFullScore(dto.getFullScore());

            int rows = scoreRecordMapper.insert(record);
            log.info("[AcademicService] 成绩记录添加成功，newId={}, affectedRows={}", record.getId(), rows);
            return rows > 0;
        }
    }

    /**
     * 删除指定的成绩记录
     * <p>安全校验：确保该记录属于当前登录学生，防止越权删除。</p>
     *
     * @param recordId 成绩记录ID
     * @param studentId 当前登录学生ID（用于归属校验）
     * @return 操作是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteScoreRecord(Long recordId, Long studentId) {
        log.info("[AcademicService] 删除成绩记录，recordId={}, studentId={}", recordId, studentId);

        ScoreRecord existing = scoreRecordMapper.selectById(recordId);
        if (existing == null) {
            log.error("[AcademicService] 成绩记录不存在，recordId={}", recordId);
            throw new RuntimeException("记录不存在");
        }
        if (!existing.getStudentId().equals(studentId)) {
            log.error("[AcademicService] 越权删除：recordStudentId={}, currentStudentId={}",
                    existing.getStudentId(), studentId);
            throw new RuntimeException("无权删除他人的成绩记录");
        }

        // MyBatis-Plus 默认物理删除；如有 @TableLogic 则为逻辑删除
        int rows = scoreRecordMapper.deleteById(recordId);
        log.info("[AcademicService] 成绩记录删除完成，recordId={}, affectedRows={}", recordId, rows);
        return rows > 0;
    }

    /**
     * 获取指定学生的所有成绩记录（用于 RecordManage 页面表格展示）
     *
     * @param studentId 学生ID
     * @return 成绩记录列表，按考试日期降序（最近一次考试排在最前）
     */
    @Override
    public List<ScoreRecord> getScoreRecords(Long studentId) {
        log.info("[AcademicService] 获取成绩记录列表，studentId={}", studentId);
        return scoreRecordMapper.selectList(
                new LambdaQueryWrapper<ScoreRecord>()
                        .eq(ScoreRecord::getStudentId, studentId)  // 仅查当前学生
                        .orderByDesc(ScoreRecord::getExamDate)     // 最近考试在前
        );
    }

    // ==================== 知识点掌握度 CRUD ====================

    /**
     * 添加或更新知识点掌握度记录
     * <p>判断逻辑：dto.id 不为 null 时执行更新，否则执行插入。</p>
     *
     * @param studentId 学生ID（用于归属校验）
     * @param dto       知识点掌握度数据，含 id/knowledge/mastery/subject
     * @return 操作是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addKnowledgeMastery(Long studentId, KnowledgeMasteryDto dto) {
        log.info("[AcademicService] 添加/更新知识点掌握度，studentId={}, dto={}", studentId, dto);

        if (dto.getId() != null) {
            // ==================== 编辑模式 ====================
            KnowledgeMastery existing = knowledgeMasteryMapper.selectById(dto.getId());
            if (existing == null) {
                log.error("[AcademicService] 知识点记录不存在，id={}", dto.getId());
                throw new RuntimeException("记录不存在，id=" + dto.getId());
            }
            if (!existing.getStudentId().equals(studentId)) {
                log.error("[AcademicService] 越权修改：recordStudentId={}, currentStudentId={}",
                        existing.getStudentId(), studentId);
                throw new RuntimeException("无权修改他人的知识点记录");
            }

            existing.setKnowledge(dto.getKnowledge());
            existing.setSubject(dto.getSubject());
            existing.setMastery(dto.getMastery());

            int rows = knowledgeMasteryMapper.updateById(existing);
            log.info("[AcademicService] 知识点掌握度更新成功，id={}, affectedRows={}", dto.getId(), rows);
            return rows > 0;

        } else {
            // ==================== 新增模式 ====================
            KnowledgeMastery mastery = new KnowledgeMastery();
            mastery.setStudentId(studentId);
            mastery.setKnowledge(dto.getKnowledge());
            mastery.setMastery(dto.getMastery());
            mastery.setSubject(dto.getSubject());  // 科目字段，为空时也可保存

            int rows = knowledgeMasteryMapper.insert(mastery);
            log.info("[AcademicService] 知识点掌握度添加成功，newId={}, affectedRows={}", mastery.getId(), rows);
            return rows > 0;
        }
    }

    /**
     * 删除指定的知识点掌握度记录
     *
     * @param recordId  知识点记录ID
     * @param studentId 当前登录学生ID（用于归属校验）
     * @return 操作是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteKnowledgeMastery(Long recordId, Long studentId) {
        log.info("[AcademicService] 删除知识点掌握度记录，recordId={}, studentId={}", recordId, studentId);

        KnowledgeMastery existing = knowledgeMasteryMapper.selectById(recordId);
        if (existing == null) {
            log.error("[AcademicService] 知识点记录不存在，recordId={}", recordId);
            throw new RuntimeException("记录不存在");
        }
        if (!existing.getStudentId().equals(studentId)) {
            log.error("[AcademicService] 越权删除：recordStudentId={}, currentStudentId={}",
                    existing.getStudentId(), studentId);
            throw new RuntimeException("无权删除他人的知识点记录");
        }

        int rows = knowledgeMasteryMapper.deleteById(recordId);
        log.info("[AcademicService] 知识点掌握度删除完成，recordId={}, affectedRows={}", recordId, rows);
        return rows > 0;
    }

    /**
     * 获取指定学生的所有知识点掌握度记录（用于 RecordManage 页面表格展示）
     *
     * @param studentId 学生ID
     * @return 知识点掌握度列表，按掌握度升序（最薄弱的在最前，便于一眼看到重点）
     */
    @Override
    public List<KnowledgeMastery> getKnowledgeMasteries(Long studentId) {
        log.info("[AcademicService] 获取知识点掌握度列表，studentId={}", studentId);
        return knowledgeMasteryMapper.selectList(
                new LambdaQueryWrapper<KnowledgeMastery>()
                        .eq(KnowledgeMastery::getStudentId, studentId)
                        .orderByAsc(KnowledgeMastery::getMastery)  // 薄弱优先
        );
    }
}
