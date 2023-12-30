package com.xqj.nutojcodesandbox.template.java.Acm;

import cn.hutool.core.util.StrUtil;

import com.xqj.nutojcodesandbox.model.dto.ExecuteMessage;
import com.xqj.nutojcodesandbox.template.java.JavaCodeSandboxTemplate;
import com.xqj.nutojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.xqj.nutojcodesandbox.constant.CodeSandboxConstants.TIME_OUT;


@Service
@Slf4j
public class JavaNativeAcmSandbox extends JavaCodeSandboxTemplate {

    @Override
    protected List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) throws Exception {
        String dir = userCodeFile.getParentFile().getAbsolutePath();
        // 3. 执行代码，得到输出结果
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String input : inputList) {
            //Linux下的命令
//            String runCmd = String.format("/software/jdk1.8.0_301/bin/java -Xmx256m -Dfile.encoding=UTF-8 -cp %s:%s -Djava.security.manager=%s Main", dir, SECURITY_MANAGER_PATH, SECURITY_MANAGER_CLASS_NAME);
            //Windows下的命令
//            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main", dir, SECURITY_MANAGER_PATH, SECURITY_MANAGER_CLASS_NAME);
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main", dir);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            Process runProcess = Runtime.getRuntime().exec(runCmd);
            // 超时控制
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(TIME_OUT);
                    //超时了
                    runProcess.destroy();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();

            ExecuteMessage executeMessage = null;
            try {
                executeMessage = ProcessUtils.getAcmProcessMessage(runProcess, input);
            } catch (Exception e){
                log.error("执行出错: {}", e.toString());
            }
            stopWatch.stop();
            if(!thread.isAlive()){
                executeMessage = new ExecuteMessage();
                executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
                executeMessage.setErrorMessage("超出时间限制");
            }
            executeMessageList.add(executeMessage);

            //已经有用例失败了
            if(StrUtil.isNotBlank(executeMessage.getErrorMessage())){
                break;
            }
        }
        return executeMessageList;
    }
}