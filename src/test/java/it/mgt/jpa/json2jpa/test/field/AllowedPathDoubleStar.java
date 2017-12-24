package it.mgt.jpa.json2jpa.test.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.jpa.json2jpa.Json2Jpa;
import it.mgt.jpa.json2jpa.Json2JpaFactory;
import it.mgt.jpa.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.jpa.json2jpa.test.config.SpringContext;
import it.mgt.jpa.json2jpa.test.field.component.FieldHelper;
import it.mgt.jpa.json2jpa.test.field.entity.FieldEmployee;
import it.mgt.jpa.json2jpa.test.field.entity.FieldRole;
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
public class AllowedPathDoubleStar {

    private static final Logger logger = LoggerFactory.getLogger(JsonIgnore.class);

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
        logger.info("Testing allowed path double star");

        FieldEmployee wile = em.createNamedQuery("FieldEmployee.findBySsn", FieldEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        ObjectNode json = objectMapper.createObjectNode();
        json.put("firstName", "DISALLOWED");
        ObjectNode companyJson = json.putObject("company");
        companyJson.put("id", wile.getCompany().getId());
        companyJson.put("name", "ACME!");
        ArrayNode rolesJson = companyJson.putArray("roles");
        for (FieldRole r : wile.getCompany().getRoles()) {
            ObjectNode roleJson = rolesJson.addObject();
            roleJson.put("id", r.getId());
            roleJson.put("name", r.getName() + "_suffix");
        }

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.addAllowedPaths("company/**");
        json2Jpa.merge(wile, json);

        em.flush();
        em.clear();

        wile = em.createNamedQuery("FieldEmployee.findBySsn", FieldEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        Assert.assertEquals("Wile", wile.getFirstName());
        Assert.assertEquals("ACME!", wile.getCompany().getName());
        for (FieldRole r : wile.getCompany().getRoles())
            Assert.assertTrue(r.getName().endsWith("_suffix"));
    }

    @After
    @Transactional
    public void after() {
        logger.info("Restoring data");

        FieldEmployee wile = em.createNamedQuery("FieldEmployee.findBySsn", FieldEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        wile.getCompany().setName("ACME");

        for (FieldRole r : wile.getCompany().getRoles())
            r.setName(r.getName().substring(0, r.getName().length() - 7));

        em.flush();
    }

}
