package it.mgt.jpa.json2jpa.test.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.jpa.json2jpa.Json2Jpa;
import it.mgt.jpa.json2jpa.Json2JpaFactory;
import it.mgt.jpa.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.jpa.json2jpa.test.config.SpringContext;
import it.mgt.jpa.json2jpa.test.property.component.PropertyHelper;
import it.mgt.jpa.json2jpa.test.property.entity.PropertyEmployee;
import it.mgt.jpa.json2jpa.test.property.entity.PropertyRole;
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

import static it.mgt.jpa.json2jpa.test.property.entity.PropertyOperation.*;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = {StandaloneDataConfig.class, SpringContext.class })
public class ManyToManyDirectNullRef {

    private static final Logger logger = LoggerFactory.getLogger(ManyToManyDirectNullRef.class);

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


    private PropertyRole newRole;


    @Test
    @Transactional
    public void test()  {
        logger.info("Testing direct many-to-many null reference update");

        PropertyEmployee wile = em.createNamedQuery("PropertyEmployee.findBySsn", PropertyEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

        int initialRolesCount = wile.getRoles().size();

        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode rolesJson = json.putArray("roles");
        for (PropertyRole r : wile.getRoles())
            rolesJson.add(r.getId());

        newRole = new PropertyRole("test", wile.getCompany(), CREATE_DOCUMENTS, READ_OTHERS_DOCUMENTS,
                EDIT_OTHER_DOCUMENTS, EDIT_PROFILE, READ_OTHER_PROFILE, EDIT_OTHER_PROFILE);

        newRole.getEmployees().add(wile);
        wile.getRoles().add(newRole);

        em.persist(newRole);

        em.flush();
        em.clear();

        wile = em.createNamedQuery("PropertyEmployee.findBySsn", PropertyEmployee.class)
                .setParameter("ssn", "123")
                .getSingleResult();

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

        Assert.assertNull(newRole);
        Assert.assertEquals(initialRolesCount, wile.getRoles().size());
    }

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        newRole = em.createNamedQuery("PropertyRole.findByName", PropertyRole.class)
                .setParameter("name", "test")
                .getSingleResult();

        em.remove(newRole);

        em.flush();
    }

}
