package it.mgt.json2jpa.test.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.json2jpa.Json2Jpa;
import it.mgt.json2jpa.Json2JpaFactory;
import it.mgt.json2jpa.test.property.component.PropertyHelper;
import it.mgt.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.json2jpa.test.config.SpringContext;
import it.mgt.json2jpa.test.property.entity.PropertyEmployee;
import it.mgt.json2jpa.test.property.entity.PropertyEmploymentContractType;
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
public class BasicUpdateEnum {

    private static final Logger logger = LoggerFactory.getLogger(BasicUpdateEnum.class);

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
        logger.info("Testing basic enum property update");

        PropertyEmployee wile = em.createNamedQuery("PropertyEmployee.findBySsn", PropertyEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();
        Assert.assertEquals(PropertyEmploymentContractType.CONSULTANT, wile.getEmploymentContract().getEmploymentContractType());

        ObjectNode json = objectMapper.createObjectNode();
        json.put("employmentContractType", "PART_TIME");

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(wile.getEmploymentContract(), json);

        em.flush();
        em.clear();

        wile = em.createNamedQuery("PropertyEmployee.findBySsn", PropertyEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();
        Assert.assertEquals(PropertyEmploymentContractType.PART_TIME, wile.getEmploymentContract().getEmploymentContractType());
    }

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        PropertyEmployee wile = em.createNamedQuery("PropertyEmployee.findBySsn", PropertyEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        wile.getEmploymentContract().setEmploymentContractType(PropertyEmploymentContractType.CONSULTANT);

        em.flush();
    }

}
