package com.xqj.nutojcodesandbox.template.java.Args;

import com.xqj.nutojcodesandbox.model.dto.ExecuteMessage;
import com.xqj.nutojcodesandbox.template.java.JavaCodeSandboxTemplate;
import com.xqj.nutojcodesandbox.utils.ProcessUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.xqj.nutojcodesandbox.constant.CodeSandboxConstants.TIME_OUT;


@Service
public class JavaNativeArgsSandbox extends JavaCodeSandboxTemplate {
    @Override
    protected List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) throws Exception {
        String dir = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> messages = new ArrayList<>();
        for (String input : inputList) {
            //Linux下的命令
//            String runCmd = String.format("/software/jdk1.8.0_301/bin/java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", dir, input);
            //Windows下命令
//            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", dir, input);
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", dir, input);
            Process runProcess = Runtime.getRuntime().exec(runCmd);
            // 超时控制
            new Thread(() -> {
                try {
                    Thread.sleep(TIME_OUT);
                    System.out.println("超时了，中断");
                    runProcess.destroy();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            ExecuteMessage executeMessage = ProcessUtils.getProcessMessage(runProcess, "运行");
            messages.add(executeMessage);
        }
        return messages;
    }
}
