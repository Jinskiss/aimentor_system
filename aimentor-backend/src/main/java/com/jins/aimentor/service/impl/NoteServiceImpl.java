package com.jins.aimentor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jins.aimentor.domain.entity.Note;
import com.jins.aimentor.domain.vo.NoteVO;
import com.jins.aimentor.mapper.NoteMapper;
import com.jins.aimentor.service.NoteService;
import com.jins.aimentor.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学习笔记服务实现
 *
 * <p>实现笔记的增删改查业务逻辑</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    @Override
    public List<NoteVO> getMyNotes() {
        Long userId = UserHolder.getUser().getId();
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
               .orderByDesc(Note::getUpdateTime);
        List<Note> list = list(wrapper);
        log.info("[NoteService] 获取用户笔记，userId={}，共 {} 条", userId, list.size());
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<NoteVO> getNotesBySubject(String subject) {
        Long userId = UserHolder.getUser().getId();
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
               .eq(StringUtils.hasLength(subject), Note::getSubject, subject)
               .orderByDesc(Note::getUpdateTime);
        List<Note> list = list(wrapper);
        log.info("[NoteService] 按科目筛选笔记，userId={}，subject={}，共 {} 条", userId, subject, list.size());
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<NoteVO> getNotesByTag(String tag) {
        Long userId = UserHolder.getUser().getId();
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
               .like(StringUtils.hasLength(tag), Note::getTags, tag)
               .orderByDesc(Note::getUpdateTime);
        List<Note> list = list(wrapper);
        log.info("[NoteService] 按标签筛选笔记，userId={}，tag={}，共 {} 条", userId, tag, list.size());
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public NoteVO getNoteById(Long id) {
        if (id == null) {
            return null;
        }
        Long userId = UserHolder.getUser().getId();
        Note entity = getById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            return null;
        }
        return toVO(entity);
    }

    @Override
    public Long saveNote(Long id, String title, String content, String subject, String tags) {
        Long userId = UserHolder.getUser().getId();
        Note entity;
        if (id != null) {
            entity = getById(id);
            if (entity == null || !entity.getUserId().equals(userId)) {
                throw new IllegalArgumentException("笔记不存在或无权限修改");
            }
        } else {
            entity = new Note();
            entity.setUserId(userId);
        }
        entity.setTitle(title);
        entity.setContent(content);
        entity.setSubject(subject);
        entity.setTags(tags);
        saveOrUpdate(entity);
        log.info("[NoteService] 保存笔记，id={}，userId={}，title={}", entity.getId(), userId, title);
        return entity.getId();
    }

    @Override
    public void deleteNote(Long id) {
        Long userId = UserHolder.getUser().getId();
        Note entity = getById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new IllegalArgumentException("笔记不存在或无权限删除");
        }
        removeById(id);
        log.info("[NoteService] 删除笔记，id={}，userId={}", id, userId);
    }

    /**
     * 将实体对象转换为视图对象
     */
    private NoteVO toVO(Note entity) {
        NoteVO vo = new NoteVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());
        vo.setSubject(entity.getSubject());
        vo.setTags(entity.getTags());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
