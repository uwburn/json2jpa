package it.mgt.util.json2jpa.test.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.util.json2jpa.Json2Jpa;
import it.mgt.util.json2jpa.Json2JpaFactory;
import it.mgt.util.json2jpa.test.config.SpringContext;
import it.mgt.util.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.util.json2jpa.test.field.component.FieldHelper;
import it.mgt.util.json2jpa.test.field.entity.FieldCompany;
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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = {StandaloneDataConfig.class, SpringContext.class })
public class ConstructCollection {

    private static final Logger logger = LoggerFactory.getLogger(ConstructCollection.class);

    @PersistenceContext
    private EntityManager em;

	@Autowired
	private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

	@Autowired
    private FieldHelper helper;


	private Collection<FieldCompany> newCompanies;


    @Before
    @Transactional
    public void before() {
        helper.initialize();
    }

	@Test
    @Transactional
	public void test()  {
        logger.info("Testing construct");


        ArrayNode json = objectMapper.createArrayNode();
        for (int i = 1; i <= 3; ++i) {
            ObjectNode test1Json = json.addObject();
            test1Json.put("name", "Test_" + i);
            test1Json.put("address", "Nowhere");
            test1Json.put("motto", "Yeah!");
            test1Json.put("anthem", "IGNORED");
        }

        Json2Jpa json2Jpa = json2JpaFactory.build();
        newCompanies = json2Jpa.construct(List.class, FieldCompany.class, json);

        em.flush();
        em.clear();

        newCompanies = em.createNamedQuery("FieldCompany.findAll", FieldCompany.class)
                .getResultList()
                .stream()
                .filter(c -> c.getName().startsWith("Test_"))
                .collect(Collectors.toList());

        Assert.assertEquals(3, newCompanies.size());
        for (FieldCompany c : newCompanies) {
            Assert.assertEquals("Nowhere", c.getAddress());
            Assert.assertNull(c.getMotto());
            Assert.assertNull(c.getAnthem());
        }
	}

	@After
    @Transactional
	public void after() {
        logger.info("Restoring data");

        for (FieldCompany c : newCompanies)
            em.remove(c);

        em.flush();
    }

}
