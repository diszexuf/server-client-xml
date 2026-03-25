package com.github.diszexuf.xml;

import noNamespace.*;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class XmlProcessor {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String buildRequest(String user, String text) {
        MessageDocument doc = MessageDocument.Factory.newInstance();
        MessageType message = doc.addNewMessage();
        message.addNewHeader().setTime(LocalDateTime.now().format(FMT));
        RequestType request = message.addNewRequest();
        request.setUser(user);
        request.setText(text);
        return doc.xmlText(new XmlOptions().setSavePrettyPrint());
    }

    public static String buildResponse(int code, String reason) {
        MessageDocument doc = MessageDocument.Factory.newInstance();
        MessageType message = doc.addNewMessage();
        message.addNewHeader().setTime(LocalDateTime.now().format(FMT));
        ResponseType response = message.addNewResponse();
        StatusType status = response.addNewStatus();
        status.setCode(java.math.BigInteger.valueOf(code));
        status.setReason(reason);
        return doc.xmlText(new XmlOptions().setSavePrettyPrint());
    }

    public static MessageDocument parse(String xml) throws XmlException {
        return MessageDocument.Factory.parse(xml);
    }
}