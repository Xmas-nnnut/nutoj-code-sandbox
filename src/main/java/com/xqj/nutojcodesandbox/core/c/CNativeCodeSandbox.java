package com.xqj.nutojcodesandbox.core.c;

import com.xqj.nutojcodesandbox.core.CodeSandboxTemplate;
import com.xqj.nutojcodesandbox.model.dto.CodeSandboxCmd;
import lombok.extern.slf4j.Slf4j;

import java.io.File;


/**
 * cpp本机代码沙箱
 *
 */
@Slf4j
public class CNativeCodeSandbox extends CodeSandboxTemplate {
    private static final String PREFIX = File.separator + "c";

    private static final String GLOBAL_CODE_DIR_PATH = File.separator + "tmpCode";

    private static final String GLOBAL_CPP_NAME = File.separator + "Main.c";

    public CNativeCodeSandbox() {
        super.prefix = PREFIX;
        super.globalCodeDirPath = GLOBAL_CODE_DIR_PATH;
        super.globalCodeFileName = GLOBAL_CPP_NAME;
    }

    @Override
    public CodeSandboxCmd getCmd(String userCodeParentPath, String userCodePath) {
        return CodeSandboxCmd
                .builder()
                .compileCmd(String.format("gcc -finput-charset=UTF-8 -fexec-charset=UTF-8 %s -o %s", userCodePath, userCodePath.substring(0, userCodePath.length() - 2)))
                .runCmd(userCodeParentPath + File.separator + "main")
                .build();
    }
}
