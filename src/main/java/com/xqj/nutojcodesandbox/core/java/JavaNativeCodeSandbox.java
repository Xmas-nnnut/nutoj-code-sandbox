package com.xqj.nutojcodesandbox.core.java;

import com.xqj.nutojcodesandbox.core.CodeSandboxTemplate;
import com.xqj.nutojcodesandbox.model.dto.CodeSandboxCmd;
import lombok.extern.slf4j.Slf4j;

import java.io.File;


/**
 * java本机代码沙箱
 *
 */
@Slf4j
public class JavaNativeCodeSandbox extends CodeSandboxTemplate {
    private static final String PREFIX = File.separator + "java";

    private static final String GLOBAL_CODE_DIR_PATH = File.separator + "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = File.separator + "Main.java";

    public JavaNativeCodeSandbox() {
        super.prefix = PREFIX;
        super.globalCodeDirPath = GLOBAL_CODE_DIR_PATH;
        super.globalCodeFileName = GLOBAL_JAVA_CLASS_NAME;
    }

    @Override
    public CodeSandboxCmd getCmd(String userCodeParentPath, String userCodePath) {
        return CodeSandboxCmd
                .builder()
                .compileCmd(String.format("javac -encoding utf-8 %s", userCodePath))
                .runCmd(String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main", userCodeParentPath))
                .build();
    }
}
