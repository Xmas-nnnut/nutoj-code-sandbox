package com.xqj.nutojcodesandbox.service;

import com.xqj.nutojcodesandbox.model.dto.ExecuteCodeRequest;
import com.xqj.nutojcodesandbox.model.dto.ExecuteCodeResponse;

/**
 * 定义代码沙箱的接口，提高通用性
 * 之后的项目代码只调用接口，不调用具体的实现类，这样在使用其他的代码沙箱实现类时，就不用去修改，便于扩展。
 */
public interface CodeSandboxService {

    /**
     *  执行代码
     *  todo: 扩展思路：增加一个查看代码沙箱状态的接口
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
