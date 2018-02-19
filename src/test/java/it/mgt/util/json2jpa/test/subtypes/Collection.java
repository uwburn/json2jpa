package it.mgt.util.json2jpa.test.subtypes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.util.json2jpa.Json2Jpa;
import it.mgt.util.json2jpa.Json2JpaFactory;
import it.mgt.util.json2jpa.test.config.SpringContext;
import it.mgt.util.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.util.json2jpa.test.subtypes.entity.Employee;
import it.mgt.util.json2jpa.test.subtypes.entity.Party;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = {StandaloneDataConfig.class, SpringContext.class })
public class Collection {

    private static final Logger logger = LoggerFactory.getLogger(Collection.class);

    @PersistenceContext
    private EntityManager em;

	@Autowired
	private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;


	private Employee newEmployee;
	private Student newStudent;


    @Before
    @Transactional
    public void before() {
        objectMapper.getSubtypeResolver().registerSubtypes(new NamedType(Student.class));
    }

	@Test
    @Transactional
	public void test()  {
        logger.info("Testing collection construct");

        Date now = new Date();

        ArrayNode json = objectMapper.createArrayNode();

        ObjectNode employeeObject = json.addObject();
        employeeObject.put("firstName", "John");
        employeeObject.put("lastName", "Doe");
        employeeObject.put("ssn", "000");
        employeeObject.put("hiringDate", now.getTime());
        employeeObject.put("@type", Employee.class.getSimpleName());

        ObjectNode studentObject = json.addObject();
        studentObject.put("firstName", "Mary");
        studentObject.put("lastName", "Smith");
        studentObject.put("ssn", "001");
        studentObject.put("section", "A");
        studentObject.put("@type", Student.class.getSimpleName());

        java.util.Collection<Person> collection = new ArrayList<>();

        Json2Jpa json2Jpa = json2JpaFactory.build();
        collection = json2Jpa.merge(collection, Person.class, json);

        em.flush();
        em.clear();

        Assert.assertEquals(2, collection.size());

        newEmployee = em.createNamedQuery("Employee.findBySsn", Employee.class)
                .setParameter("ssn", "000")
                .getSingleResult();

        Assert.assertNotNull(newEmployee);
        Assert.assertEquals("John", newEmployee.getFirstName());
        Assert.assertEquals("Doe", newEmployee.getLastName());
        Assert.assertEquals("000", newEmployee.getSsn());
        Assert.assertTrue(newEmployee instanceof Employee);
        Assert.assertEquals(now, newEmployee.getHiringDate());

        newStudent = em.createNamedQuery("Student.findBySsn", Student.class)
                .setParameter("ssn", "001")
                .getSingleResult();

        Assert.assertNotNull(newStudent);
        Assert.assertEquals("Mary", newStudent.getFirstName());
        Assert.assertEquals("Smith", newStudent.getLastName());
        Assert.assertEquals("001", newStudent.getSsn());
        Assert.assertTrue(newStudent instanceof Student);
        Assert.assertEquals("A", newStudent.getSection());
	}

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        em.remove(newEmployee);
        em.remove(newStudent);

        em.flush();
    }

}
