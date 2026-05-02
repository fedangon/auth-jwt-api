package com.fedangon.authjwtapi.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        // Executa migrations do Flyway no startup (antes do Hibernate validar)
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
    }

    @Bean
    public static BeanFactoryPostProcessor entityManagerFactoryDependsOnFlyway() {
        // Garante que o Flyway rode antes do EntityManagerFactory (evita falha no ddl-auto=validate)
        return new EntityManagerFactoryDependsOnFlywayPostProcessor();
    }

    private static final class EntityManagerFactoryDependsOnFlywayPostProcessor implements BeanFactoryPostProcessor {
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            if (!beanFactory.containsBeanDefinition("entityManagerFactory")) {
                return;
            }
            if (!beanFactory.containsBeanDefinition("flyway")) {
                return;
            }

            BeanDefinition emf = beanFactory.getBeanDefinition("entityManagerFactory");
            String[] existing = emf.getDependsOn();

            Set<String> dependsOn = new LinkedHashSet<>();
            if (existing != null) {
                for (String dep : existing) {
                    if (dep != null && !dep.isBlank()) {
                        dependsOn.add(dep);
                    }
                }
            }
            dependsOn.add("flyway");

            emf.setDependsOn(dependsOn.toArray(new String[0]));
        }
    }
}

