package com.jins.aimentor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource")
public class Resource {
    
    /** 资源ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 资源标题 */
    private String title;
    
    /** 资源描述 */
    private String description;
    
    /** 资源类型：视频/文档/练习等 */
    private String type;
    
    /** 资源链接 */
    private String url;
    
    /** 所属科目 */
    private String subject;
    
    /** 推荐分数（用于排序） */
    private Integer recommendScore;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}