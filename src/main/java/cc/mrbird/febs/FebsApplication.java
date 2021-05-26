package cc.mrbird.febs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author MrBird
 */
@EnableAsync
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("cc.mrbird.febs.*.mapper")
@ServletComponentScan
public class FebsApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FebsApplication.class).run(args);
    }

}
