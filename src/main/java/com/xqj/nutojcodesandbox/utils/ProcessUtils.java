package com.xqj.nutojcodesandbox.utils;

import cn.hutool.core.util.StrUtil;
import com.xqj.nutojcodesandbox.model.dto.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StopWatch;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 进程工具类
 */
@Slf4j
public class ProcessUtils {

    /**
     * 获取进程执行信息
     * @param process
     * @return
     */
    public static ExecuteMessage getProcessMessage(Process process, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            //计时
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // 等待程序执行，获取退出码
            int exitValue = process.waitFor();
            executeMessage.setExitValue(exitValue);
            // 错误退出
            if(exitValue != 0){
                executeMessage.setErrorMessage(getProcessOutput(process.getErrorStream()));
            } else {
                executeMessage.setMessage(getProcessOutput(process.getInputStream()));
            }

            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (Exception e) {
            log.error(opName + "失败：{}", e.toString());
        }
        return executeMessage;
    }

    /**
     * 执行交互式进程并获取信息
     * @param runProcess
     * @param input
     * @return
     */
    public static ExecuteMessage getAcmProcessMessage(Process runProcess, String input) throws IOException {
        ExecuteMessage executeMessage = new ExecuteMessage();

        StringReader inputReader = new StringReader(input);
        BufferedReader inputBufferedReader = new BufferedReader(inputReader);

        //计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        //输入（模拟控制台输入）
        PrintWriter consoleInput = new PrintWriter(runProcess.getOutputStream());
        String line;
        while ((line = inputBufferedReader.readLine()) != null) {
            consoleInput.println(line);
            consoleInput.flush();
        }
        consoleInput.close();

        //获取输出
        BufferedReader userCodeOutput = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
        List<String> outputList = new ArrayList<>();
        String outputLine;
        while ((outputLine = userCodeOutput.readLine()) != null) {
            outputList.add(outputLine);
        }
        userCodeOutput.close();

        //获取错误输出
        BufferedReader errorOutput = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
        List<String> errorList = new ArrayList<>();
        String errorLine;
        while ((errorLine = errorOutput.readLine()) != null) {
            errorList.add(errorLine);
        }
        errorOutput.close();

        stopWatch.stop();
        executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        executeMessage.setMessage(StringUtils.join(outputList, "\n"));
        executeMessage.setErrorMessage(StringUtils.join(errorList, "\n"));
        runProcess.destroy();

        return executeMessage;
    }

    /**
     * 获取某个流的输出
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String getProcessOutput(InputStream inputStream) throws IOException {
        // 分批获取进程的正常输出
        // Linux写法
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        //Windows写法
        // BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "GBK"));
        StringBuilder outputSb = new StringBuilder();
        // 逐行读取
        String outputLine;
        while ((outputLine = bufferedReader.readLine()) != null) {
            outputSb.append(outputLine).append("\n");
        }
        bufferedReader.close();
        return outputSb.toString();
    }

    /**
     * 执行进程并获取信息(old)
     *
     * @param runProcess
     * @param opName
     * @return
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 等待程序执行，获取错误码
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            // 正常退出
            if (exitValue == 0) {
                System.out.println(opName + "成功");
                // 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                List<String> outputStrList = new ArrayList<>();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compileOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(outputStrList, "\n"));
            } else {
                // 异常退出
                System.out.println(opName + "失败，错误码： " + exitValue);
                // 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                List<String> outputStrList = new ArrayList<>();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compileOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(outputStrList, "\n"));

                // 分批获取进程的错误输出
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                // 逐行读取
                List<String> errorOutputStrList = new ArrayList<>();
                // 逐行读取
                String errorCompileOutputLine;
                while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
                    errorOutputStrList.add(errorCompileOutputLine);
                }
                executeMessage.setErrorMessage(StringUtils.join(errorOutputStrList, "\n"));
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;
    }

    /**
     * 执行交互式进程并获取信息(old)
     *
     * @param runProcess
     * @param args
     * @return
     */
    public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String args) {
        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            // 向控制台输入程序
            OutputStream outputStream = runProcess.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            String[] s = args.split(" ");
            String join = StrUtil.join("\n", s) + "\n";
            outputStreamWriter.write(join);
            // 相当于按了回车，执行输入的发送
            outputStreamWriter.flush();

            // 分批获取进程的正常输出
            InputStream inputStream = runProcess.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder compileOutputStringBuilder = new StringBuilder();
            // 逐行读取
            String compileOutputLine;
            while ((compileOutputLine = bufferedReader.readLine()) != null) {
                compileOutputStringBuilder.append(compileOutputLine);
            }
            executeMessage.setMessage(compileOutputStringBuilder.toString());
            // 记得资源的释放，否则会卡死
            outputStreamWriter.close();
            outputStream.close();
            inputStream.close();
            runProcess.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;
    }
}