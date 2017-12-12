package it.mgt.json2jpa.test.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import it.mgt.json2jpa.Json2JpaFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@Configuration
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@EnableScheduling
@EnableAsync
@ImportResource("classpath:spring-aspectj.xml")
@ComponentScans({
        @ComponentScan("it.mgt.json2jpa.test.field.component"),
        @ComponentScan("it.mgt.json2jpa.test.property.component")
})
public class SpringContext {

    @Autowired
    Environment env;
    @Autowired
    DataSource dataSource;
    @Autowired
    JpaVendorAdapter jpaVendorAdapter;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ObjectMapper objectMapper() {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        AnnotationIntrospector pair = new AnnotationIntrospectorPair(primary, secondary);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setAnnotationIntrospector(pair);

        return mapper;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("it.mgt.json2jpa.test.field.entity", "it.mgt.json2jpa.test.property.entity");
        em.setJpaVendorAdapter(jpaVendorAdapter);
        em.getJpaPropertyMap().put("hibernate.hbm2ddl.auto", "update");
        em.getJpaPropertyMap().put("hibernate.physical_naming_strategy", "it.mgt.json2jpa.test.config.ImprovedNamingStrategy");
        em.getJpaPropertyMap().put("hibernate.id.new_generator_mappings", "false");

        String dialect = env.getProperty("it.mgt.json2jpa.test.hibernate.dialect");
        if (dialect != null)
            em.getJpaPropertyMap().put("hibernate.dialect", dialect);

        String storageEngine = env.getProperty("it.mgt.json2jpa.test.dialect.storage_engine");
        if (storageEngine != null)
            em.getJpaPropertyMap().put("hibernate.dialect.storage_engine", storageEngine);

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);

        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public Json2JpaFactory json2JpaFactory() {
        return new Json2JpaFactory();
    }

}
