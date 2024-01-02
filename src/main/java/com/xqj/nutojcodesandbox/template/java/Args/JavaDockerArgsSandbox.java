package com.xqj.nutojcodesandbox.template.java.Args;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.InvocationBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.xqj.nutojcodesandbox.model.dto.ExecuteCodeResponse;
import com.xqj.nutojcodesandbox.model.dto.ExecuteMessage;
import com.xqj.nutojcodesandbox.template.java.JavaCodeSandboxTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.xqj.nutojcodesandbox.constant.CodeSandboxConstants.*;

@Service
public class JavaDockerArgsSandbox extends JavaCodeSandboxTemplate {
    public static void main(String[] args) {
        JavaDockerArgsSandbox javaDockerArgsSandbox = new JavaDockerArgsSandbox();
        List<String> inputList = Arrays.asList("1 2", "1 3");
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/unsafeCode/RunFileError.java", StandardCharsets.UTF_8);
//        String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
        ExecuteCodeResponse executeCodeResponse = javaDockerArgsSandbox.executeCode(inputList, code);
        System.out.println(executeCodeResponse);
    }

    @Override
    protected List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) throws Exception {
        String dir = userCodeFile.getParentFile().getAbsolutePath();

        // 获取默认的 Docker Client
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();

        // 1、拉取镜像（镜像不存在）
        List<Image> images = dockerClient.listImagesCmd().exec();
        boolean imageExists = images.stream()
                .anyMatch(image -> Arrays.asList(image.getRepoTags()).contains(JAVA_IMAGE_NAME));

        if (!imageExists) {
            // 拉取镜像
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(JAVA_IMAGE_NAME);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    String status = item.getStatus();
                    System.out.println("下载镜像：" + status);
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd
                        .exec(pullImageResultCallback)
                        .awaitCompletion();
                System.out.println("下载完成");
            } catch (InterruptedException e) {
                System.out.println("拉取镜像异常");
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("镜像已经存在");
        }

        // 2、创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(JAVA_IMAGE_NAME);
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(100 * 1000 * 1000L);
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);
        //        hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"));
        // 与主机上的目录进行绑定
        hostConfig.setBinds(new Bind(dir, new Volume("/app")));
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true) //禁用联网功能
                .withReadonlyRootfs(true) //禁止往root目录写文件
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();
        System.out.println(createContainerResponse);
        String containerId = createContainerResponse.getId();

        // 3、启动容器
        dockerClient.startContainerCmd(containerId).exec();
        // docker exec keen_blackwell java -cp /app Main 1 3
        // 执行命令并获取结果
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            String[] inputArgsArray = inputArgs.split(" ");
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();
            System.out.println("创建执行命令：" + execCreateCmdResponse);
            String execId = execCreateCmdResponse.getId();
            // 新建结果信息
            ExecuteMessage executeMessage = new ExecuteMessage();
            final String[] message = {null};
            final String[] errorMessage = {null};
            long time = 0L;
            // 判断是否超时
            final boolean[] isTimeout = {true};
            // 定义一个回调函数
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onComplete() {
                    // 如果执行完成，则表示没超时
                    System.out.println("没有超时");
                    isTimeout[0] = false;
                    super.onComplete();
                }

                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        errorMessage[0] = new String(frame.getPayload());
                        System.out.println("输出错误结果：" + errorMessage[0]);
                    } else {
                        message[0] = new String(frame.getPayload());
                        System.out.println("输出结果：" + message[0]);
                    }
                    super.onNext(frame);
                }
            };
            // 获取占用的内存
            final long[] maxMemory = {0L};
            // 定义获取容器状态的命令
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            // 定义用于处理容器统计信息的回调方法
            InvocationBuilder.AsyncResultCallback<Statistics> statisticsResultCallback = new InvocationBuilder.AsyncResultCallback<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    Long usage = statistics.getMemoryStats().getUsage();
                    if (usage == null) {
                        usage = 0L;
                    }
                    System.out.println("内存占用：" + usage);
                    maxMemory[0] = Math.max(maxMemory[0], usage);
                }
            };
            statsCmd.exec(statisticsResultCallback);
            try {
                // 记时开始
                stopWatch.start();
                dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
//                        .awaitCompletion();//阻塞程序持续完成
                        .awaitCompletion(TIME_OUT, TimeUnit.MICROSECONDS);
                // 记时结束
                stopWatch.stop();
                time = stopWatch.getLastTaskTimeMillis();
                System.out.println("记时完成");
                // 关闭内存占用计算
                statisticsResultCallback.close();
            } catch (InterruptedException e) {
                System.out.println("程序执行异常");
                throw new RuntimeException(e);
            }
            // 4、收集结果设置信息
            executeMessage.setMessage(message[0]);
            executeMessage.setErrorMessage(errorMessage[0]);
            executeMessage.setTime(time);
            executeMessage.setMemory(maxMemory[0]);
            executeMessageList.add(executeMessage);
        }
        // todo 停止并销毁容器
        dockerClient.stopContainerCmd(containerId).exec();
        dockerClient.removeContainerCmd(containerId).exec();
        System.out.println("销毁容器成功");

        return executeMessageList;
    }

//    public Long getUsage(DockerClient dockerClient, String containerId) {
//        StatsCmd statsCmd = dockerClient.statsCmd(containerId);
//        InvocationBuilder.AsyncResultCallback<Statistics> callback = new InvocationBuilder.AsyncResultCallback<>();
//        statsCmd.exec(callback);
//        Long usage = 0L;
//        try {
//            usage = callback.awaitResult().getMemoryStats().getUsage();
//            callback.close();
//        } catch (RuntimeException | IOException e) {
//            // you may want to throw an exception here
//        }
//        return usage; // this may be null or invalid if the container has terminated
//    }

}
