package it.mgt.jpa.json2jpa.test.field;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import it.mgt.jpa.json2jpa.test.config.SpringContext;
import it.mgt.jpa.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.jpa.json2jpa.test.field.component.FieldHelper;
import it.mgt.jpa.json2jpa.test.field.entity.FieldBook;
import it.mgt.jpa.json2jpa.test.field.entity.FieldBookstore;
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
public class Serialize {

    private static final Logger logger = LoggerFactory.getLogger(Serialize.class);

    @PersistenceContext
    private EntityManager em;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
    private FieldHelper helper;

    @Before
    @Transactional
    public void before() {
        helper.initialize();
    }

	@Test
    @Transactional
	public void test() throws JsonProcessingException {
        logger.info("Testing serialization");

        FieldBook book = em.createNamedQuery("FieldBook.findByIsbn", FieldBook.class)
                .setParameter("isbn", "001")
                .getSingleResult();

        FieldBookstore store = em.createNamedQuery("FieldBookstore.findByName", FieldBookstore.class)
                .setParameter("name", "Duckburg Store")
                .getSingleResult();

        ObjectWriter writer = objectMapper.writerWithView(Serialize.class);

        /*String bookJson =*/ writer.writeValueAsString(book);
        /*String storeJson =*/ writer.writeValueAsString(store);

        // Assert.assertEquals("{\"id\":1,\"title\":\"Topolino\",\"isbn\":\"001\",\"price\":4.0}", bookJson);
        // Assert.assertEquals("{\"id\":2,\"name\":\"Duckburg Store\",\"director\":{\"id\":3,\"firstName\":\"Scrooge\",\"lastName\":\"McDuck\"},\"employees\":[{\"id\":3,\"firstName\":\"Scrooge\",\"lastName\":\"McDuck\"},{\"id\":4,\"firstName\":\"Donald\",\"lastName\":\"McDuck\"}],\"books\":[1,2,3]}", storeJson);
	}

}
