package com.xqj.nutojcodesandbox.core;

import com.xqj.nutojcodesandbox.model.dto.ExecuteCodeRequest;
import com.xqj.nutojcodesandbox.model.dto.ExecuteCodeResponse;

/**
 * 定义代码沙箱的接口，提高通用性
 * 之后的项目代码只调用接口，不调用具体的实现类，这样在使用其他的代码沙箱实现类时，就不用去修改，便于扩展。
 */
public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest 执行代码请求
     * @return
     */

    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
