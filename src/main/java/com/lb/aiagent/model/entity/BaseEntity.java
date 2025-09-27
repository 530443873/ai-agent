package com.lb.aiagent.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import java.util.Date;

public class BaseEntity {
    /**
     * 创建时间
     */
    protected Date createdTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedTime;
}
