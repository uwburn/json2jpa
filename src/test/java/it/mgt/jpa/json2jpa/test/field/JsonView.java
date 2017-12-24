package it.mgt.jpa.json2jpa.test.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.jpa.json2jpa.Json2Jpa;
import it.mgt.jpa.json2jpa.Json2JpaFactory;
import it.mgt.jpa.json2jpa.test.config.SpringContext;
import it.mgt.jpa.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.jpa.json2jpa.test.field.component.FieldHelper;
import it.mgt.jpa.json2jpa.test.field.entity.FieldEmployee;
import it.mgt.jpa.json2jpa.test.view.NoSsn;
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
import java.util.List;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = {StandaloneDataConfig.class, SpringContext.class })
public class JsonView {

    private static final Logger logger = LoggerFactory.getLogger(JsonView.class);

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
        logger.info("Testing json view");

        FieldEmployee wile = em.createNamedQuery("FieldEmployee.findBySsn", FieldEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();
        Assert.assertEquals("123", wile.getSsn());

        ObjectNode json = objectMapper.createObjectNode();
        json.put("firstName", "Willy");
        json.put("ssn", "JSON-VIEW");

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.mergeWithView(wile, json, NoSsn.class);

        em.flush();
        em.clear();

        List<FieldEmployee> employees = em.createNamedQuery("FieldEmployee.findBySsn", FieldEmployee.class)
                .setParameter("ssn", "123")
                .getResultList();

        Assert.assertEquals(1, employees.size());
	}

    @After
    @Transactional
    public void after() {
        logger.info("Restoring data");

        FieldEmployee wile = em.createNamedQuery("FieldEmployee.findBySsn", FieldEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        wile.setFirstName("Wile");

        em.flush();
    }

}
