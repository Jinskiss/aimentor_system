package com.jins.aimentor.controller;

import com.jins.aimentor.common.Result;
import com.jins.aimentor.constants.Status;
import com.jins.aimentor.domain.entity.User;
import com.jins.aimentor.domain.vo.*;
import com.jins.aimentor.domain.entity.Plan;
import com.jins.aimentor.domain.entity.ScoreRecord;
import com.jins.aimentor.domain.entity.KnowledgeMastery;
import com.jins.aimentor.service.TeacherService;
import com.jins.aimentor.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 教师端控制器
 *
 * <p>提供教师专属的学情查看功能，包括 Dashboard 数据聚合、学生列表查看等。
 * 所有接口均要求当前登录用户角色为 teacher。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Api(tags = "教师端", description = "教师管理学生学情的接口")
public class TeacherController {

    private final TeacherService teacherService;

    /**
     * 获取教师 Dashboard 汇总数据
     * <p>返回该教师所在年级的学生统计、趋势图、雷达图、待关注学生列表</p>
     */
    @ApiOperation("获取教师 Dashboard 数据")
    @GetMapping("/dashboard")
    public Result<TeacherDashboardVO> getDashboard() {
        User teacher = UserHolder.getUser();
        if (teacher == null) {
            return Result.error(Status.CODE_401, "用户未登录");
        }
        if (!"teacher".equals(teacher.getRole())) {
            return Result.error(Status.CODE_403, "仅限教师账号访问");
        }
        log.info("[TeacherController] 获取教师 Dashboard，teacherId={}", teacher.getId());
        TeacherDashboardVO data = teacherService.getDashboardData(teacher.getId());
        return Result.success(data);
    }

    /**
     * 获取年级学生列表（带学情摘要）
     *
     * @param grade 年级名称（如"高一"），不传则返回所有年级学生
     */
    @ApiOperation("获取学生列表（教师用）")
    @GetMapping("/students")
    public Result<List<StudentListItemVO>> getStudentList(
            @RequestParam(required = false) String grade) {
        User teacher = UserHolder.getUser();
        if (teacher == null) {
            return Result.error(Status.CODE_401, "用户未登录");
        }
        if (!"teacher".equals(teacher.getRole())) {
            return Result.error(Status.CODE_403, "仅限教师账号访问");
        }
        // 如果教师设了年级，只返回该年级的学生
        String targetGrade = (grade != null && !grade.isBlank()) ? grade : teacher.getGrade();
        log.info("[TeacherController] 获取学生列表，grade={}", targetGrade);
        List<StudentListItemVO> list = teacherService.getStudentList(targetGrade);
        return Result.success(list);
    }

    /**
     * 获取年级成绩趋势（按月聚合）
     */
    @ApiOperation("获取年级成绩趋势")
    @GetMapping("/trend")
    public Result<List<GradeScoreTrendVO>> getGradeTrend(
            @RequestParam(required = false) String grade) {
        User teacher = UserHolder.getUser();
        if (teacher == null) {
            return Result.error(Status.CODE_401, "用户未登录");
        }
        if (!"teacher".equals(teacher.getRole())) {
            return Result.error(Status.CODE_403, "仅限教师账号访问");
        }
        String targetGrade = (grade != null && !grade.isBlank()) ? grade : teacher.getGrade();
        log.info("[TeacherController] 获取年级趋势，grade={}", targetGrade);
        return Result.success(teacherService.getGradeTrend(targetGrade));
    }

    /**
     * 获取年级各科目平均分（雷达图用）
     */
    @ApiOperation("获取科目平均分")
    @GetMapping("/subjects")
    public Result<List<SubjectAvgVO>> getSubjectAvgs(
            @RequestParam(required = false) String grade) {
        User teacher = UserHolder.getUser();
        if (teacher == null) {
            return Result.error(Status.CODE_401, "用户未登录");
        }
        if (!"teacher".equals(teacher.getRole())) {
            return Result.error(Status.CODE_403, "仅限教师账号访问");
        }
        String targetGrade = (grade != null && !grade.isBlank()) ? grade : teacher.getGrade();
        return Result.success(teacherService.getSubjectAvgs(targetGrade));
    }

    /**
     * 获取待关注学生列表
     */
    @ApiOperation("获取待关注学生")
    @GetMapping("/attention")
    public Result<List<AttentionStudentVO>> getAttentionStudents(
            @RequestParam(required = false) String grade) {
        User teacher = UserHolder.getUser();
        if (teacher == null) {
            return Result.error(Status.CODE_401, "用户未登录");
        }
        if (!"teacher".equals(teacher.getRole())) {
            return Result.error(Status.CODE_403, "仅限教师账号访问");
        }
        String targetGrade = (grade != null && !grade.isBlank()) ? grade : teacher.getGrade();
        return Result.success(teacherService.getAttentionStudents(targetGrade));
    }

    @ApiOperation("获取指定学生的笔记（教师用）")
    @GetMapping("/student/{studentId}/notes")
    public Result<List<NoteVO>> getStudentNotes(@PathVariable Long studentId) {
        User teacher = UserHolder.getUser();
        if (teacher == null) {
            return Result.error(Status.CODE_401, "用户未登录");
        }
        if (!"teacher".equals(teacher.getRole())) {
            return Result.error(Status.CODE_403, "仅限教师账号访问");
        }
        // 检查权限
        if (!teacherService.canViewStudent(teacher.getId(), studentId)) {
            return Result.error(Status.CODE_403, "无权查看该学生数据");
        }
        log.info("[TeacherController] 获取学生笔记，teacherId={}, studentId={}", teacher.getId(), studentId);
        return Result.success(teacherService.getStudentNotes(studentId));
    }

    @ApiOperation("获取指定学生的学习计划（教师用）")
    @GetMapping("/student/{studentId}/plans")
    public Result<List<Plan>> getStudentPlans(@PathVariable Long studentId) {
        User teacher = UserHolder.getUser();
        if (teacher == null) {
            return Result.error(Status.CODE_401, "用户未登录");
        }
        if (!"teacher".equals(teacher.getRole())) {
            return Result.error(Status.CODE_403, "仅限教师账号访问");
        }
        if (!teacherService.canViewStudent(teacher.getId(), studentId)) {
            return Result.error(Status.CODE_403, "无权查看该学生数据");
        }
        log.info("[TeacherController] 获取学生学习计划，teacherId={}, studentId={}", teacher.getId(), studentId);
        return Result.success(teacherService.getStudentPlans(studentId));
    }

    @ApiOperation("获取指定学生的成绩记录（教师用）")
    @GetMapping("/student/{studentId}/scores")
    public Result<List<ScoreRecord>> getStudentScores(@PathVariable Long studentId) {
        User teacher = UserHolder.getUser();
        if (teacher == null) {
            return Result.error(Status.CODE_401, "用户未登录");
        }
        if (!"teacher".equals(teacher.getRole())) {
            return Result.error(Status.CODE_403, "仅限教师账号访问");
        }
        if (!teacherService.canViewStudent(teacher.getId(), studentId)) {
            return Result.error(Status.CODE_403, "无权查看该学生数据");
        }
        log.info("[TeacherController] 获取学生成绩记录，teacherId={}, studentId={}", teacher.getId(), studentId);
        return Result.success(teacherService.getStudentScores(studentId));
    }

    @ApiOperation("获取指定学生的知识点掌握度（教师用）")
    @GetMapping("/student/{studentId}/masteries")
    public Result<List<KnowledgeMastery>> getStudentMasteries(@PathVariable Long studentId) {
        User teacher = UserHolder.getUser();
        if (teacher == null) {
            return Result.error(Status.CODE_401, "用户未登录");
        }
        if (!"teacher".equals(teacher.getRole())) {
            return Result.error(Status.CODE_403, "仅限教师账号访问");
        }
        if (!teacherService.canViewStudent(teacher.getId(), studentId)) {
            return Result.error(Status.CODE_403, "无权查看该学生数据");
        }
        log.info("[TeacherController] 获取学生知识点掌握度，teacherId={}, studentId={}", teacher.getId(), studentId);
        return Result.success(teacherService.getStudentMasteries(studentId));
    }

    @ApiOperation("获取所有学生笔记列表（教师用）")
    @GetMapping("/notes")
    public Result<List<Map<String, Object>>> getAllStudentNotes(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) Long studentId) {
        User teacher = UserHolder.getUser();
        if (teacher == null) {
            return Result.error(Status.CODE_401, "用户未登录");
        }
        if (!"teacher".equals(teacher.getRole())) {
            return Result.error(Status.CODE_403, "仅限教师账号访问");
        }
        log.info("[TeacherController] 获取学生笔记列表，teacherId={}, subject={}, studentId={}",
                teacher.getId(), subject, studentId);
        List<Map<String, Object>> notes = teacherService.getAllStudentNotes(subject, studentId);
        return Result.success(notes);
    }

    @ApiOperation("获取所有学生学习计划（教师用）")
    @GetMapping("/plans")
    public Result<List<Map<String, Object>>> getAllStudentPlans(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) Long studentId) {
        User teacher = UserHolder.getUser();
        if (teacher == null) {
            return Result.error(Status.CODE_401, "用户未登录");
        }
        if (!"teacher".equals(teacher.getRole())) {
            return Result.error(Status.CODE_403, "仅限教师账号访问");
        }
        log.info("[TeacherController] 获取学生学习计划，teacherId={}, subject={}, studentId={}",
                teacher.getId(), subject, studentId);
        List<Map<String, Object>> plans = teacherService.getAllStudentPlans(subject, studentId);
        return Result.success(plans);
    }
}
