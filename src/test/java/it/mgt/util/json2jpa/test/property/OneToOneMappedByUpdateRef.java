package it.mgt.util.json2jpa.test.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.util.json2jpa.Json2Jpa;
import it.mgt.util.json2jpa.Json2JpaFactory;
import it.mgt.util.json2jpa.test.property.component.PropertyHelper;
import it.mgt.util.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.util.json2jpa.test.config.SpringContext;
import it.mgt.util.json2jpa.test.property.entity.PropertyEmployee;
import it.mgt.util.json2jpa.test.property.entity.PropertyEmploymentContract;
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
public class OneToOneMappedByUpdateRef {

    private static final Logger logger = LoggerFactory.getLogger(OneToOneMappedByUpdateRef.class);

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
        logger.info("Testing mapped-by one-to-one reference update");

        PropertyEmployee wile = em.createNamedQuery("PropertyEmployee.findBySsn", PropertyEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        PropertyEmployee beep = em.createNamedQuery("PropertyEmployee.findBySsn", PropertyEmployee.class)
                .setParameter("ssn", "789")
                .getSingleResult();

        PropertyEmploymentContract contractWile = wile.getEmploymentContract();
        wile.setEmploymentContract(beep.getEmploymentContract());
        beep.setEmploymentContract(null);

        em.flush();

        ObjectNode json = objectMapper.createObjectNode();
        json.put("employee", beep.getId());

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(contractWile, json);

        em.flush();
        em.clear();

        contractWile = em.find(PropertyEmploymentContract.class, contractWile.getId());
        Assert.assertEquals(beep.getId(), contractWile.getEmployee().getId());
    }

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        PropertyEmployee wile = em.createNamedQuery("PropertyEmployee.findBySsn", PropertyEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        PropertyEmployee beep = em.createNamedQuery("PropertyEmployee.findBySsn", PropertyEmployee.class)
                .setParameter("ssn", "789")
                .getSingleResult();

        PropertyEmploymentContract contractWile = wile.getEmploymentContract();
        PropertyEmploymentContract contractBeep = beep.getEmploymentContract();

        wile.setEmploymentContract(contractBeep);
        beep.setEmploymentContract(contractWile);

        em.flush();
    }

}
