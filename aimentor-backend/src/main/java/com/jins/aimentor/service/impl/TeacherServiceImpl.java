package com.jins.aimentor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jins.aimentor.domain.entity.KnowledgeMastery;
import com.jins.aimentor.domain.entity.Note;
import com.jins.aimentor.domain.entity.Plan;
import com.jins.aimentor.domain.entity.ScoreRecord;
import com.jins.aimentor.domain.entity.User;
import com.jins.aimentor.domain.vo.*;
import com.jins.aimentor.mapper.KnowledgeMasteryMapper;
import com.jins.aimentor.mapper.NoteMapper;
import com.jins.aimentor.mapper.PlanMapper;
import com.jins.aimentor.mapper.ScoreRecordMapper;
import com.jins.aimentor.mapper.UserMapper;
import com.jins.aimentor.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 教师端服务实现
 *
 * <p>核心逻辑：根据教师所在年级，聚合该年级所有学生的学情数据</p>
 */
@Slf4j
@Service
    @RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final UserMapper userMapper;
    private final ScoreRecordMapper scoreRecordMapper;
    private final KnowledgeMasteryMapper knowledgeMasteryMapper;
    private final NoteMapper noteMapper;
    private final PlanMapper planMapper;

    @Override
    public TeacherDashboardVO getDashboardData(Long teacherId) {
        // 获取教师信息，确定管辖年级
        User teacher = userMapper.selectById(teacherId);
        if (teacher == null) {
            return new TeacherDashboardVO();
        }
        String grade = teacher.getGrade();
        log.info("[TeacherService] 获取教师 Dashboard，teacherId={}，grade={}", teacherId, grade);

        TeacherDashboardVO vo = new TeacherDashboardVO();

        // 1. 获取年级学生列表
        List<User> students = getStudentsByGrade(grade);
        List<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toList());
        vo.setStudentCount((long) students.size());

        if (studentIds.isEmpty()) {
            vo.setTrendData(new ArrayList<>());
            vo.setSubjectAvgs(new ArrayList<>());
            vo.setAttentionStudents(new ArrayList<>());
            return vo;
        }

        // 2. 成绩趋势（按月聚合）
        List<GradeScoreTrendVO> trendData = getGradeTrend(grade);
        vo.setTrendData(trendData);

        // 3. 考试人次（当月）
        Long examCount = countMonthlyExams(studentIds);
        vo.setExamCount(examCount);

        // 4. 年级平均分
        Double avgScore = calcGradeAvgScore(studentIds);
        vo.setAvgScore(avgScore);

        // 5. 薄弱知识点总数
        Long weakCount = countWeakPoints(studentIds);
        vo.setWeakPointCount(weakCount);

        // 6. 各科目平均分（雷达图）
        vo.setSubjectAvgs(getSubjectAvgs(grade));

        // 7. 待关注学生
        vo.setAttentionStudents(getAttentionStudents(grade));

        return vo;
    }

    @Override
    public List<StudentListItemVO> getStudentList(String grade) {
        List<User> students = getStudentsByGrade(grade);
        if (students.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> studentIds = students.stream().map(User::getId).collect(Collectors.toList());

        return students.stream().map(student -> {
            StudentListItemVO item = new StudentListItemVO();
            item.setId(student.getId());
            item.setName(student.getName());
            item.setGrade(student.getGrade());
            item.setEmail(student.getEmail());

            // 计算该学生平均分
            Double avgScore = calcStudentAvgScore(student.getId());
            item.setAvgScore(avgScore);

            // 统计薄弱知识点数
            LambdaQueryWrapper<KnowledgeMastery> kw = new LambdaQueryWrapper<>();
            kw.eq(KnowledgeMastery::getStudentId, student.getId())
              .lt(KnowledgeMastery::getMastery, 60);
            long weakCount = knowledgeMasteryMapper.selectCount(kw);
            item.setWeakPointCount((int) weakCount);

            return item;
        }).sorted((a, b) -> {
            // 按平均分降序
            if (a.getAvgScore() == null && b.getAvgScore() == null) return 0;
            if (a.getAvgScore() == null) return 1;
            if (b.getAvgScore() == null) return -1;
            return Double.compare(b.getAvgScore(), a.getAvgScore());
        }).collect(Collectors.toList());
    }

    @Override
    public List<GradeScoreTrendVO> getGradeTrend(String grade) {
        List<User> students = getStudentsByGrade(grade);
        if (students.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = students.stream().map(User::getId).collect(Collectors.toList());

        LambdaQueryWrapper<ScoreRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ScoreRecord::getStudentId, ids)
               .orderByAsc(ScoreRecord::getExamDate);

        List<ScoreRecord> records = scoreRecordMapper.selectList(wrapper);

        // 按月份聚合（dateMonth 非数据库字段，须由 examDate 推导）
        Map<String, List<ScoreRecord>> byMonth = records.stream()
            .filter(r -> r.getExamDate() != null)
            .collect(Collectors.groupingBy(r -> YearMonth.from(r.getExamDate()).toString()));

        List<GradeScoreTrendVO> result = new ArrayList<>();
        byMonth.forEach((month, monthRecords) -> {
            GradeScoreTrendVO vo = new GradeScoreTrendVO();
            vo.setDateMonth(month);
            double avg = monthRecords.stream()
                .mapToInt(ScoreRecord::getScore)
                .average().orElse(0);
            vo.setAvgScore(Math.round(avg * 10.0) / 10.0);
            vo.setExamCount((long) monthRecords.size());
            result.add(vo);
        });

        result.sort(Comparator.comparing(GradeScoreTrendVO::getDateMonth));
        return result;
    }

    @Override
    public List<SubjectAvgVO> getSubjectAvgs(String grade) {
        List<User> students = getStudentsByGrade(grade);
        if (students.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = students.stream().map(User::getId).collect(Collectors.toList());

        LambdaQueryWrapper<ScoreRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ScoreRecord::getStudentId, ids);
        List<ScoreRecord> records = scoreRecordMapper.selectList(wrapper);

        Map<String, List<ScoreRecord>> bySubject = records.stream()
            .collect(Collectors.groupingBy(ScoreRecord::getSubject));

        List<SubjectAvgVO> result = new ArrayList<>();
        bySubject.forEach((subject, subjectRecords) -> {
            SubjectAvgVO vo = new SubjectAvgVO();
            vo.setSubject(subject);
            double avg = subjectRecords.stream()
                .mapToInt(ScoreRecord::getScore)
                .average().orElse(0);
            vo.setAvgScore(Math.round(avg * 10.0) / 10.0);
            result.add(vo);
        });

        result.sort(Comparator.comparing(SubjectAvgVO::getSubject));
        return result;
    }

    @Override
    public List<AttentionStudentVO> getAttentionStudents(String grade) {
        List<User> students = getStudentsByGrade(grade);
        if (students.isEmpty()) {
            return new ArrayList<>();
        }

        double gradeAvg = calcGradeAvgScore(
            students.stream().map(User::getId).collect(Collectors.toList())
        );
        double threshold = gradeAvg - 20;

        List<AttentionStudentVO> result = new ArrayList<>();
        for (User student : students) {
            Double studentAvg = calcStudentAvgScore(student.getId());
            if (studentAvg != null && studentAvg < threshold) {
                AttentionStudentVO vo = new AttentionStudentVO();
                vo.setId(student.getId());
                vo.setName(student.getName());
                vo.setGrade(student.getGrade());
                vo.setAvgScore(studentAvg);

                // 薄弱知识点
                LambdaQueryWrapper<KnowledgeMastery> kw = new LambdaQueryWrapper<>();
                kw.eq(KnowledgeMastery::getStudentId, student.getId())
                  .lt(KnowledgeMastery::getMastery, 60)
                  .orderByAsc(KnowledgeMastery::getMastery)
                  .last("LIMIT 1");
                KnowledgeMastery weakest = knowledgeMasteryMapper.selectOne(kw);
                if (weakest != null) {
                    vo.setWeakPointCount(knowledgeMasteryMapper.selectCount(
                        new LambdaQueryWrapper<KnowledgeMastery>()
                            .eq(KnowledgeMastery::getStudentId, student.getId())
                            .lt(KnowledgeMastery::getMastery, 60)
                    ).intValue());
                    vo.setWeakSubject(weakest.getSubject());
                } else {
                    vo.setWeakPointCount(0);
                }

                result.add(vo);
            }
        }

        // 按平均分升序（最需要关注的排前面）
        result.sort(Comparator.comparing(AttentionStudentVO::getAvgScore));
        return result;
    }

    @Override
    public boolean canViewStudent(Long teacherId, Long studentId) {
        if (teacherId == null || studentId == null) {
            return false;
        }
        User teacher = userMapper.selectById(teacherId);
        User student = userMapper.selectById(studentId);
        if (teacher == null || student == null) {
            return false;
        }
        // 只校验角色，不限制年级（教师可查看任何学生）
        return "teacher".equals(teacher.getRole()) && "student".equals(student.getRole());
    }

    // ========== 私有辅助方法 ==========

    /**
     * 根据年级获取学生列表
     */
    private List<User> getStudentsByGrade(String grade) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "student");
        if (grade != null && !grade.isBlank()) {
            wrapper.eq(User::getGrade, grade);
        }
        wrapper.orderByAsc(User::getName);
        return userMapper.selectList(wrapper);
    }

    /**
     * 计算年级平均分
     */
    private Double calcGradeAvgScore(List<Long> studentIds) {
        if (studentIds.isEmpty()) return 0.0;
        LambdaQueryWrapper<ScoreRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ScoreRecord::getStudentId, studentIds);
        List<ScoreRecord> records = scoreRecordMapper.selectList(wrapper);
        if (records.isEmpty()) return 0.0;
        return Math.round(records.stream()
            .mapToInt(ScoreRecord::getScore)
            .average().orElse(0) * 10.0) / 10.0;
    }

    /**
     * 计算单个学生平均分
     */
    private Double calcStudentAvgScore(Long studentId) {
        LambdaQueryWrapper<ScoreRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreRecord::getStudentId, studentId);
        List<ScoreRecord> records = scoreRecordMapper.selectList(wrapper);
        if (records.isEmpty()) return null;
        return Math.round(records.stream()
            .mapToInt(ScoreRecord::getScore)
            .average().orElse(0) * 10.0) / 10.0;
    }

    /**
     * 统计当月考试人次
     */
    private Long countMonthlyExams(List<Long> studentIds) {
        if (studentIds.isEmpty()) return 0L;
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());
        LambdaQueryWrapper<ScoreRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ScoreRecord::getStudentId, studentIds)
               .between(ScoreRecord::getExamDate, start, end);
        return scoreRecordMapper.selectCount(wrapper);
    }

    /**
     * 统计年级薄弱知识点总数
     */
    private Long countWeakPoints(List<Long> studentIds) {
        if (studentIds.isEmpty()) return 0L;
        LambdaQueryWrapper<KnowledgeMastery> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(KnowledgeMastery::getStudentId, studentIds)
               .lt(KnowledgeMastery::getMastery, 60);
        return knowledgeMasteryMapper.selectCount(wrapper);
    }

    // ========== 教师查看学生详情相关方法 ==========

    @Override
    public List<NoteVO> getStudentNotes(Long studentId) {
        log.info("[TeacherService] 获取学生笔记，studentId={}", studentId);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, studentId)
               .orderByDesc(Note::getUpdateTime);
        List<Note> notes = noteMapper.selectList(wrapper);
        return notes.stream().map(this::noteToVO).collect(Collectors.toList());
    }

    @Override
    public List<Plan> getStudentPlans(Long studentId) {
        log.info("[TeacherService] 获取学生学习计划，studentId={}", studentId);
        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Plan::getStudentId, studentId)
               .orderByDesc(Plan::getCreateTime);
        return planMapper.selectList(wrapper);
    }

    @Override
    public List<ScoreRecord> getStudentScores(Long studentId) {
        log.info("[TeacherService] 获取学生成绩记录，studentId={}", studentId);
        LambdaQueryWrapper<ScoreRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreRecord::getStudentId, studentId)
               .orderByDesc(ScoreRecord::getExamDate);
        return scoreRecordMapper.selectList(wrapper);
    }

    @Override
    public List<KnowledgeMastery> getStudentMasteries(Long studentId) {
        log.info("[TeacherService] 获取学生知识点掌握度，studentId={}", studentId);
        LambdaQueryWrapper<KnowledgeMastery> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeMastery::getStudentId, studentId);
        return knowledgeMasteryMapper.selectList(wrapper);
    }

    /**
     * 将Note实体转换为NoteVO
     */
    private NoteVO noteToVO(Note note) {
        NoteVO vo = new NoteVO();
        vo.setId(note.getId());
        vo.setTitle(note.getTitle());
        vo.setContent(note.getContent());
        vo.setSubject(note.getSubject());
        vo.setTags(note.getTags());
        vo.setCreateTime(note.getCreateTime());
        vo.setUpdateTime(note.getUpdateTime());
        return vo;
    }

    @Override
    public List<Map<String, Object>> getAllStudentNotes(String subject, Long studentId) {
        log.info("[TeacherService] 获取学生笔记列表，subject={}, studentId={}", subject, studentId);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(Note::getUserId, studentId);
        }
        if (subject != null && !subject.isBlank()) {
            wrapper.eq(Note::getSubject, subject);
        }
        wrapper.orderByDesc(Note::getUpdateTime);
        List<Note> notes = noteMapper.selectList(wrapper);

        // 转换为Map，加入学生姓名
        return notes.stream().map(note -> {
            User student = userMapper.selectById(note.getUserId());
            Map<String, Object> map = new HashMap<>();
            map.put("id", note.getId());
            map.put("title", note.getTitle());
            map.put("content", note.getContent());
            map.put("subject", note.getSubject());
            map.put("tags", note.getTags());
            map.put("createTime", note.getCreateTime());
            map.put("updateTime", note.getUpdateTime());
            map.put("studentId", note.getUserId());
            map.put("studentName", student != null ? student.getName() : "未知");
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getAllStudentPlans(String subject, Long studentId) {
        log.info("[TeacherService] 获取学生学习计划列表，subject={}, studentId={}", subject, studentId);
        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(Plan::getStudentId, studentId);
        }
        if (subject != null && !subject.isBlank()) {
            wrapper.eq(Plan::getSubject, subject);
        }
        wrapper.orderByDesc(Plan::getCreateTime);
        List<Plan> plans = planMapper.selectList(wrapper);

        // 转换为Map，加入学生姓名
        return plans.stream().map(plan -> {
            User student = userMapper.selectById(plan.getStudentId());
            Map<String, Object> map = new HashMap<>();
            map.put("id", plan.getId());
            map.put("content", plan.getContent());
            map.put("startDate", plan.getStartDate());
            map.put("endDate", plan.getEndDate());
            map.put("status", plan.getStatus());
            map.put("subject", plan.getSubject());
            map.put("progress", plan.getProgress());
            map.put("description", plan.getDescription());
            map.put("createTime", plan.getCreateTime());
            map.put("studentId", plan.getStudentId());
            map.put("studentName", student != null ? student.getName() : "未知");
            return map;
        }).collect(Collectors.toList());
    }
}
