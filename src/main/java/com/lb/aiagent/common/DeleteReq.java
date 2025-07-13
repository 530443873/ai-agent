package com.lb.aiagent.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteReq implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
