import com.github.diszexuf.server.ClientHandler;
import com.github.diszexuf.xml.XmlProcessor;
import noNamespace.MessageDocument;
import org.junit.jupiter.api.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerTest {

    private String processMessage(ClientHandler handler, String xml) throws Exception {
        Method method = ClientHandler.class.getDeclaredMethod("processMessage", String.class);
        method.setAccessible(true);
        return (String) method.invoke(handler, xml);
    }

    private ClientHandler handlerWithWords(Set<String> bannedWords) {
        Socket dummySocket = new Socket();
        return new ClientHandler(dummySocket, bannedWords);
    }

    @Test
    void processMessage_cleanText_returnsSuccess() throws Exception {
        ClientHandler handler = handlerWithWords(Set.of("spam", "badword"));
        String request = XmlProcessor.buildRequest("alice", "hello world");

        String response = processMessage(handler, request);
        MessageDocument doc = XmlProcessor.parse(response);

        assertEquals(0, doc.getMessage().getResponse().getStatus().getCode().intValue());
        assertEquals("success", doc.getMessage().getResponse().getStatus().getReason());
    }

    @Test
    void processMessage_bannedWord_returnsRejected() throws Exception {
        ClientHandler handler = handlerWithWords(Set.of("spam", "badword"));
        String request = XmlProcessor.buildRequest("bob", "this is spam message");

        String response = processMessage(handler, request);
        MessageDocument doc = XmlProcessor.parse(response);

        assertEquals(1, doc.getMessage().getResponse().getStatus().getCode().intValue());
        assertEquals("used inappropriate language",
                doc.getMessage().getResponse().getStatus().getReason());
    }

    @Test
    void processMessage_bannedWordCaseInsensitive_returnsRejected() throws Exception {
        ClientHandler handler = handlerWithWords(Set.of("spam"));
        String request = XmlProcessor.buildRequest("bob", "This is SPAM!");

        String response = processMessage(handler, request);
        MessageDocument doc = XmlProcessor.parse(response);

        assertEquals(1, doc.getMessage().getResponse().getStatus().getCode().intValue());
    }

    @Test
    void processMessage_bannedWordAsSubstring_returnsRejected() throws Exception {
        ClientHandler handler = handlerWithWords(Set.of("bad"));
        String request = XmlProcessor.buildRequest("bob", "this is badword");

        String response = processMessage(handler, request);
        MessageDocument doc = XmlProcessor.parse(response);

        assertEquals(1, doc.getMessage().getResponse().getStatus().getCode().intValue());
    }

    @Test
    void processMessage_emptyBannedList_alwaysAccepts() throws Exception {
        ClientHandler handler = handlerWithWords(Set.of());
        String request = XmlProcessor.buildRequest("carol", "spam drugs badword");

        String response = processMessage(handler, request);
        MessageDocument doc = XmlProcessor.parse(response);

        assertEquals(0, doc.getMessage().getResponse().getStatus().getCode().intValue());
    }

    @Test
    void processMessage_invalidXml_returnsInvalidFormat() throws Exception {
        ClientHandler handler = handlerWithWords(Set.of("spam"));

        String response = processMessage(handler, "not valid xml");
        MessageDocument doc = XmlProcessor.parse(response);

        assertEquals(1, doc.getMessage().getResponse().getStatus().getCode().intValue());
        assertEquals("invalid message format",
                doc.getMessage().getResponse().getStatus().getReason());
    }

    @Test
    void processMessage_responseXmlInsteadOfRequest_returnsInvalidFormat() throws Exception {
        ClientHandler handler = handlerWithWords(Set.of("spam"));
        String responseXml = XmlProcessor.buildResponse(0, "success");

        String response = processMessage(handler, responseXml);
        MessageDocument doc = XmlProcessor.parse(response);

        assertEquals(1, doc.getMessage().getResponse().getStatus().getCode().intValue());
        assertEquals("invalid message format",
                doc.getMessage().getResponse().getStatus().getReason());
    }
}