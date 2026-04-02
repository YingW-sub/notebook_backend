package redlib.backend;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class WebBackendApplicationTests {

    @Test
    void testAnalyze() {
        String realPassword = redlib.backend.utils.FormatUtils.password("123456");
        System.out.println("====== 下面就是123456真正的加密结果 ======");
        System.out.println(realPassword);
    }
}
