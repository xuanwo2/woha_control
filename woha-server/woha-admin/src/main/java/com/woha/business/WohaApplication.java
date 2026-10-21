package com.woha.business;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/**
 * 启动程序
 * 
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class },
        excludeName = {
                // MyBatis-Plus 自动配置
                "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration",
                "com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration",
                // MyBatis Spring Boot 自动配置（由 pagehelper / mp 传递引入）
                "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",
                "org.mybatis.spring.boot.autoconfigure.MybatisLanguageDriverAutoConfiguration"
        })
public class WohaApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(WohaApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  若依启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\ \\  |  ||   |(_,_)'         \n" +
                " |  | \\ `'   /|   `-'  /           \n" +
                " |  |  \\    /  \\      /           \n" +
                " ''-'   `'-'    `-..-'              ");
    }
}
