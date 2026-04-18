package com.jins.aimentor.service;

import com.jins.aimentor.domain.vo.AttentionStudentVO;
import com.jins.aimentor.domain.vo.GradeScoreTrendVO;
import com.jins.aimentor.domain.vo.StudentListItemVO;
import com.jins.aimentor.domain.vo.SubjectAvgVO;
import com.jins.aimentor.domain.vo.TeacherDashboardVO;
import com.jins.aimentor.domain.vo.NoteVO;
import com.jins.aimentor.domain.entity.Plan;
import com.jins.aimentor.domain.entity.ScoreRecord;
import com.jins.aimentor.domain.entity.KnowledgeMastery;

import java.util.List;
import java.util.Map;

/**
 * 教师端服务接口
 *
 * <p>提供教师查看和管理学生学情的业务方法</p>
 */
public interface TeacherService {

    /**
     * 获取教师 Dashboard 汇总数据
     *
     * <p>根据教师所在年级，聚合所有学生的成绩和知识点数据</p>
     *
     * @param teacherId 教师用户 ID
     * @return Dashboard 数据（含统计卡片、趋势图、雷达图、待关注学生）
     */
    TeacherDashboardVO getDashboardData(Long teacherId);

    /**
     * 获取年级下的学生列表（带学情摘要）
     *
     * @param grade 年级名称（如"高一"），传 null 则返回所有年级学生
     * @return 学生列表（按平均分降序）
     */
    List<StudentListItemVO> getStudentList(String grade);

    /**
     * 获取年级成绩趋势（按月聚合）
     *
     * @param grade 年级名称
     * @return 月度趋势列表
     */
    List<GradeScoreTrendVO> getGradeTrend(String grade);

    /**
     * 获取年级各科目平均分（雷达图用）
     *
     * @param grade 年级名称
     * @return 科目平均分列表
     */
    List<SubjectAvgVO> getSubjectAvgs(String grade);

    /**
     * 获取待关注学生列表（平均分低于年级平均 20 分）
     *
     * @param grade 年级名称
     * @return 待关注学生列表
     */
    List<AttentionStudentVO> getAttentionStudents(String grade);

    /**
     * 教师是否可查看指定学生学情（同年级，或教师未绑定年级则可看全部学生）
     *
     * @param teacherId 教师用户 ID
     * @param studentId 学生用户 ID
     * @return 是否允许
     */
    boolean canViewStudent(Long teacherId, Long studentId);

    /**
     * 获取指定学生的所有笔记（教师用）
     *
     * @param studentId 学生ID
     * @return 笔记列表
     */
    List<NoteVO> getStudentNotes(Long studentId);

    /**
     * 获取指定学生的学习计划（教师用）
     *
     * @param studentId 学生ID
     * @return 计划列表
     */
    List<Plan> getStudentPlans(Long studentId);

    /**
     * 获取指定学生的成绩记录（教师用）
     *
     * @param studentId 学生ID
     * @return 成绩记录列表
     */
    List<ScoreRecord> getStudentScores(Long studentId);

    /**
     * 获取指定学生的知识点掌握度（教师用）
     *
     * @param studentId 学生ID
     * @return 知识点掌握度列表
     */
    List<KnowledgeMastery> getStudentMasteries(Long studentId);

    /**
     * 获取所有学生的笔记（教师端列表用）
     *
     * @param subject 科目筛选（可选）
     * @param studentId 学生ID筛选（可选）
     * @return 包含学生信息的笔记列表
     */
    List<Map<String, Object>> getAllStudentNotes(String subject, Long studentId);

    /**
     * 获取所有学生的学习计划（教师端列表用）
     *
     * @param subject 科目筛选（可选）
     * @param studentId 学生ID筛选（可选）
     * @return 包含学生信息的计划列表
     */
    List<Map<String, Object>> getAllStudentPlans(String subject, Long studentId);
}
