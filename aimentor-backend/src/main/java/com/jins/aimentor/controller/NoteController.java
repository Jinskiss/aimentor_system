package com.jins.aimentor.controller;

import com.jins.aimentor.common.Result;
import com.jins.aimentor.constants.Status;
import com.jins.aimentor.domain.vo.NoteVO;
import com.jins.aimentor.service.NoteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习笔记控制器
 *
 * <p>提供笔记的增删改查接口，所有操作基于当前登录用户</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/note")
@RequiredArgsConstructor
@Api(tags = "学习笔记", description = "学习笔记管理接口")
public class NoteController {

    private final NoteService noteService;

    @ApiOperation("获取当前用户的所有笔记")
    @GetMapping("/my")
    public Result<List<NoteVO>> getMyNotes(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String tag) {
        if (subject != null && !subject.isBlank()) {
            return Result.success(noteService.getNotesBySubject(subject));
        }
        if (tag != null && !tag.isBlank()) {
            return Result.success(noteService.getNotesByTag(tag));
        }
        return Result.success(noteService.getMyNotes());
    }

    @ApiOperation("获取单条笔记详情")
    @GetMapping("/{id}")
    public Result<NoteVO> getById(@PathVariable Long id) {
        NoteVO vo = noteService.getNoteById(id);
        if (vo == null) {
            return Result.error(Status.CODE_404, "笔记不存在");
        }
        return Result.success(vo);
    }

    @ApiOperation("新建或更新笔记")
    @PostMapping
    public Result<Long> saveNote(@RequestBody NoteSaveDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            return Result.error(Status.CODE_400, "笔记标题不能为空");
        }
        Long id = noteService.saveNote(
                dto.getId(),
                dto.getTitle().trim(),
                dto.getContent(),
                dto.getSubject(),
                dto.getTags()
        );
        return Result.success(id);
    }

    @ApiOperation("删除笔记")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return Result.success();
    }

    /**
     * 笔记保存请求体
     */
    public static class NoteSaveDto {
        private Long id;
        private String title;
        private String content;
        private String subject;
        private String tags;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
    }
}
