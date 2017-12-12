package it.mgt.json2jpa.test.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.json2jpa.Json2Jpa;
import it.mgt.json2jpa.Json2JpaFactory;
import it.mgt.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.json2jpa.test.config.SpringContext;
import it.mgt.json2jpa.test.property.component.PropertyHelper;
import it.mgt.json2jpa.test.property.entity.PropertyEmployee;
import it.mgt.json2jpa.test.property.entity.PropertyRole;
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
public class ManyToManyMappedByNew {

    private static final Logger logger = LoggerFactory.getLogger(ManyToManyMappedByNew.class);

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


    private PropertyEmployee newEmployee;


    @Test
    @Transactional
    public void test()  {
        logger.info("Testing mapped-by many-to-many object creation");

        PropertyRole role = em.createNamedQuery("PropertyRole.findByName", PropertyRole.class)
                .setParameter("name", "employee")
                .getSingleResult();

        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode employeesJson = json.putArray("employees");
        for (PropertyEmployee r : role.getEmployees())
            employeesJson.add(r.getId());

        ObjectNode employeeJson = employeesJson.addObject();
        employeeJson.put("firstName", "Test");
        employeeJson.put("lastName", "Test");
        employeeJson.put("ssn", "000");
        employeeJson.put("email", "test@acme.it");
        employeeJson.put("company", role.getCompany().getId());

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(role, json);

        em.flush();
        em.clear();

        role = em.createNamedQuery("PropertyRole.findByName", PropertyRole.class)
                .setParameter("name", "employee")
                .getSingleResult();

        newEmployee = role.getEmployees()
                .stream()
                .filter(e -> e.getSsn().equals("000"))
                .findFirst()
                .orElse(null);

        Assert.assertNotNull(newEmployee);
        Assert.assertEquals("Test", newEmployee.getFirstName());
        Assert.assertEquals("Test", newEmployee.getLastName());
        Assert.assertEquals("000", newEmployee.getSsn());
        Assert.assertEquals("test@acme.it", newEmployee.getEmail());
    }

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        em.remove(newEmployee);

        em.flush();
    }

}
