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
import java.util.List;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = {StandaloneDataConfig.class, SpringContext.class })
public class CollectionBasicUpdate {

    private static final Logger logger = LoggerFactory.getLogger(CollectionBasicUpdate.class);

    @PersistenceContext
    private EntityManager em;

	@Autowired
	private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

	@Autowired
    private FieldHelper helper;


	private List<FieldEmployee> employees;


    @Before
    @Transactional
    public void before() {
        helper.initialize();
    }

	@Test
    @Transactional
	public void test()  {
        logger.info("Testing collection basic property update");

        employees = em.createNamedQuery("FieldEmployee.findAll", FieldEmployee.class)
                .getResultList();

        ArrayNode json = objectMapper.createArrayNode();
        for (FieldEmployee e : employees) {
            ObjectNode employeeJson = json.addObject();
            employeeJson.put("id", e.getId());
            employeeJson.put("firstName", e.getFirstName() + "_suffix");
            employeeJson.put("lastName", e.getLastName() + "_suffix");
        }

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(employees, FieldEmployee.class, json);

        em.flush();
        em.clear();

        employees = em.createNamedQuery("FieldEmployee.findAll", FieldEmployee.class)
                .getResultList();

        for (FieldEmployee e : employees) {
            Assert.assertTrue(e.getFirstName().endsWith("_suffix"));
            Assert.assertTrue(e.getLastName().endsWith("_suffix"));
        }
	}

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        for (FieldEmployee e : employees) {
            e.setFirstName(e.getFirstName().substring(0, e.getFirstName().length() - 7));
            e.setLastName(e.getLastName().substring(0, e.getLastName().length() - 7));
        }

        em.flush();
    }

}
