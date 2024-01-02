package com.xqj.nutojcodesandbox.core;

import com.xqj.nutojcodesandbox.core.c.CNativeCodeSandbox;
import com.xqj.nutojcodesandbox.core.cpp.CppNativeCodeSandbox;
import com.xqj.nutojcodesandbox.core.java.JavaNativeCodeSandbox;

public class CodeSandboxFactory {
    public static CodeSandboxTemplate getInstance(String language) {
        switch (language) {
            case "java":
                return new JavaNativeCodeSandbox();
            case "cpp":
                return new CppNativeCodeSandbox();
            case "c":
                return new CNativeCodeSandbox();
            default:
                throw new RuntimeException("暂不支持");
        }
    }
}
