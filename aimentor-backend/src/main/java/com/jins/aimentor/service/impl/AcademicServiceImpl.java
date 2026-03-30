package com.jins.aimentor.service.impl;

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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学术分析服务实现类
 * <p>实现学生学业数据的统计分析和业务逻辑处理</p>
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AcademicServiceImpl implements AcademicService {
    
    @Autowired
    private ScoreRecordMapper scoreRecordMapper;
    
    @Autowired
    private KnowledgeMasteryMapper knowledgeMasteryMapper;

    /**
     * 获取指定学生的成绩趋势
     * <p>需要教师或管理员权限，用于查看特定学生的学业情况</p>
     *
     * @param studentId 学生ID
     * @return 成绩趋势列表
     */
    @Override
    public List<ScoreTrendVO> getScoreTrend(Long studentId) {
        log.info("获取指定学生成绩趋势，studentId: {}", studentId);

        List<ScoreRecord> records = scoreRecordMapper.selectTrendByStudentId(studentId);

        // 无记录时返回空列表
        if (CollectionUtils.isEmpty(records)) {
            log.warn("该学生暂无成绩记录，studentId: {}", studentId);
            return Collections.emptyList();
        }

        // 转换为VO对象
        List<ScoreTrendVO> result = records.stream()
                .map(this::convertToScoreTrendVO)
                .collect(Collectors.toList());

        log.info("成绩趋势查询完成，共 {} 条记录", result.size());
        return result;
    }

    /**
     * 将ScoreRecord转换为ScoreTrendVO
     * <p>注意：dateMonth和avgScore是查询时动态计算的字段</p>
     *
     * @param record 成绩记录实体（包含聚合字段）
     * @return 成绩趋势VO
     */
    private ScoreTrendVO convertToScoreTrendVO(ScoreRecord record) {
        ScoreTrendVO scoreTrendVO = new ScoreTrendVO();

        // 使用@TableField(exist = false)标记的聚合字段
        scoreTrendVO.setDateMonth(record.getDateMonth());
        scoreTrendVO.setAvgScore(record.getAvgScore());

        return scoreTrendVO;
    }
    
    @Override
    public List<WeakPointVO> getWeakPoints(Long studentId) {
        log.info("查询薄弱知识点，studentId: {}", studentId);

        List<KnowledgeMastery> list = knowledgeMasteryMapper.selectWeakPointsByStudentId(studentId);

        // 无薄弱点时返回空列表
        if (CollectionUtils.isEmpty(list)) {
            log.info("该学生暂无薄弱知识点，studentId: {}", studentId);
            return Collections.emptyList();
        }

        // 转换为VO对象
        List<WeakPointVO> result = list.stream()
                .map(this::convertToWeakPointVO)
                .collect(Collectors.toList());

        log.info("薄弱知识点查询完成，共 {} 个薄弱点", result.size());
        return result;
    }

    /**
     * 将KnowledgeMastery转换为WeakPointVO
     *
     * @param mastery 知识点掌握度实体
     * @return 薄弱知识点VO
     */
    private WeakPointVO convertToWeakPointVO(KnowledgeMastery mastery) {
        WeakPointVO weakPointVO = new WeakPointVO();

        weakPointVO.setKnowledge(mastery.getKnowledge());
        weakPointVO.setMastery(mastery.getMastery());
        weakPointVO.setSubject(mastery.getSubject());

        return weakPointVO;
    }

    @Override
    public boolean addScoreRecord(Long studentId, ScoreRecordDto dto) {
        log.info("添加成绩记录，studentId: {}, dto: {}", studentId, dto);

        ScoreRecord record = new ScoreRecord();
        record.setStudentId(studentId);
        record.setExamDate(dto.getExamDate());
        record.setSubject(dto.getSubject());
        record.setScore(dto.getScore());
        record.setFullScore(dto.getFullScore());

        int result = scoreRecordMapper.insert(record);
        log.info("成绩记录添加结果: {}", result > 0 ? "成功" : "失败");
        return result > 0;
    }

    @Override
    public boolean addKnowledgeMastery(Long studentId, KnowledgeMasteryDto dto) {
        log.info("添加知识点掌握度，studentId: {}, dto: {}", studentId, dto);

        KnowledgeMastery mastery = new KnowledgeMastery();
        mastery.setStudentId(studentId);
        mastery.setKnowledge(dto.getKnowledge());
        mastery.setMastery(dto.getMastery());
        mastery.setSubject(dto.getSubject());

        int result = knowledgeMasteryMapper.insert(mastery);
        log.info("知识点掌握度添加结果: {}", result > 0 ? "成功" : "失败");
        return result > 0;
    }

    @Override
    public List<ScoreRecord> getScoreRecords(Long studentId) {
        log.info("获取学生所有成绩记录，studentId: {}", studentId);
        return scoreRecordMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ScoreRecord>()
                .eq(ScoreRecord::getStudentId, studentId)
                .orderByDesc(ScoreRecord::getExamDate)
        );
    }

    @Override
    public List<KnowledgeMastery> getKnowledgeMasteries(Long studentId) {
        log.info("获取学生所有知识点掌握度，studentId: {}", studentId);
        return knowledgeMasteryMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeMastery>()
                .eq(KnowledgeMastery::getStudentId, studentId)
                .orderByAsc(KnowledgeMastery::getMastery)
        );
    }
}