package it.mgt.jpa.json2jpa.test.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.jpa.json2jpa.Json2Jpa;
import it.mgt.jpa.json2jpa.Json2JpaFactory;
import it.mgt.jpa.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.jpa.json2jpa.test.config.SpringContext;
import it.mgt.jpa.json2jpa.test.property.component.PropertyHelper;
import it.mgt.jpa.json2jpa.test.property.entity.PropertyEmployee;
import it.mgt.jpa.json2jpa.test.property.entity.PropertyRole;
import org.junit.After;
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


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = {StandaloneDataConfig.class, SpringContext.class })
public class ManyToManyMappedByUpdateObj {

    private static final Logger logger = LoggerFactory.getLogger(ManyToManyMappedByUpdateObj.class);

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


    private PropertyRole role;


    @Test
    @Transactional
    public void test()  {
        logger.info("Testing mapped-by many-to-many object update");

        role = em.createNamedQuery("PropertyRole.findByName", PropertyRole.class)
                .setParameter("name", "employee")
                .getSingleResult();

        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode employeesJson = json.putArray("employees");
        for (PropertyEmployee e : role.getEmployees()) {
            ObjectNode employeeJson = employeesJson.addObject();
            employeeJson.put("id", e.getId());
            employeeJson.put("firstName", e.getFirstName() + "_suffix");
            employeeJson.put("lastName", e.getLastName() + "_suffix");
        }

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(role, json);

        em.flush();
        em.clear();

        role = em.createNamedQuery("PropertyRole.findByName", PropertyRole.class)
                .setParameter("name", "employee")
                .getSingleResult();

        for (PropertyEmployee e : role.getEmployees()) {
            Assert.assertTrue(e.getFirstName().endsWith("_suffix"));
            Assert.assertTrue(e.getLastName().endsWith("_suffix"));
        }
    }

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        for (PropertyEmployee e : role.getEmployees()) {
            e.setFirstName(e.getFirstName().substring(0, e.getFirstName().length() - 7));
            e.setLastName(e.getLastName().substring(0, e.getLastName().length() - 7));
        }

        em.flush();
    }

}
