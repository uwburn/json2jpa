package it.mgt.json2jpa.test.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.json2jpa.Json2Jpa;
import it.mgt.json2jpa.Json2JpaFactory;
import it.mgt.json2jpa.test.field.component.FieldHelper;
import it.mgt.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.json2jpa.test.config.SpringContext;
import it.mgt.json2jpa.test.field.entity.FieldCompany;
import it.mgt.json2jpa.test.field.entity.FieldEmploymentContract;
import it.mgt.json2jpa.test.field.entity.FieldEmploymentContractType;
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
import java.util.Date;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = {StandaloneDataConfig.class, SpringContext.class })
public class OneToOneMappedByNew {

    private static final Logger logger = LoggerFactory.getLogger(OneToOneMappedByNew.class);

    @PersistenceContext
    private EntityManager em;

	@Autowired
	private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

	@Autowired
    private FieldHelper helper;


    private FieldEmploymentContract contract;


    @Before
    @Transactional
    public void before() {
        helper.initialize();
    }

    @Test
    @Transactional
    public void test()  {
        logger.info("Testing mapped-by one-to-one object creation");

        FieldCompany company = em.createNamedQuery("FieldCompany.findByName", FieldCompany.class)
                .setParameter("name", "ACME")
                .getSingleResult();

        Date now = new Date();
        contract = new FieldEmploymentContract("Test", "Test", "000", FieldEmploymentContractType.CONSULTANT, now, now, 0, company);
        em.persist(contract);

        em.flush();
        em.clear();

        ObjectNode json = objectMapper.createObjectNode();
        ObjectNode employeeJson = json.putObject("employee");
        employeeJson.put("firstName", "Test");
        employeeJson.put("lastName", "Test");
        employeeJson.put("ssn", "000");
        employeeJson.put("email", "test@acme.it");
        employeeJson.put("company", company.getId());

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(contract, json);

        em.flush();
        em.clear();

        contract = em.find(FieldEmploymentContract.class, contract.getId());
        Assert.assertNotNull(contract.getEmployee());
        Assert.assertEquals("Test", contract.getEmployee().getFirstName());
        Assert.assertEquals("Test", contract.getEmployee().getLastName());
        Assert.assertEquals("000", contract.getEmployee().getSsn());
        Assert.assertEquals("test@acme.it", contract.getEmployee().getEmail());
    }

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        em.flush();
        em.clear();

        contract = em.merge(contract);

        em.remove(contract.getEmployee());
        em.remove(contract);

        em.flush();
        em.clear();
    }

}
