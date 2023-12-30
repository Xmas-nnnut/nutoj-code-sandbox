package com.xqj.nutojcodesandbox.service.impl;

import com.xqj.nutojcodesandbox.model.dto.ExecuteCodeRequest;
import com.xqj.nutojcodesandbox.model.dto.ExecuteCodeResponse;
import com.xqj.nutojcodesandbox.service.CodeSandboxService;
import com.xqj.nutojcodesandbox.template.cpp.CppSandboxTemplate;
import com.xqj.nutojcodesandbox.template.java.JavaCodeSandboxTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class CodeSandboxServiceImpl implements CodeSandboxService {
    @Resource(name = "javaNativeArgsSandbox")
    private JavaCodeSandboxTemplate javaNativeArgsCodeSandbox;
    @Resource(name = "javaNativeAcmSandbox")
    private JavaCodeSandboxTemplate javaNativeAcmCodeSandbox;
    @Resource(name = "javaDockerArgsSandbox")
    private JavaCodeSandboxTemplate javaDockerArgsCodeSandbox;
    @Resource
    private CppSandboxTemplate cppNativeAcmSandbox;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        switch (language) {
            case "java-args":
                return javaNativeArgsCodeSandbox.executeCode(inputList, code);
            case "java-docker-args":
                return javaDockerArgsCodeSandbox.executeCode(inputList, code);
            case "java-acm":
                return javaNativeAcmCodeSandbox.executeCode(inputList, code);
            case "cpp":
                return cppNativeAcmSandbox.executeCppCode(inputList, code);
            default:
                return null;
        }

    }
}
