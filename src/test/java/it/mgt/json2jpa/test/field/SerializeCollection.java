package it.mgt.json2jpa.test.field;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import it.mgt.json2jpa.test.config.SpringContext;
import it.mgt.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.json2jpa.test.field.component.FieldHelper;
import it.mgt.json2jpa.test.field.entity.FieldBook;
import it.mgt.json2jpa.test.field.entity.FieldBookstore;
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
public class SerializeCollection {

    private static final Logger logger = LoggerFactory.getLogger(SerializeCollection.class);

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

        List<FieldBook> books = em.createNamedQuery("FieldBook.findAll", FieldBook.class)
                .getResultList();

        List<FieldBookstore> stores = em.createNamedQuery("FieldBookstore.findAll", FieldBookstore.class)
                .getResultList();

        ObjectWriter writer = objectMapper.writerWithView(SerializeCollection.class);

        /*String booksJson =*/ writer.writeValueAsString(books);
        /*String storesJson =*/ writer.writeValueAsString(stores);

        // Assert.assertEquals("[{\"id\":1,\"title\":\"Topolino\",\"isbn\":\"001\",\"price\":4.0},{\"id\":2,\"title\":\"Mega 2000\",\"isbn\":\"002\",\"price\":5.0},{\"id\":3,\"title\":\"I Grandi Classici\",\"isbn\":\"003\",\"price\":6.5}]", booksJson);
        // Assert.assertEquals("[{\"id\":1,\"name\":\"Mouseton Store\",\"director\":{\"id\":1,\"firstName\":\"Mickey\",\"lastName\":\"Mouse\"},\"employees\":[{\"id\":1,\"firstName\":\"Mickey\",\"lastName\":\"Mouse\"},{\"id\":2,\"firstName\":\"Goofy\",\"lastName\":\"Goof\"}],\"books\":[1,2,3]},{\"id\":2,\"name\":\"Duckburg Store\",\"director\":{\"id\":3,\"firstName\":\"Scrooge\",\"lastName\":\"McDuck\"},\"employees\":[{\"id\":3,\"firstName\":\"Scrooge\",\"lastName\":\"McDuck\"},{\"id\":4,\"firstName\":\"Donald\",\"lastName\":\"McDuck\"}],\"books\":[1,2,3]}]", storesJson);
	}

}
