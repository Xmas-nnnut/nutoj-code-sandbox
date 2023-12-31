package com.xqj.nutojcodesandbox.controller;

import com.xqj.nutojcodesandbox.service.CodeSandboxService;
import com.xqj.nutojcodesandbox.model.dto.ExecuteCodeRequest;
import com.xqj.nutojcodesandbox.model.dto.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController()
@RequestMapping("/codesandbox")
public class CodeSandboxController {

    // 定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "secretKey";

    @Resource
    private CodeSandboxService codeSandboxService;

    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                    HttpServletResponse response) {
        // 基本的认证
//        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
//        if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
//            response.setStatus(403);
//            return null;
//        }
        executeCodeRequest.setCode("import java.io.*;\n" +
                "import java.util.*;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) throws Exception{\n" +
                "        Scanner cin = new Scanner(System.in);\n" +
                "        int a = cin.nextInt(), b = cin.nextInt();\n" +
                "        System.out.println(\"交互式进程结果:\" + (a + b));\n" +
                "    }\n" +
                "}");
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }
        return codeSandboxService.executeCode(executeCodeRequest);
    }
}
