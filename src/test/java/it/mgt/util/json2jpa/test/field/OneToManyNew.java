package it.mgt.util.json2jpa.test.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.util.json2jpa.Json2Jpa;
import it.mgt.util.json2jpa.Json2JpaFactory;
import it.mgt.util.json2jpa.test.field.component.FieldHelper;
import it.mgt.util.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.util.json2jpa.test.config.SpringContext;
import it.mgt.util.json2jpa.test.field.entity.FieldCompany;
import it.mgt.util.json2jpa.test.field.entity.FieldEmployee;
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
public class OneToManyNew {

    private static final Logger logger = LoggerFactory.getLogger(OneToManyNew.class);

    @PersistenceContext
    private EntityManager em;

	@Autowired
	private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

	@Autowired
    private FieldHelper helper;


	private FieldEmployee employee;


    @Before
    @Transactional
    public void before() {
        helper.initialize();
    }

    @Test
    @Transactional
    public void test()  {
        logger.info("Testing one-to-many object creation");

        FieldCompany company = em.createNamedQuery("FieldCompany.findByName", FieldCompany.class)
                .setParameter("name", "ACME")
                .getSingleResult();

        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode employeesJson = json.putArray("employees");

        for (FieldEmployee employee : company.getEmployees()) {
            employeesJson.add(employee.getId());
        }

        ObjectNode employeeJson = employeesJson.addObject();
        employeeJson.put("firstName", "Test");
        employeeJson.put("lastName", "Test");
        employeeJson.put("ssn", "000");
        employeeJson.put("email", "test@acme.it");

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(company, json);

        em.flush();
        em.clear();

        company = em.createNamedQuery("FieldCompany.findByName", FieldCompany.class)
                .setParameter("name", "ACME")
                .getSingleResult();

        employee = company.getEmployees()
                .stream()
                .filter(e -> e.getSsn().equals("000"))
                .findFirst()
                .orElse(null);

        Assert.assertNotNull(employee);
        Assert.assertEquals("Test", employee.getFirstName());
        Assert.assertEquals("Test", employee.getLastName());
        Assert.assertEquals("test@acme.it", employee.getEmail());
    }

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        em.flush();
        em.clear();

        employee = em.merge(employee);

        em.remove(employee);

        em.flush();
    }

}
