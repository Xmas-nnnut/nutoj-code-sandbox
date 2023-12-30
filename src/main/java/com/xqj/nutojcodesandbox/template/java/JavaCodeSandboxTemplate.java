package com.xqj.nutojcodesandbox.template.java;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.xqj.nutojcodesandbox.model.dto.ExecuteCodeRequest;
import com.xqj.nutojcodesandbox.model.dto.ExecuteCodeResponse;
import com.xqj.nutojcodesandbox.model.dto.ExecuteMessage;
import com.xqj.nutojcodesandbox.model.dto.JudgeInfo;
import com.xqj.nutojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.xqj.nutojcodesandbox.constant.CodeSandboxConstants.*;


/**
 * Java 代码沙箱模板方法的实现
 * 模板方法设计模式
 */
@Slf4j
public abstract class JavaCodeSandboxTemplate {

    private static final WordTree WORD_TREE;

    static {
        WORD_TREE = new WordTree();
        WORD_TREE.addWords("Files", "exec");
    }

    public final ExecuteCodeResponse executeCode(List<String> inputList, String code) {

        // 1. 把用户的代码保存为文件
        File userCodeFile = saveCodeToFile(code);

        // 2. 编译代码，得到 class 文件
        ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
        System.out.println(compileFileExecuteMessage);

        // 3. 执行代码，得到输出结果
        List<ExecuteMessage> executeMessageList;
        try {
            executeMessageList = runFile(userCodeFile, inputList);
        } catch (Exception e) {
            throw new RuntimeException("执行错误", e);
        }

        // 4. 收集整理输出结果
        ExecuteCodeResponse outputResponse = getOutputResponse(executeMessageList);

        // 5. 文件清理
        boolean b = deleteFile(userCodeFile);
        if (!b) {
            log.error("deleteFile error, userCodeFilePath = {}", userCodeFile.getAbsolutePath());
        }
        return outputResponse;
    }


    /**
     * 1. 把用户的代码保存为文件
     *
     * @param code 用户代码
     * @return
     */
    protected File saveCodeToFile(String code) {
        //检查代码内容，是否有黑名单代码
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if (foundWord != null) {
            throw new RuntimeException("代码不合法");
        }
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 判断全局代码目录是否存在，没有则新建
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }
        // 把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        return userCodeFile;
    }

    /**
     * 2、编译代码
     *
     * @param userCodeFile
     * @return
     */
    protected ExecuteMessage compileFile(File userCodeFile) {
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.getProcessMessage(compileProcess, "编译");
            if (executeMessage.getExitValue() != 0) {
                throw new RuntimeException("编译错误");
            }
            return executeMessage;
        } catch (Exception e) {
            // todo
//            return getErrorResponse(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 3、执行文件，获得执行结果列表
     * 运行代码，这一步针对Args和ACM有不同实现
     *
     * @param userCodeFile
     * @param inputList
     * @return
     */
    protected abstract List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) throws Exception;

//    /**
//     * 3、执行文件，获得执行结果列表
//     * @param userCodeFile
//     * @param inputList
//     * @return
//     */
//    protected List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
//        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
//
//        List<ExecuteMessage> executeMessageList = new ArrayList<>();
//        for (String inputArgs : inputList) {
//            // todo 安全检查
////            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s",
////                    userCodeParentPath, SECURITY_MANAGER_PATH, SECURITY_MANAGER_CLASS_NAME, inputArgs);
//            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
//            try {
//                Process runProcess = Runtime.getRuntime().exec(runCmd);
//                // 超时控制
//                new Thread(() -> {
//                    try {
//                        Thread.sleep(TIME_OUT);
//                        System.out.println("超时了，中断");
//                        runProcess.destroy();
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }).start();
//                ExecuteMessage executeMessage = ProcessUtils.getProcessMessage(runProcess, "运行");
//                System.out.println(executeMessage);
//                executeMessageList.add(executeMessage);
//            } catch (Exception e) {
//                throw new RuntimeException("执行错误", e);
//            }
//        }
//        return executeMessageList;
//    }

    /**
     * 4、收集整理运行结果
     *
     * @param executeMessageList
     * @return
     */
    protected ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 取用时最大值，便于判断是否超时
        long maxTime = 0;
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                // 用户提交的代码执行中存在错误
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
        }
        // 正常运行完成
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        // 要借助第三方库来获取内存占用，非常麻烦，此处不做实现
//        judgeInfo.setMemory();
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 5、删除文件
     *
     * @param userCodeFile
     * @return
     */
    protected boolean deleteFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }

    /**
     * todo
     * 6、获取错误响应
     *
     * @param e
     * @return
     */
    protected ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        // 表示代码沙箱错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
