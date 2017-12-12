package it.mgt.json2jpa.test.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.json2jpa.Json2Jpa;
import it.mgt.json2jpa.Json2JpaFactory;
import it.mgt.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.json2jpa.test.config.SpringContext;
import it.mgt.json2jpa.test.property.component.PropertyHelper;
import it.mgt.json2jpa.test.property.entity.PropertyCompany;
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
public class JsonIgnore {

    private static final Logger logger = LoggerFactory.getLogger(JsonIgnore.class);

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
        logger.info("Testing json ignored property");

        PropertyCompany company = em.createNamedQuery("PropertyCompany.findByName", PropertyCompany.class)
                .setParameter("name", "ACME")
                .getSingleResult();
        Assert.assertEquals("Hooray", company.getMotto());
        Assert.assertEquals("Na-na-na", company.getAnthem());

        ObjectNode json = objectMapper.createObjectNode();
        json.put("motto", "IGNORE");
        json.put("anthem", "IGNORE");

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(company, json);

        em.flush();
        em.clear();

        company = em.createNamedQuery("PropertyCompany.findByName", PropertyCompany.class)
                .setParameter("name", "ACME")
                .getSingleResult();

        Assert.assertEquals("Hooray", company.getMotto());
        Assert.assertEquals("Na-na-na", company.getAnthem());
	}

}
