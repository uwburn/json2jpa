package it.mgt.util.json2jpa.test.subtypes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.util.json2jpa.Json2Jpa;
import it.mgt.util.json2jpa.Json2JpaFactory;
import it.mgt.util.json2jpa.test.config.SpringContext;
import it.mgt.util.json2jpa.test.config.StandaloneDataConfig;
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
public class RootProgrammatic {

    private static final Logger logger = LoggerFactory.getLogger(RootProgrammatic.class);

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
        logger.info("Testing construct for a programmatically mapped entity");

        Date now = new Date();

        ObjectNode json = objectMapper.createObjectNode();
        json.put("firstName", "Mary");
        json.put("lastName", "Smith");
        json.put("ssn", "001");
        json.put("section", "A");
        json.put("@type", Student.class.getSimpleName());

        Json2Jpa json2Jpa = json2JpaFactory.build();
        newPerson = json2Jpa.construct(Person.class, json);

        em.flush();
        em.clear();

        newPerson = em.createNamedQuery("Student.findBySsn", Student.class)
                .setParameter("ssn", "001")
                .getSingleResult();

        Assert.assertNotNull(newPerson);
        Assert.assertEquals("Mary", newPerson.getFirstName());
        Assert.assertEquals("Smith", newPerson.getLastName());
        Assert.assertEquals("001", newPerson.getSsn());
        Assert.assertTrue(newPerson instanceof Student);
        Assert.assertEquals("A", ((Student) newPerson).getSection());
	}

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        em.remove(newPerson);

        em.flush();
    }

}
