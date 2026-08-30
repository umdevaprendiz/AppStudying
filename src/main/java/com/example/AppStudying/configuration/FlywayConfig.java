package com.example.AppStudying.configuration;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.util.LinkedHashSet;

/**
 * Esta versão do Spring Boot não traz mais a autoconfiguração automática do
 * Flyway (spring-boot-autoconfigure não contém FlywayAutoConfiguration), então
 * conectamos manualmente: um bean "flyway" que roda as migrations ao ser
 * criado, e um BeanFactoryPostProcessor que força o "entityManagerFactory" a
 * esperar esse bean — senão o Hibernate poderia validar o schema antes das
 * migrations rodarem.
 */
@Configuration
public class FlywayConfig {

    @Bean
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(false)
                .load();
        flyway.migrate();
        return flyway;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static BeanFactoryPostProcessor flywayEntityManagerFactoryDependency() {
        return (ConfigurableListableBeanFactory beanFactory) -> {
            if (!beanFactory.containsBeanDefinition("entityManagerFactory")) {
                return;
            }
            BeanDefinition emf = beanFactory.getBeanDefinition("entityManagerFactory");
            var dependsOn = new LinkedHashSet<String>();
            if (emf.getDependsOn() != null) {
                dependsOn.addAll(java.util.List.of(emf.getDependsOn()));
            }
            dependsOn.add("flyway");
            emf.setDependsOn(dependsOn.toArray(new String[0]));
        };
    }
}
