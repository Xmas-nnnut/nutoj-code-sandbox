package com.xqj.nutojcodesandbox.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 进程执行信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteMessage {

    /**
     * 退出码
     */
    private Integer exitCode;

    /**
     * 正常信息
     */
    private String message;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 运行时间
     */
    private Long time;

    /**
     * 消耗内存
     */
    private Long memory;

}
