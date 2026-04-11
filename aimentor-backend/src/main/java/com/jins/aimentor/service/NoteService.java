package com.jins.aimentor.service;

import com.jins.aimentor.domain.vo.NoteVO;

import java.util.List;

/**
 * 学习笔记服务接口
 *
 * <p>定义笔记相关的业务方法</p>
 */
public interface NoteService {

    /**
     * 获取当前用户的所有笔记
     *
     * @return 按更新时间倒序的笔记列表
     */
    List<NoteVO> getMyNotes();

    /**
     * 根据科目筛选当前用户的笔记
     *
     * @param subject 科目名称
     * @return 该科目下的笔记列表
     */
    List<NoteVO> getNotesBySubject(String subject);

    /**
     * 根据标签筛选当前用户的笔记
     *
     * @param tag 标签名称
     * @return 含有该标签的笔记列表
     */
    List<NoteVO> getNotesByTag(String tag);

    /**
     * 按 ID 获取单条笔记（编辑回显）
     *
     * @param id 笔记ID
     * @return 笔记详情，未找到或不属于当前用户返回 null
     */
    NoteVO getNoteById(Long id);

    /**
     * 新增或更新笔记
     *
     * @param id       笔记ID（新增时为 null）
     * @param title    标题
     * @param content  内容
     * @param subject  科目
     * @param tags     标签
     * @return 新增后生成的笔记ID
     */
    Long saveNote(Long id, String title, String content, String subject, String tags);

    /**
     * 删除笔记（物理删除，内部走逻辑删除）
     *
     * @param id 笔记ID
     */
    void deleteNote(Long id);
}
