package com.jins.aimentor.controller;

import com.jins.aimentor.common.Result;
import com.jins.aimentor.domain.dto.KnowledgeMasteryDto;
import com.jins.aimentor.domain.dto.ScoreRecordDto;
import com.jins.aimentor.domain.entity.KnowledgeMastery;
import com.jins.aimentor.domain.entity.ScoreRecord;
import com.jins.aimentor.domain.vo.ScoreTrendVO;
import com.jins.aimentor.domain.vo.WeakPointVO;
import com.jins.aimentor.service.AcademicService;
import com.jins.aimentor.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 添加成绩记录
     */
    @ApiOperation("添加成绩记录")
    @PostMapping("/score")
    public Result<Void> addScoreRecord(@Valid @RequestBody ScoreRecordDto dto) {
        Long studentId = UserHolder.getUser().getId();
        log.info("添加成绩记录，studentId: {}, dto: {}", studentId, dto);

        boolean success = academicService.addScoreRecord(studentId, dto);
        return success ? Result.success() : Result.error("添加成绩记录失败");
    }

    /**
     * 添加知识点掌握度记录
     */
    @ApiOperation("添加知识点掌握度")
    @PostMapping("/mastery")
    public Result<Void> addKnowledgeMastery(@Valid @RequestBody KnowledgeMasteryDto dto) {
        Long studentId = UserHolder.getUser().getId();
        log.info("添加知识点掌握度，studentId: {}, dto: {}", studentId, dto);

        boolean success = academicService.addKnowledgeMastery(studentId, dto);
        return success ? Result.success() : Result.error("添加知识点掌握度失败");
    }

    /**
     * 获取当前学生的所有成绩记录
     */
    @ApiOperation("获取当前学生所有成绩记录")
    @GetMapping("/scores")
    public Result<List<ScoreRecord>> getCurrentStudentScores() {
        Long studentId = UserHolder.getUser().getId();
        log.info("获取当前学生所有成绩记录，studentId: {}", studentId);

        List<ScoreRecord> records = academicService.getScoreRecords(studentId);
        return Result.success(records);
    }

    /**
     * 获取当前学生的所有知识点掌握度记录
     */
    @ApiOperation("获取当前学生所有知识点掌握度")
    @GetMapping("/masteries")
    public Result<List<KnowledgeMastery>> getCurrentStudentMasteries() {
        Long studentId = UserHolder.getUser().getId();
        log.info("获取当前学生所有知识点掌握度，studentId: {}", studentId);

        List<KnowledgeMastery> masteries = academicService.getKnowledgeMasteries(studentId);
        return Result.success(masteries);
    }
}