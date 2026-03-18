package com.jins.aimentor.domain.vo;

import lombok.Data;

@Data
public class WeakPointVO {

    /**
     * 知识点名称
     */
    private String knowledge;

    /**
     * 掌握度（0-100）
     * <p>数值越低表示越薄弱</p>
     */
    private Integer mastery;
}