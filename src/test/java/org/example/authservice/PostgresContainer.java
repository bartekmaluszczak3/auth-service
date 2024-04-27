package org.example.authservice;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ResourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PostgresContainer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final PostgreSQLContainer container;
    private static final HikariDataSource dataSource;
    private static final DockerImageName dockerImageName = DockerImageName.
            parse("postgres:latest")
            .asCompatibleSubstituteFor("postgres");

    static {
        container = new PostgreSQLContainer(dockerImageName)
                .withDatabaseName("jwt-service")
                .withUsername("postgres")
                .withUsername("admin");
        container.start();
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(container.getJdbcUrl());
        dataSource.setUsername(container.getUsername());
        dataSource.setPassword(container.getPassword());
    }

    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.datasource.url=" + container.getJdbcUrl(),
                "spring.datasource.username=" + container.getUsername(),
                "spring.datasource.password=" + container.getPassword(),
                "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
                "spring.jpa.hibernate.ddl-auto=none"
        ).applyTo(applicationContext.getEnvironment());
    }

    private void executeResourceSQL(String path) throws IOException {
        try(var fileReader = new FileReader(ResourceUtils.getFile(path))){
            try(var bufferReader = new BufferedReader(fileReader)){
                executeRawSql(String.join("\n", bufferReader.lines().collect(Collectors.toList())));
            }
        }
    }

    public void initDatabase() throws IOException {
        executeResourceSQL("classpath:init.sql");
    }

    public void clearRecords() throws IOException {
        executeResourceSQL("classpath:clear-records.sql");
    }

    private void executeRawSql(String sql){
        new JdbcTemplate(dataSource).execute(sql);
    }

    public List<Map<String, Object>> executeQueryForObjects(String sql){
        return new JdbcTemplate(dataSource).queryForList(sql);
    }

    public void clearDatabase() throws IOException {
        executeResourceSQL("classpath:clear.sql");

    }
}
