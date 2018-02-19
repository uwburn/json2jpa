package it.mgt.util.json2jpa.test.subtypes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.util.json2jpa.Json2Jpa;
import it.mgt.util.json2jpa.Json2JpaFactory;
import it.mgt.util.json2jpa.test.config.SpringContext;
import it.mgt.util.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.util.json2jpa.test.subtypes.entity.Employee;
import it.mgt.util.json2jpa.test.subtypes.entity.Person;
import it.mgt.util.json2jpa.test.subtypes.entity.Student;
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
import java.util.Date;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = {StandaloneDataConfig.class, SpringContext.class })
public class RootDeclarative {

    private static final Logger logger = LoggerFactory.getLogger(RootDeclarative.class);

    @PersistenceContext
    private EntityManager em;

	@Autowired
	private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;


	private Person newPerson;


    @Before
    @Transactional
    public void before() {
        objectMapper.getSubtypeResolver().registerSubtypes(new NamedType(Student.class));
    }

	@Test
    @Transactional
	public void test()  {
        logger.info("Testing construct for a declaratively mapped entity");

        Date now = new Date();

        ObjectNode json = objectMapper.createObjectNode();
        json.put("firstName", "John");
        json.put("lastName", "Doe");
        json.put("ssn", "000");
        json.put("hiringDate", now.getTime());
        json.put("@type", Employee.class.getSimpleName());

        Json2Jpa json2Jpa = json2JpaFactory.build();
        newPerson = json2Jpa.construct(Person.class, json);

        em.flush();
        em.clear();

        newPerson = em.createNamedQuery("Employee.findBySsn", Employee.class)
                .setParameter("ssn", "000")
                .getSingleResult();

        Assert.assertNotNull(newPerson);
        Assert.assertEquals("John", newPerson.getFirstName());
        Assert.assertEquals("Doe", newPerson.getLastName());
        Assert.assertEquals("000", newPerson.getSsn());
        Assert.assertTrue(newPerson instanceof Employee);
        Assert.assertEquals(now, ((Employee) newPerson).getHiringDate());
	}

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        em.remove(newPerson);

        em.flush();
    }

}
