package com.xqj.nutojcodesandbox.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  判题信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeInfo {

    /**
     *  程序执行信息
     */
    private String message;

    /**
     *  消耗时间（ms）
     */
    private Long time;

    /**
     *  消耗内存（KB）
     */
    private Long memory;
}
