package it.mgt.util.json2jpa.test.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.util.json2jpa.Json2Jpa;
import it.mgt.util.json2jpa.Json2JpaFactory;
import it.mgt.util.json2jpa.test.property.component.PropertyHelper;
import it.mgt.util.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.util.json2jpa.test.config.SpringContext;
import it.mgt.util.json2jpa.test.property.entity.PropertyCompany;
import it.mgt.util.json2jpa.test.property.entity.PropertyEmployee;
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
public class OneToManyUpdateObj {

    private static final Logger logger = LoggerFactory.getLogger(OneToManyUpdateObj.class);

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
        logger.info("Testing one-to-many object update");

        PropertyCompany company = em.createNamedQuery("PropertyCompany.findByName", PropertyCompany.class)
                .setParameter("name", "ACME")
                .getSingleResult();

        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode employeesJson = json.putArray("employees");

        for (PropertyEmployee employee : company.getEmployees()) {
            ObjectNode employeeJson = employeesJson.addObject();
            employeeJson.put("id", employee.getId());

            String firstName = null;
            switch (employee.getSsn()) {
                case "123":
                    firstName = "Willy";
                    break;
                case "456":
                    firstName = "Buggy";
                    break;
                case "789":
                    firstName = "Bip";
                    break;
            }

            employeeJson.put("firstName", firstName);
        }

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(company, json);

        em.flush();
        em.clear();

        company = em.createNamedQuery("PropertyCompany.findByName", PropertyCompany.class)
                .setParameter("name", "ACME")
                .getSingleResult();

        for (PropertyEmployee employee : company.getEmployees()) {
            switch (employee.getSsn()) {
                case "123":
                    Assert.assertEquals("Willy", employee.getFirstName());
                    break;
                case "456":
                    Assert.assertEquals("Buggy", employee.getFirstName());
                    break;
                case "789":
                    Assert.assertEquals("Bip", employee.getFirstName());
                    break;
            }
        }
    }

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        PropertyCompany company = em.createNamedQuery("PropertyCompany.findByName", PropertyCompany.class)
                .setParameter("name", "ACME")
                .getSingleResult();

        for (PropertyEmployee employee : company.getEmployees()) {
            switch (employee.getSsn()) {
                case "123":
                    employee.setFirstName("Wile");
                    break;
                case "456":
                    employee.setFirstName("Bugs");
                    break;
                case "789":
                    employee.setFirstName("Beep");
                    break;
            }
        }

        em.flush();
    }

}
