package com.jins.aimentor.controller;

import com.jins.aimentor.common.Result;
import com.jins.aimentor.domain.vo.ScoreTrendVO;
import com.jins.aimentor.domain.vo.WeakPointVO;
import com.jins.aimentor.service.AcademicService;
import com.jins.aimentor.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学术分析控制器
 * <p>提供学生学业数据分析相关接口，包括成绩趋势和薄弱知识点</p>
 *
 * @author jins
 */
@Slf4j
@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
@Api(tags = "学术分析", description = "学业数据分析接口")
public class AcademicController {
    
    @Autowired
    private AcademicService academicService;

    /**
     * 获取当前登录学生的成绩趋势
     * <p>从ThreadLocal获取当前用户ID，查询该学生的成绩趋势</p>
     *
     * @return 成绩趋势列表，按月份排序
     */
    @ApiOperation("获取当前学生成绩趋势")
    @GetMapping("/trend")
    public Result<List<ScoreTrendVO>> getCurrentStudentTrend() {
        Long studentId = UserHolder.getUser().getId();
        log.info("获取当前学生成绩趋势，studentId: {}", studentId);

        List<ScoreTrendVO> trend = academicService.getScoreTrend(studentId);
        return Result.success(trend);
    }

    /**
     * 获取指定学生的成绩趋势
     * <p>需要教师或管理员权限，用于查看特定学生的学业情况</p>
     *
     * @param studentId 学生ID
     * @return 成绩趋势列表
     */
    @ApiOperation("获取指定学生成绩趋势")
    @GetMapping("/trend/{studentId}")
    public Result<List<ScoreTrendVO>> getScoreTrend(@PathVariable Long studentId) {
        log.info("获取指定学生成绩趋势，studentId: {}", studentId);

        List<ScoreTrendVO> trend = academicService.getScoreTrend(studentId);

        return Result.success(trend);
    }

    /**
     * 获取当前登录学生的薄弱知识点
     * <p>从ThreadLocal获取当前用户ID，识别需要重点复习的知识点</p>
     *
     * @return 薄弱知识点列表，按掌握度升序排列
     */
    @ApiOperation("获取当前学生薄弱知识点")
    @GetMapping("/weakpoints")
    public Result<List<WeakPointVO>> getCurrentStudentWeakPoints() {
        Long studentId = UserHolder.getUser().getId();
        log.info("获取当前学生薄弱知识点，studentId: {}", studentId);

        List<WeakPointVO> weakPoints = academicService.getWeakPoints(studentId);
        return Result.success(weakPoints);
    }

    /**
     * 获取指定学生的薄弱知识点
     * <p>需要教师或管理员权限，用于查看特定学生的薄弱项</p>
     *
     * @param studentId 学生ID
     * @return 薄弱知识点列表
     */
    @GetMapping("/weakpoints/{studentId}")
    public Result<List<WeakPointVO>> getWeakPoints(@PathVariable Long studentId) {
        log.info("获取指定学生薄弱知识点，studentId: {}", studentId);

        List<WeakPointVO> weakPoints = academicService.getWeakPoints(studentId);

        return Result.success(weakPoints);
    }
}