package com.jins.aimentor.controller;

import com.jins.aimentor.common.Result;
import com.jins.aimentor.domain.vo.ScoreTrendVO;
import com.jins.aimentor.domain.vo.WeakPointVO;
import com.jins.aimentor.service.AcademicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/academic")
public class AcademicController {
    
    @Autowired
    private AcademicService academicService;
    
    /**
     * 获取成绩趋势
     * @param studentId 学生ID
     */
    @GetMapping("/trend/{studentId}")
    public Result<List<ScoreTrendVO>> getScoreTrend(@PathVariable Long studentId) {
        List<ScoreTrendVO> trend = academicService.getScoreTrend(studentId);

        return Result.success(trend);
    }
    
    /**
     * 获取薄弱知识点
     * @param studentId 学生ID
     */
    @GetMapping("/weakpoints/{studentId}")
    public Result<List<WeakPointVO>> getWeakPoints(@PathVariable Long studentId) {
        List<WeakPointVO> weakPoints = academicService.getWeakPoints(studentId);
        return Result.success(weakPoints);
    }
}