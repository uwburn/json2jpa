package it.mgt.json2jpa.test.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.json2jpa.Json2Jpa;
import it.mgt.json2jpa.Json2JpaFactory;
import it.mgt.json2jpa.test.config.SpringContext;
import it.mgt.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.json2jpa.test.field.component.FieldHelper;
import it.mgt.json2jpa.test.field.entity.FieldCompany;
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
public class Construct {

    private static final Logger logger = LoggerFactory.getLogger(Construct.class);

    @PersistenceContext
    private EntityManager em;

	@Autowired
	private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

	@Autowired
    private FieldHelper helper;


	private FieldCompany newCompany;


    @Before
    @Transactional
    public void before() {
        helper.initialize();
    }

	@Test
    @Transactional
	public void test()  {
        logger.info("Testing construct");

        ObjectNode json = objectMapper.createObjectNode();
        json.put("name", "Test");
        json.put("address", "Nowhere");
        json.put("motto", "Yeah!");
        json.put("anthem", "IGNORED");

        Json2Jpa json2Jpa = json2JpaFactory.build();
        newCompany = json2Jpa.construct(FieldCompany.class, json);

        em.flush();
        em.clear();

        newCompany = em.createNamedQuery("FieldCompany.findByName", FieldCompany.class)
                .setParameter("name", "Test")
                .getSingleResult();

        Assert.assertNotNull(newCompany);
        Assert.assertEquals("Test", newCompany.getName());
        Assert.assertEquals("Nowhere", newCompany.getAddress());
        Assert.assertNull(newCompany.getMotto());
        Assert.assertNull(newCompany.getAnthem());
	}

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        em.remove(newCompany);

        em.flush();
    }

}
