package it.mgt.util.json2jpa.test.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import it.mgt.util.json2jpa.Json2Jpa;
import it.mgt.util.json2jpa.Json2JpaFactory;
import it.mgt.util.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.util.json2jpa.test.config.SpringContext;
import it.mgt.util.json2jpa.test.property.component.PropertyHelper;
import it.mgt.util.json2jpa.test.property.entity.PropertyEmployee;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = {StandaloneDataConfig.class, SpringContext.class })
public class CollectionRemove {

    private static final Logger logger = LoggerFactory.getLogger(CollectionRemove.class);

    @PersistenceContext
    private EntityManager em;

	@Autowired
	private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

	@Autowired
    private PropertyHelper helper;

    @Before
    @Transactional
    public void before() {
        helper.initialize();
    }

	@Test
    @Transactional
	public void test()  {
        logger.info("Testing collection remove");

        List<PropertyEmployee> employees = em.createNamedQuery("PropertyEmployee.findAll", PropertyEmployee.class)
                .getResultList();

        int initialEmployeesCount = employees.size();

        ArrayNode json = objectMapper.createArrayNode();
        for (PropertyEmployee e : employees)
            json.add(e.getId());

        PropertyEmployee employee = new PropertyEmployee("Test", "Test", "000", "test@test.it");
        em.persist(employee);

        em.flush();
        em.clear();

        employees = em.createNamedQuery("PropertyEmployee.findAll", PropertyEmployee.class)
                .getResultList();

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(employees, PropertyEmployee.class, json);

        em.flush();
        em.clear();

        employees = em.createNamedQuery("PropertyEmployee.findAll", PropertyEmployee.class)
                .getResultList();

        Assert.assertEquals(initialEmployeesCount, employees.size());

        employees = em.createNamedQuery("PropertyEmployee.findBySsn", PropertyEmployee.class)
                .setParameter("ssn", "000")
                .getResultList();

        Assert.assertEquals(0, employees.size());
	}

}
