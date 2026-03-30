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
 * AcademicController - 学情分析控制器
 *
 * <p>职责：
 * <ul>
 *   <li>成绩趋势查询：GET /api/academic/trend，返回 Dashboard 折线图数据</li>
 *   <li>薄弱知识点查询：GET /api/academic/weakpoints，返回 Dashboard 薄弱列表</li>
 *   <li>成绩记录管理：CRUD，支持 RecordManage 页面添加/编辑/删除成绩</li>
 *   <li>知识点掌握度管理：CRUD，支持 RecordManage 页面添加/编辑/删除知识点</li>
 * </ul>
 *
 * <p>所有查询接口均从 UserHolder 获取当前登录用户 ID，
 * 保证学生只能访问/操作自己的数据（归属校验在 Service 层执行）。
 *
 * <p>接口一览：
 * <table border="1">
 *   <tr><th>方法</th><th>路径</th><th>说明</th></tr>
 *   <tr><td>GET</td><td>/api/academic/trend</td><td>当前学生成绩趋势（无需参数）</td></tr>
 *   <tr><td>GET</td><td>/api/academic/trend/{studentId}</td><td>指定学生成绩趋势（教师用）</td></tr>
 *   <tr><td>GET</td><td>/api/academic/weakpoints</td><td>当前学生薄弱知识点</td></tr>
 *   <tr><td>GET</td><td>/api/academic/scores</td><td>当前学生所有成绩记录</td></tr>
 *   <tr><td>POST</td><td>/api/academic/score</td><td>添加/更新成绩记录（id存在则更新）</td></tr>
 *   <tr><td>DELETE</td><td>/api/academic/score/{id}</td><td>删除指定成绩记录</td></tr>
 *   <tr><td>GET</td><td>/api/academic/masteries</td><td>当前学生所有知识点掌握度</td></tr>
 *   <tr><td>POST</td><td>/api/academic/mastery</td><td>添加/更新知识点（id存在则更新）</td></tr>
 *   <tr><td>DELETE</td><td>/api/academic/mastery/{id}</td><td>删除指定知识点记录</td></tr>
 * </table>
 */
@Slf4j
@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
@Api(tags = "学情分析", description = "学业数据分析、成绩记录管理、知识点掌握度管理")
public class AcademicController {

    /** 学情分析服务 */
    @Autowired
    private AcademicService academicService;

    // ==================== 成绩趋势查询 ====================

    /**
     * 获取当前登录学生的成绩趋势
     * <p>后端从 UserHolder 自动获取当前用户 ID，无需前端传递 studentId，
     *    避免雪花 ID 在 JSON 序列化时精度丢失的问题。</p>
     *
     * @return 成绩趋势列表（含月份 dateMonth 和平均分 avgScore），按时间升序
     */
    @ApiOperation("获取当前学生成绩趋势")
    @GetMapping("/trend")
    public Result<List<ScoreTrendVO>> getCurrentStudentTrend() {
        Long studentId = UserHolder.getUser().getId();
        log.info("[AcademicController] 获取当前学生成绩趋势，studentId={}", studentId);

        List<ScoreTrendVO> trend = academicService.getScoreTrend(studentId);
        log.info("[AcademicController] 成绩趋势返回 {} 个月份数据", trend.size());
        return Result.success(trend);
    }

    /**
     * 获取指定学生的成绩趋势（教师/管理员用）
     *
     * @param studentId 学生ID
     * @return 成绩趋势列表
     */
    @ApiOperation("获取指定学生成绩趋势（教师用）")
    @GetMapping("/trend/{studentId}")
    public Result<List<ScoreTrendVO>> getScoreTrend(@PathVariable Long studentId) {
        log.info("[AcademicController] 获取指定学生成绩趋势，studentId={}", studentId);
        List<ScoreTrendVO> trend = academicService.getScoreTrend(studentId);
        return Result.success(trend);
    }

    // ==================== 薄弱知识点查询 ====================

    /**
     * 获取当前登录学生的薄弱知识点（掌握度 < 60%）
     *
     * @return 薄弱知识点列表，按掌握度升序
     */
    @ApiOperation("获取当前学生薄弱知识点")
    @GetMapping("/weakpoints")
    public Result<List<WeakPointVO>> getCurrentStudentWeakPoints() {
        Long studentId = UserHolder.getUser().getId();
        log.info("[AcademicController] 获取当前学生薄弱知识点，studentId={}", studentId);

        List<WeakPointVO> weakPoints = academicService.getWeakPoints(studentId);
        return Result.success(weakPoints);
    }

    /**
     * 获取指定学生的薄弱知识点（教师/管理员用）
     *
     * @param studentId 学生ID
     * @return 薄弱知识点列表
     */
    @ApiOperation("获取指定学生薄弱知识点（教师用）")
    @GetMapping("/weakpoints/{studentId}")
    public Result<List<WeakPointVO>> getWeakPoints(@PathVariable Long studentId) {
        log.info("[AcademicController] 获取指定学生薄弱知识点，studentId={}", studentId);
        return Result.success(academicService.getWeakPoints(studentId));
    }

    // ==================== 成绩记录 CRUD ====================

    /**
     * 添加或更新成绩记录
     * <p>判断逻辑：请求体中 id != null 时执行更新，否则执行插入。</p>
     *
     * @param dto 成绩记录数据（id/examDate/subject/score/fullScore）
     * @return 操作结果
     */
    @ApiOperation("添加/更新成绩记录（id存在则更新，否则添加）")
    @PostMapping("/score")
    public Result<Void> addOrUpdateScoreRecord(@Valid @RequestBody ScoreRecordDto dto) {
        Long studentId = UserHolder.getUser().getId();
        log.info("[AcademicController] 添加/更新成绩记录，studentId={}, dto={}", studentId, dto);

        try {
            boolean success = academicService.addScoreRecord(studentId, dto);
            if (success) {
                return Result.success();
            } else {
                return Result.error("操作失败，请稍后重试");
            }
        } catch (RuntimeException e) {
            log.error("[AcademicController] 操作失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除指定的成绩记录
     *
     * @param id 成绩记录ID
     * @return 操作结果
     */
    @ApiOperation("删除指定成绩记录")
    @DeleteMapping("/score/{id}")
    public Result<Void> deleteScoreRecord(@PathVariable Long id) {
        Long studentId = UserHolder.getUser().getId();
        log.info("[AcademicController] 删除成绩记录，id={}, studentId={}", id, studentId);

        try {
            boolean success = academicService.deleteScoreRecord(id, studentId);
            return success ? Result.success() : Result.error("删除失败");
        } catch (RuntimeException e) {
            log.error("[AcademicController] 删除失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前学生的所有成绩记录（用于 RecordManage 页面表格）
     *
     * @return 成绩记录列表，按考试日期降序
     */
    @ApiOperation("获取当前学生所有成绩记录")
    @GetMapping("/scores")
    public Result<List<ScoreRecord>> getCurrentStudentScores() {
        Long studentId = UserHolder.getUser().getId();
        log.info("[AcademicController] 获取当前学生所有成绩记录，studentId={}", studentId);

        List<ScoreRecord> records = academicService.getScoreRecords(studentId);
        log.info("[AcademicController] 返回 {} 条成绩记录", records.size());
        return Result.success(records);
    }

    // ==================== 知识点掌握度 CRUD ====================

    /**
     * 添加或更新知识点掌握度记录
     * <p>判断逻辑：请求体中 id != null 时执行更新，否则执行插入。</p>
     *
     * @param dto 知识点掌握度数据（id/knowledge/mastery/subject）
     * @return 操作结果
     */
    @ApiOperation("添加/更新知识点掌握度（id存在则更新，否则添加）")
    @PostMapping("/mastery")
    public Result<Void> addOrUpdateKnowledgeMastery(@Valid @RequestBody KnowledgeMasteryDto dto) {
        Long studentId = UserHolder.getUser().getId();
        log.info("[AcademicController] 添加/更新知识点掌握度，studentId={}, dto={}", studentId, dto);

        try {
            boolean success = academicService.addKnowledgeMastery(studentId, dto);
            return success ? Result.success() : Result.error("操作失败，请稍后重试");
        } catch (RuntimeException e) {
            log.error("[AcademicController] 操作失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除指定的知识点掌握度记录
     *
     * @param id 知识点记录ID
     * @return 操作结果
     */
    @ApiOperation("删除指定知识点掌握度记录")
    @DeleteMapping("/mastery/{id}")
    public Result<Void> deleteKnowledgeMastery(@PathVariable Long id) {
        Long studentId = UserHolder.getUser().getId();
        log.info("[AcademicController] 删除知识点掌握度记录，id={}, studentId={}", id, studentId);

        try {
            boolean success = academicService.deleteKnowledgeMastery(id, studentId);
            return success ? Result.success() : Result.error("删除失败");
        } catch (RuntimeException e) {
            log.error("[AcademicController] 删除失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前学生的所有知识点掌握度记录（用于 RecordManage 页面表格）
     *
     * @return 知识点掌握度列表，按掌握度升序
     */
    @ApiOperation("获取当前学生所有知识点掌握度")
    @GetMapping("/masteries")
    public Result<List<KnowledgeMastery>> getCurrentStudentMasteries() {
        Long studentId = UserHolder.getUser().getId();
        log.info("[AcademicController] 获取当前学生所有知识点掌握度，studentId={}", studentId);

        List<KnowledgeMastery> masteries = academicService.getKnowledgeMasteries(studentId);
        log.info("[AcademicController] 返回 {} 条知识点记录", masteries.size());
        return Result.success(masteries);
    }
}
