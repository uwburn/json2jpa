package it.mgt.util.json2jpa.test.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.util.json2jpa.Json2Jpa;
import it.mgt.util.json2jpa.Json2JpaFactory;
import it.mgt.util.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.util.json2jpa.test.config.SpringContext;
import it.mgt.util.json2jpa.test.property.component.PropertyHelper;
import it.mgt.util.json2jpa.test.property.entity.PropertyEmployee;
import it.mgt.util.json2jpa.test.property.entity.PropertyRole;
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

import static it.mgt.util.json2jpa.test.property.entity.PropertyOperation.*;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = {StandaloneDataConfig.class, SpringContext.class })
public class ManyToManyDirectUpdateRef {

    private static final Logger logger = LoggerFactory.getLogger(ManyToManyDirectUpdateRef.class);

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


    private PropertyEmployee wile;
    private PropertyRole newRole;


    @Test
    @Transactional
    public void test()  {
        logger.info("Testing direct many-to-many reference update");

        wile = em.createNamedQuery("PropertyEmployee.findBySsn", PropertyEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        int initialRolesCount = wile.getRoles().size();

        newRole = new PropertyRole("test", wile.getCompany(), CREATE_DOCUMENTS, READ_OTHERS_DOCUMENTS,
                EDIT_OTHER_DOCUMENTS, EDIT_PROFILE, READ_OTHER_PROFILE, EDIT_OTHER_PROFILE);

        em.persist(newRole);

        em.flush();
        em.clear();

        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode rolesJson = json.putArray("roles");
        for (PropertyRole r : wile.getRoles())
            rolesJson.add(r.getId());

        rolesJson.add(newRole.getId());

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(wile, json);

        em.flush();
        em.clear();

        wile = em.createNamedQuery("PropertyEmployee.findBySsn", PropertyEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        newRole = wile.getRoles()
                .stream()
                .filter(r -> r.getName().equals("test"))
                .findFirst()
                .orElse(null);

        Assert.assertNotNull(newRole);
        Assert.assertEquals(initialRolesCount + 1, wile.getRoles().size());
    }

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        wile.getRoles().remove(newRole);
        em.remove(newRole);

        em.flush();
    }

}
