package it.mgt.jpa.json2jpa.test.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.jpa.json2jpa.Json2Jpa;
import it.mgt.jpa.json2jpa.Json2JpaFactory;
import it.mgt.jpa.json2jpa.test.property.component.PropertyHelper;
import it.mgt.jpa.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.jpa.json2jpa.test.config.SpringContext;
import it.mgt.jpa.json2jpa.test.property.entity.PropertyEmployee;
import it.mgt.jpa.json2jpa.test.property.entity.PropertyEmploymentContract;
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
public class OneToOneDirectNew {

    private static final Logger logger = LoggerFactory.getLogger(OneToOneDirectNew.class);

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


    private PropertyEmploymentContract originalContract;
    private PropertyEmploymentContract newContract;


    @Test
    @Transactional
    public void test()  {
        logger.info("Testing direct one-to-one object creation");

        Date now = new Date();

        PropertyEmployee wile = em.createNamedQuery("PropertyEmployee.findBySsn", PropertyEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        originalContract = wile.getEmploymentContract();

        ObjectNode json = objectMapper.createObjectNode();
        ObjectNode employmentContractJson = json.putObject("employmentContract");
        employmentContractJson.put("firstName", "Willy");
        employmentContractJson.put("lastName", "Coy");
        employmentContractJson.put("ssn", "abc");
        employmentContractJson.put("employmentContractType", "CONSULTANT");
        employmentContractJson.put("signatureDate", now.getTime());
        employmentContractJson.put("enrollmentDate", now.getTime());
        employmentContractJson.put("remuneration", 0);
        employmentContractJson.put("company", originalContract.getCompany().getId());

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(wile, json);

        em.flush();
        em.clear();

        wile = em.createNamedQuery("PropertyEmployee.findBySsn", PropertyEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();
        newContract = wile.getEmploymentContract();

        Assert.assertNotEquals(originalContract.getId(), newContract.getId());
        Assert.assertEquals("abc", newContract.getSsn());
    }

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        PropertyEmployee wile = em.createNamedQuery("PropertyEmployee.findBySsn", PropertyEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        originalContract = em.merge(originalContract);
        wile.setEmploymentContract(originalContract);

        em.remove(newContract);

        em.flush();
    }

}
