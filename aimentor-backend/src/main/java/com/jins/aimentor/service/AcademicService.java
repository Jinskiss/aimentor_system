package com.jins.aimentor.service;

import com.jins.aimentor.domain.vo.ScoreTrendVO;
import com.jins.aimentor.domain.vo.WeakPointVO;

import java.util.List;

public interface AcademicService {
    
    /**
     * 获取成绩趋势
     * @param studentId 学生ID
     * @return 成绩趋势列表
     */
    List<ScoreTrendVO> getScoreTrend(Long studentId);
    
    /**
     * 获取薄弱知识点
     * @param studentId 学生ID
     * @return 薄弱知识点列表
     */
    List<WeakPointVO> getWeakPoints(Long studentId);
}