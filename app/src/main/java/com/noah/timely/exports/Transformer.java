package com.noah.timely.exports;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Transformer class implementation to tranform any data into xml representation
 */
public class Transformer {

   /**
    * Types of data that can be transformed by the Transformer
    */
   public enum Type {
      METADATA, DATAMODEL
   }

   private static void checkType(Type type) {
      boolean found = false;
      for (Type tp : Type.values()) {
         if (tp == type) {
            found = true;
            break;
         }
      }
      if (!found) throw new IllegalArgumentException(type + " is not supported");
   }

   /**
    * Transforms a map into xml
    *
    * @param type the transformation type
    * @param map  the map to be transformed
    * @return the required xml representation of the map
    */
   public static String getXML(Type type, Map<String, String> map) {
      checkType(type);
      String xmlString;
      Set<Map.Entry<String, String>> entrySet = map.entrySet();

      try {
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
         Document document = documentBuilder.newDocument();
         // root element
         Element rootElement;

         if (type == Type.DATAMODEL) {
            rootElement = document.createElement("Table");
            document.appendChild(rootElement);
            // structure elements
            Element structureElement = document.createElement("TableStructure");
//            rootElement.setAttribute("name", "Timetable");
            rootElement.appendChild(structureElement);
         } else {
            rootElement = document.createElement("metadata");
            document.appendChild(rootElement);
            // data elements
            for (Map.Entry<String, String> entry : entrySet) {
               Element dataElement = document.createElement("data");
               dataElement.setAttribute("name", entry.getKey());
               dataElement.setTextContent(entry.getValue());
               rootElement.appendChild(dataElement);
            }
         }

         // then transform generated XML to string representation
         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
         // prettier
         transformer.setOutputProperty(OutputKeys.METHOD, "xml");
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
         transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(3));
         // to string result
         StringWriter stringWriter = new StringWriter();
         StreamResult streamResult = new StreamResult(stringWriter);
         DOMSource domSource = new DOMSource(document);
         transformer.transform(domSource, streamResult);
         // results
         xmlString = stringWriter.toString().trim();
      } catch (ParserConfigurationException | TransformerException e) {
         return null;
      }

      return xmlString;
   }

}
