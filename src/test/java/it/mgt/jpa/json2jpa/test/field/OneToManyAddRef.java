package it.mgt.jpa.json2jpa.test.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.jpa.json2jpa.Json2Jpa;
import it.mgt.jpa.json2jpa.Json2JpaFactory;
import it.mgt.jpa.json2jpa.test.field.component.FieldHelper;
import it.mgt.jpa.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.jpa.json2jpa.test.config.SpringContext;
import it.mgt.jpa.json2jpa.test.field.entity.FieldCompany;
import it.mgt.jpa.json2jpa.test.field.entity.FieldEmployee;
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
public class OneToManyAddRef {

    private static final Logger logger = LoggerFactory.getLogger(OneToManyAddRef.class);

    @PersistenceContext
    private EntityManager em;

	@Autowired
	private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

	@Autowired
    private FieldHelper helper;


	private FieldEmployee newEmployee;


    @Before
    @Transactional
    public void before() {
        helper.initialize();
    }

    @Test
    @Transactional
    public void test()  {
        logger.info("Testing one-to-many object addition");

        newEmployee = new FieldEmployee("Test", "Test", "000", "test@test.it");
        em.persist(newEmployee);

        em.flush();
        em.clear();

        FieldCompany company = em.createNamedQuery("FieldCompany.findByName", FieldCompany.class)
                .setParameter("name", "ACME")
                .getSingleResult();

        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode employeesJson = json.putArray("employees");

        for (FieldEmployee employee : company.getEmployees())
            employeesJson.add(employee.getId());

        employeesJson.add(newEmployee.getId());

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(company, json);

        em.flush();
        em.clear();

        company = em.createNamedQuery("FieldCompany.findByName", FieldCompany.class)
                .setParameter("name", "ACME")
                .getSingleResult();

        newEmployee = company.getEmployees()
                .stream()
                .filter(e -> e.getSsn().equals("000"))
                .findFirst()
                .orElse(null);

        Assert.assertNotNull(newEmployee);
        Assert.assertEquals("Test", newEmployee.getFirstName());
        Assert.assertEquals("Test", newEmployee.getLastName());
        Assert.assertEquals("test@test.it", newEmployee.getEmail());
    }

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        em.flush();
        em.clear();

        newEmployee = em.merge(newEmployee);

        em.remove(newEmployee);

        em.flush();
    }

}