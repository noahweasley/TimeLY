package com.noah.timely.exports;

import com.noah.timely.core.DataModel;
import com.noah.timely.util.Constants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.List;
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
    * Transforms a map into xml
    *
    * @param type the transformation type
    * @param map  the map to be transformed
    * @return the required xml representation of the map
    */
   public static <T> String getXML(Class<?> type, T data) {
      String xmlString;

      try {
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
         Document document = documentBuilder.newDocument();
         // root element
         Element rootElement;
         // export main table data - data models
         if (type == Object[].class) {
            Object[] ss = (Object[]) data;
            List<DataModel> dataModelList = (List<DataModel>) ss[1];
            if (dataModelList.size() == 0) return null;
            rootElement = document.createElement("Table");
            rootElement.setAttribute("name", getProperTableName((String) ss[0]));
            document.appendChild(rootElement);
            // structure and data elements
            Element structureElement = document.createElement("TableStructure");
            Element dataElement = document.createElement("TableData");
            rootElement.appendChild(structureElement);
         } else {
            // export metadata
            Map<String, String> map = (Map<String, String>) data;
            Set<Map.Entry<String, String>> entrySet = map.entrySet();
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


   private static String getProperTableName(String dataModelIdentifier) {
      if (Constants.ASSIGNMENT.equals(dataModelIdentifier)) {
         return "Assignments";
      } else if (Constants.COURSE.equals(dataModelIdentifier)) {
         return "Courses";
      } else if (Constants.EXAM.equals(dataModelIdentifier)) {
         return "Exams";
      } else if (Constants.TIMETABLE.equals(dataModelIdentifier)) {
         return "Timetable";
      } else if (Constants.SCHEDULED_TIMETABLE.equals(dataModelIdentifier)) {
         return "Scheduled Timetable";
      }
      throw new IllegalArgumentException("The identifier " + dataModelIdentifier + " doesn't exists in database");

   }
}
