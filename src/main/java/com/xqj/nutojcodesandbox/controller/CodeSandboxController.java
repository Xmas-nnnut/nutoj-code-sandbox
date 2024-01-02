package com.xqj.nutojcodesandbox.controller;

import com.xqj.nutojcodesandbox.core.CodeSandboxFactory;
import com.xqj.nutojcodesandbox.core.CodeSandboxTemplate;
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
    @PostMapping("/executeCodeOld")
    ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request,
                                    HttpServletResponse response) {
        // 基本的认证
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
            response.setStatus(403);
            return null;
        }
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }
//        if (language.equals("cpp")) {
//            executeCodeRequest.setCode("#include <iostream>\n" +
//                    "\n" +
//                    "int main() {\n" +
//                    "    int a, b;\n" +
//                    "    std::cin >> a >> b;\n" +
//                    "\n" +
//                    "    int sum = a + b;\n" +
//                    "\n" +
//                    "    std::cout << \"您输入的两个数的和是: \" << sum << std::endl;\n" +
//                    "\n" +
//                    "    return 0;\n" +
//                    "}");
//        } else
//            executeCodeRequest.setCode("import java.io.*;\n" +
//                    "import java.util.*;\n" +
//                    "\n" +
//                    "public class Main {\n" +
//                    "    public static void main(String[] args) throws Exception{\n" +
//                    "        Scanner cin = new Scanner(System.in);\n" +
//                    "        int a = cin.nextInt(), b = cin.nextInt();\n" +
//                    "        System.out.println(\"交互式进程结果:\" + (a + b));\n" +
//                    "    }\n" +
//                    "}");
        return codeSandboxService.executeCode(executeCodeRequest);
    }

    @PostMapping("/executeCode")
    public ExecuteCodeResponse executeCodeByAcm(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request, HttpServletResponse response) {
        // 基本的认证
//        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
//        if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
//            response.setStatus(403);
//            return null;
//        }
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }
        String language = executeCodeRequest.getLanguage();
        if (!language.equals("cpp") && !language.equals("java") && !language.equals("c")){
            return ExecuteCodeResponse.builder().message("暂不支持").status(0).build();
        }

        CodeSandboxTemplate sandboxTemplate = CodeSandboxFactory.getInstance(language);
        return sandboxTemplate.executeCode(executeCodeRequest);
    }
}
