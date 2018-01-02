package it.mgt.util.json2jpa.test.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.util.json2jpa.Json2Jpa;
import it.mgt.util.json2jpa.Json2JpaFactory;
import it.mgt.util.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.util.json2jpa.test.config.SpringContext;
import it.mgt.util.json2jpa.test.field.component.FieldHelper;
import it.mgt.util.json2jpa.test.field.entity.FieldEmployee;
import it.mgt.util.json2jpa.test.field.entity.FieldRole;
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
public class ManyToManyDirectUpdateObj {

    private static final Logger logger = LoggerFactory.getLogger(ManyToManyDirectUpdateObj.class);

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


    private FieldEmployee wile;


    @Test
    @Transactional
    public void test()  {
        logger.info("Testing direct many-to-one object update");

        wile = em.createNamedQuery("FieldEmployee.findBySsn", FieldEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode rolesJson = json.putArray("roles");
        for (FieldRole r : wile.getRoles()) {
            ObjectNode roleJson = rolesJson.addObject();
            roleJson.put("id", r.getId());
            roleJson.put("name", r.getName() + "_suffix");
        }

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(wile, json);

        em.flush();
        em.clear();

        wile = em.createNamedQuery("FieldEmployee.findBySsn", FieldEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        for (FieldRole r : wile.getRoles())
            Assert.assertTrue(r.getName().endsWith("_suffix"));
    }

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        for (FieldRole r : wile.getRoles())
            r.setName(r.getName().substring(0, r.getName().length() - 7));

        em.flush();
    }

}
