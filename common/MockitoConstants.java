package com.cq.common;

import java.util.HashSet;
import java.util.Set;

/**
 * @author 有尘
 * @date 2021/9/28
 */
public class MockitoConstants {
    public static String COMMON_IMPORT = "import lombok.extern.slf4j.Slf4j;\n"
        + "import com.alibaba.fastjson.TypeReference;\n"
        + "import com.alibaba.fastjson.JSONObject;\n"
        + "import org.junit.jupiter.api.BeforeEach;\n"
        + "import org.mockito.junit.jupiter.MockitoSettings;\n"
        + "import org.mockito.quality.Strictness;\n"
        + "import org.junit.jupiter.api.DisplayName;\n"
        + "import org.junit.jupiter.api.extension.ExtendWith;\n"
        + "import org.junit.jupiter.params.ParameterizedTest;\n"
        + "import org.mockito.InjectMocks;\n"
        + "import org.mockito.Matchers;\n"
        + "import org.mockito.Mock;\n"
        + "import org.mockito.MockitoAnnotations;\n"
        + "import org.mockito.junit.jupiter.MockitoExtension;\n"
        + "\n"
        + "import java.util.*;\n\n"
        + "import static org.junit.jupiter.api.Assertions.assertEquals;\n"
        + "import static org.mockito.ArgumentMatchers.any;\n"
        + "import static org.mockito.Mockito.doReturn;\n"
        + "import static org.mockito.Mockito.when;\n\n";

    public static String COMMON_ANNOTATION = "@ExtendWith(MockitoExtension.class)\n"
        + "@MockitoSettings(strictness = Strictness.LENIENT)\n";

    public static String BEFORE_SETUP = "\t@BeforeEach\n"
        + "\tpublic void setUp() throws Exception {\n"
        + "\t\tMockitoAnnotations.initMocks(this);\n"
        + "\t}\n";
    public static Set<String> BASE_TYPE_LIST = new HashSet<String>() {{
        add("int");
        add("char");
        add("double");
        add("lang");
        add("byte");
        add("short");
        add("float");
        add("boolean");
    }};
}
