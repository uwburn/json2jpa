package it.mgt.jpa.json2jpa.test.property;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import it.mgt.jpa.json2jpa.test.config.SpringContext;
import it.mgt.jpa.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.jpa.json2jpa.test.property.component.PropertyHelper;
import it.mgt.jpa.json2jpa.test.property.entity.PropertyBook;
import it.mgt.jpa.json2jpa.test.property.entity.PropertyBookstore;
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
public class Serialize {

    private static final Logger logger = LoggerFactory.getLogger(Serialize.class);

    @PersistenceContext
    private EntityManager em;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
    private PropertyHelper helper;

    @Before
    @Transactional
    public void before() {
        helper.initialize();
    }

	@Test
    @Transactional
	public void test() throws JsonProcessingException {
        logger.info("Testing serialization");

        PropertyBook book = em.createNamedQuery("PropertyBook.findByIsbn", PropertyBook.class)
                .setParameter("isbn", "001")
                .getSingleResult();

        PropertyBookstore store = em.createNamedQuery("PropertyBookstore.findByName", PropertyBookstore.class)
                .setParameter("name", "Duckburg Store")
                .getSingleResult();

        ObjectWriter writer = objectMapper.writerWithView(Serialize.class);

        /*String bookJson =*/ writer.writeValueAsString(book);
        /*String storeJson =*/ writer.writeValueAsString(store);

        //Assert.assertEquals("{\"id\":1,\"title\":\"Topolino\",\"isbn\":\"001\",\"price\":4.0}", bookJson);
        //Assert.assertEquals("{\"id\":2,\"name\":\"Duckburg Store\",\"director\":{\"id\":3,\"firstName\":\"Scrooge\",\"lastName\":\"McDuck\"},\"employees\":[{\"id\":3,\"firstName\":\"Scrooge\",\"lastName\":\"McDuck\"},{\"id\":4,\"firstName\":\"Donald\",\"lastName\":\"McDuck\"}],\"books\":[1,2,3]}", storeJson);
	}

}
