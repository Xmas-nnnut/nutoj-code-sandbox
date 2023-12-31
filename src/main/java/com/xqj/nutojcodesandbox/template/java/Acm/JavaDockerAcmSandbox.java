package com.xqj.nutojcodesandbox.template.java.Acm;

import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.xqj.nutojcodesandbox.model.dto.ExecuteMessage;
import com.xqj.nutojcodesandbox.template.java.JavaCodeSandboxTemplate;

import java.io.File;
import java.util.List;

public class JavaDockerAcmSandbox extends JavaCodeSandboxTemplate {
    @Override
    protected List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) throws Exception {
        String dir = userCodeFile.getParentFile().getAbsolutePath();
        String[] cmdArray = {"/bin/sh", "-c", "echo " + inputList + " | java -cp /app Main"};
//        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
//                .withCmd(cmdArray)
//                .withAttachStderr(true)
//                .withAttachStdin(true)
//                .withAttachStdout(true)
//                .exec();
        return null;
    }
}
