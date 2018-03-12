package pro.bigbro;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import pro.bigbro.services.Main;

import javax.sql.DataSource;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan({"pro.bigbro"})
@PropertySource("classpath:siteConfig.properties")
public class Application {

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Application.class, args);
        Main main = context.getBean(Main.class);
        main.runReportMaker();
        System.out.println("Executed!");
        System.exit(0);
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(environment.getProperty("spring.datasource.driver-class-name"));
        dataSource.setUrl(environment.getProperty("spring.datasource.url"));
        dataSource.setUsername(environment.getProperty("spring.datasource.username"));
        dataSource.setPassword(environment.getProperty("spring.datasource.password"));
        return dataSource;
    }
}
