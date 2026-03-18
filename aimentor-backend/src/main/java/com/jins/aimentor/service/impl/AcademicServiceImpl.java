package com.jins.aimentor.service.impl;

import com.jins.aimentor.domain.entity.KnowledgeMastery;
import com.jins.aimentor.domain.entity.ScoreRecord;
import com.jins.aimentor.domain.vo.ScoreTrendVO;
import com.jins.aimentor.domain.vo.WeakPointVO;
import com.jins.aimentor.mapper.KnowledgeMasteryMapper;
import com.jins.aimentor.mapper.ScoreRecordMapper;
import com.jins.aimentor.service.AcademicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AcademicServiceImpl implements AcademicService {
    
    @Autowired
    private ScoreRecordMapper scoreRecordMapper;
    
    @Autowired
    private KnowledgeMasteryMapper knowledgeMasteryMapper;
    
    @Override
    public List<ScoreTrendVO> getScoreTrend(Long studentId) {
        List<ScoreRecord> records = scoreRecordMapper.selectTrendByStudentId(studentId);
        
        // 转换为VO对象
        return records.stream().map(record -> {
            ScoreTrendVO vo = new ScoreTrendVO();
            // 这里假设record中有dateMonth和avgScore字段，实际应根据查询结果调整
            vo.setDate(record.getDateMonth());
            vo.setScore(record.getAvgScore());
            return vo;
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<WeakPointVO> getWeakPoints(Long studentId) {
        List<KnowledgeMastery> list = knowledgeMasteryMapper.selectWeakPointsByStudentId(studentId);
        
        return list.stream().map(item -> {
            WeakPointVO vo = new WeakPointVO();
            vo.setKnowledge(item.getKnowledge());
            vo.setMastery(item.getMastery());
            return vo;
        }).collect(Collectors.toList());
    }
}