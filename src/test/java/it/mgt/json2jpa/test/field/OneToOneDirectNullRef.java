package it.mgt.json2jpa.test.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.json2jpa.Json2Jpa;
import it.mgt.json2jpa.Json2JpaFactory;
import it.mgt.json2jpa.test.field.component.FieldHelper;
import it.mgt.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.json2jpa.test.config.SpringContext;
import it.mgt.json2jpa.test.field.entity.FieldEmployee;
import it.mgt.json2jpa.test.field.entity.FieldEmploymentContract;
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
public class OneToOneDirectNullRef {

    private static final Logger logger = LoggerFactory.getLogger(OneToOneDirectNullRef.class);

    @PersistenceContext
    private EntityManager em;

	@Autowired
	private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

	@Autowired
    private FieldHelper helper;


    private FieldEmploymentContract contractWile;


    @Before
    @Transactional
    public void before() {
        helper.initialize();
    }

    @Test
    @Transactional
    public void test()  {
        logger.info("Testing direct one-to-one update null reference");

        FieldEmployee wile = em.createNamedQuery("FieldEmployee.findBySsn", FieldEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        contractWile = wile.getEmploymentContract();

        ObjectNode json = objectMapper.createObjectNode();
        json.putNull("employmentContract");

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(wile, json);

        em.flush();
        em.clear();

        wile = em.createNamedQuery("FieldEmployee.findBySsn", FieldEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();
        Assert.assertNull(wile.getEmploymentContract());
    }

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        FieldEmployee wile = em.createNamedQuery("FieldEmployee.findBySsn", FieldEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        wile.setEmploymentContract(contractWile);

        em.flush();
    }

}
