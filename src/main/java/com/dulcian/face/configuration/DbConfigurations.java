package com.dulcian.face.configuration;

import com.dulcian.face.utils.EncryptionUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.Properties;

@Configuration
public class DbConfigurations {

    @Bean
    DataSource dataSource(Environment environment){
        Properties properties = loadDbProperties(environment);
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setJdbcUrl("jdbc:sqlserver://"+properties.getProperty("url")+";DatabaseName=iTap");
        dataSource.setUsername(properties.getProperty("username"));
        dataSource.setPassword(properties.getProperty("password"));

        // Connection pool settings
        int cpuCores = Runtime.getRuntime().availableProcessors();
        dataSource.setMaximumPoolSize(4 * cpuCores);           // Set max pool size to number of CPU cores
        dataSource.setMinimumIdle(cpuCores); // Minimum idle connections
        return dataSource;
    }

    private static Properties loadDbProperties(Environment environment) {
        File dbPropertiesFile = new File(System.getProperty("catalina.base"), "webapps/db.txt");
        try {
            if(!dbPropertiesFile.exists()){
                Properties properties = new Properties();
                properties.setProperty("url", environment.getProperty("database.url"));
                properties.setProperty("username", environment.getProperty("database.username"));
                properties.setProperty("password", environment.getProperty("database.password"));
                return properties;
            }
            return loadFile(dbPropertiesFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static Properties loadFile(File file) throws Exception {
        Properties props = new Properties();
        String content = new String(Files.readAllBytes(file.toPath())).trim();
        try {
            String decrypted = EncryptionUtils.decrypt(content);
            props.load(new StringReader(decrypted));
            content = decrypted;
        } catch (Exception e) {
            // Failed to decrypt treat as plain text
        }

        // Plaintext: load and encrypt
        props.load(new StringReader(content));

        //validate the properties
        if(!(props.containsKey("url") && props.containsKey("username") && props.containsKey("password"))){
            throw new RuntimeException("Invalid db properties file");
        }

        String encrypted = EncryptionUtils.encrypt(content);
        // Overwrite file with encrypted content
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(encrypted);
        }

        return props;
    }

}
