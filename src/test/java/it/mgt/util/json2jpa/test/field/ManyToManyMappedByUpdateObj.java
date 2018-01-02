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
public class ManyToManyMappedByUpdateObj {

    private static final Logger logger = LoggerFactory.getLogger(ManyToManyMappedByUpdateObj.class);

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


    private FieldRole role;


    @Test
    @Transactional
    public void test()  {
        logger.info("Testing mapped-by many-to-many object update");

        role = em.createNamedQuery("FieldRole.findByName", FieldRole.class)
                .setParameter("name", "employee")
                .getSingleResult();

        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode employeesJson = json.putArray("employees");
        for (FieldEmployee e : role.getEmployees()) {
            ObjectNode employeeJson = employeesJson.addObject();
            employeeJson.put("id", e.getId());
            employeeJson.put("firstName", e.getFirstName() + "_suffix");
            employeeJson.put("lastName", e.getLastName() + "_suffix");
        }

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(role, json);

        em.flush();
        em.clear();

        role = em.createNamedQuery("FieldRole.findByName", FieldRole.class)
                .setParameter("name", "employee")
                .getSingleResult();

        for (FieldEmployee e : role.getEmployees()) {
            Assert.assertTrue(e.getFirstName().endsWith("_suffix"));
            Assert.assertTrue(e.getLastName().endsWith("_suffix"));
        }
    }

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        for (FieldEmployee e : role.getEmployees()) {
            e.setFirstName(e.getFirstName().substring(0, e.getFirstName().length() - 7));
            e.setLastName(e.getLastName().substring(0, e.getLastName().length() - 7));
        }

        em.flush();
    }

}
