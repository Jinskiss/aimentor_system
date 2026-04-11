package com.jins.aimentor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jins.aimentor.domain.entity.Note;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习笔记Mapper接口
 *
 * <p>继承BaseMapper获得基础CRUD能力</p>
 * <p>自定义复杂查询可在此添加方法</p>
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}
