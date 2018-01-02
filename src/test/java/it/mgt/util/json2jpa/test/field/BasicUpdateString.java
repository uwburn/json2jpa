package it.mgt.util.json2jpa.test.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.util.json2jpa.Json2Jpa;
import it.mgt.util.json2jpa.Json2JpaFactory;
import it.mgt.util.json2jpa.test.field.component.FieldHelper;
import it.mgt.util.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.util.json2jpa.test.config.SpringContext;
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
public class BasicUpdateString {

    private static final Logger logger = LoggerFactory.getLogger(BasicUpdateString.class);

    @PersistenceContext
    private EntityManager em;

	@Autowired
	private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

	@Autowired
    private FieldHelper helper;

    @Before
    @Transactional
    public void before() {
        helper.initialize();
    }

	@Test
    @Transactional
	public void test()  {
        logger.info("Testing basic string property update");

        FieldEmployee beep = em.createNamedQuery("FieldEmployee.findBySsn", FieldEmployee.class)
                .setParameter("ssn", "789")
                .getSingleResult();
        Assert.assertEquals("Beep", beep.getFirstName());

        ObjectNode json = objectMapper.createObjectNode();
        json.put("firstName", "Bip");

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(beep, json);

        em.flush();
        em.clear();

        beep = em.createNamedQuery("FieldEmployee.findBySsn", FieldEmployee.class)
                .setParameter("ssn", "789")
                .getSingleResult();
        Assert.assertEquals("Bip", beep.getFirstName());
	}

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        FieldEmployee beep = em.createNamedQuery("FieldEmployee.findBySsn", FieldEmployee.class)
                .setParameter("ssn", "789")
                .getSingleResult();

        beep.setFirstName("Beep");

        em.flush();
    }

}
