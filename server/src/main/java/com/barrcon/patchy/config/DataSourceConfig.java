package com.barrcon.patchy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.sql.DataSource;
// this sets up the datasource for the application to connect to the MySQL database

@Configuration
public class DataSourceConfig {
    @Bean
    public DataSource datasource(@Value("${spring.datasource.password}") String password,
                                 @Value("${spring.datasource.username}") String username) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/patchy");
        dataSource.setPassword(password);
        dataSource.setUsername(username);

        return dataSource;
    }
}
