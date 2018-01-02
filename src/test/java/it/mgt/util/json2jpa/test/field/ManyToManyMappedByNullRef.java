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
public class ManyToManyMappedByNullRef {

    private static final Logger logger = LoggerFactory.getLogger(ManyToManyMappedByNullRef.class);

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
        logger.info("Testing mapped-by many-to-many null reference update");

        FieldRole role = em.createNamedQuery("FieldRole.findByName", FieldRole.class)
                .setParameter("name", "employee")
                .getSingleResult();

        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode employeesJson = json.putArray("employees");
        for (FieldEmployee r : role.getEmployees())
            employeesJson.add(r.getId());

        int initialEmployeesCount = role.getEmployees().size();

        FieldEmployee newEmployee = new FieldEmployee("Test", "Test", "000", "test@acme.it", role.getCompany());
        newEmployee.getRoles().add(role);
        role.getEmployees().add(newEmployee);
        em.persist(newEmployee);

        em.flush();
        em.clear();

        role = em.createNamedQuery("FieldRole.findByName", FieldRole.class)
                .setParameter("name", "employee")
                .getSingleResult();

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(role, json);

        em.flush();
        em.clear();

        role = em.createNamedQuery("FieldRole.findByName", FieldRole.class)
                .setParameter("name", "employee")
                .getSingleResult();

        newEmployee = role.getEmployees()
                .stream()
                .filter(e -> e.getSsn().equals("000"))
                .findFirst()
                .orElse(null);

        Assert.assertNull(newEmployee);
        Assert.assertEquals(initialEmployeesCount, role.getEmployees().size());
    }

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        FieldEmployee newEmployee = em.createNamedQuery("FieldEmployee.findBySsn", FieldEmployee.class)
                .setParameter("ssn", "000")
                .getSingleResult();

        em.remove(newEmployee);

        em.flush();
    }

}
