package com.lb.aiagent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 违禁词
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "prohibited_words")
public class ProhibitedWords extends BaseEntity {

    public ProhibitedWords(String word, Date date) {
        this.word = word;
        super.createdTime = date;
    }

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 违禁词
     */
    private String word;

    /**
     * 分类，0其他、1政治、2色情、3暴力、4辱骂、5广告
     */
    private Integer category;

    /**
     * 风险等级：1-高风险，2-中风险，3-低风险
     */
    private Integer riskLevel;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建人
     */
    private String creator;
}
