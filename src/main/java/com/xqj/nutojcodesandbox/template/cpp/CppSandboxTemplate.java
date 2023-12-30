package com.xqj.nutojcodesandbox.template.cpp;

import com.xqj.nutojcodesandbox.model.dto.ExecuteCodeResponse;

import java.util.List;

public abstract class CppSandboxTemplate {
    public final ExecuteCodeResponse executeCppCode(List<String> inputList, String code){
        step1();
        step2();
        step3();
        return null;
    }

    protected abstract void step1();
    protected abstract void step2();
    protected abstract void step3();

}
