import com.github.diszexuf.xml.XmlProcessor;
import noNamespace.MessageDocument;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XmlProcessorTest {

    @Test
    void buildRequest_containsUserAndText() throws Exception {
        String xml = XmlProcessor.buildRequest("alice", "hello world");
        MessageDocument doc = XmlProcessor.parse(xml);

        assertEquals("alice", doc.getMessage().getRequest().getUser());
        assertEquals("hello world", doc.getMessage().getRequest().getText());
    }

    @Test
    void buildRequest_headerTimeIsSet() throws Exception {
        String xml = XmlProcessor.buildRequest("alice", "hi");
        MessageDocument doc = XmlProcessor.parse(xml);

        assertNotNull(doc.getMessage().getHeader().getTime());
        assertFalse(doc.getMessage().getHeader().getTime().isBlank());
    }

    @Test
    void buildRequest_noResponseBlock() throws Exception {
        String xml = XmlProcessor.buildRequest("alice", "hi");
        MessageDocument doc = XmlProcessor.parse(xml);

        assertNull(doc.getMessage().getResponse());
    }

    @Test
    void buildResponse_successCode() throws Exception {
        String xml = XmlProcessor.buildResponse(0, "success");
        MessageDocument doc = XmlProcessor.parse(xml);

        assertEquals(0, doc.getMessage().getResponse().getStatus().getCode().intValue());
        assertEquals("success", doc.getMessage().getResponse().getStatus().getReason());
    }

    @Test
    void buildResponse_rejectedCode() throws Exception {
        String xml = XmlProcessor.buildResponse(1, "used inappropriate language");
        MessageDocument doc = XmlProcessor.parse(xml);

        assertEquals(1, doc.getMessage().getResponse().getStatus().getCode().intValue());
        assertEquals("used inappropriate language",
                doc.getMessage().getResponse().getStatus().getReason());
    }

    @Test
    void buildResponse_noRequestBlock() throws Exception {
        String xml = XmlProcessor.buildResponse(0, "success");
        MessageDocument doc = XmlProcessor.parse(xml);

        assertNull(doc.getMessage().getRequest());
    }

    @Test
    void parse_invalidXml_throwsException() {
        assertThrows(Exception.class, () -> XmlProcessor.parse("not xml"));
    }

    @Test
    void parse_emptyString_throwsException() {
        assertThrows(Exception.class, () -> XmlProcessor.parse("   "));
    }

    @Test
    void parse_roundTrip_preservesData() throws Exception {
        String xml = XmlProcessor.buildRequest("bob", "message");
        MessageDocument doc = XmlProcessor.parse(xml);

        assertEquals("bob", doc.getMessage().getRequest().getUser());
        assertEquals("message", doc.getMessage().getRequest().getText());
    }
}
