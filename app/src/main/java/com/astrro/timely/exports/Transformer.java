package com.astrro.timely.exports;

import android.text.TextUtils;
import android.util.Log;

import com.astrro.timely.assignment.AssignmentModel;
import com.astrro.timely.core.DataModel;
import com.astrro.timely.courses.CourseModel;
import com.astrro.timely.exam.ExamModel;
import com.astrro.timely.timetable.TimetableModel;
import com.astrro.timely.util.Constants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
class Transformer {

   /**
    * Transforms a map into xml
    *
    * @param type the transformation type
    * @param map  the map to be transformed
    * @return the required xml representation of the map
    */
   @SuppressWarnings("unchecked")
   public static <T> String getXML(T data) {
      String xmlString;

      try {
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
         Document document = documentBuilder.newDocument();
         // root element
         Element rootElement;
         // export main table data - data models
         if (data.getClass() == Object[].class) {
            Object[] ss = (Object[]) data;
            String identifier = (String) ss[0];

            List<DataModel> dataModelList = (List<DataModel>) ss[1];
            if (dataModelList.size() == 0) return null;

            rootElement = document.createElement("Table");
            rootElement.setAttribute("name", getProperTableName(identifier));
            document.appendChild(rootElement);

            // structure and data elements
            Element dataElement = document.createElement("TableData");
            rootElement.appendChild(dataElement);
            // add list nodes of dataModelList as child nodes to dataElement
            appendTableData(identifier, document, dataElement, dataModelList);

         } else if (data.getClass() == HashMap.class) {
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
         } else {
            throw new IllegalArgumentException(data.getClass() + " is not supported");
         }

         // then transform generated XML to string representation
         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
         // prettier
         transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
//         transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(3));
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

   /**
    * Transforms xml data representation of data model list into list of data models
    *
    * @param datamodelIdentifier the identifier constant of the data contained in the xml
    * @param xml                 the xml string data
    * @return the required List of DataModels
    */
   public static List<? extends DataModel> getDataModel(String datamodelIdentifier, String xml) {
      List<? extends DataModel> dataModels = new ArrayList<>();

      try {
         XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
         XmlPullParser xpp = xppf.newPullParser();
         xpp.setInput(new StringReader(xml));
         // get events
         String tableName = null, dataTagNode = null;
         boolean isDataFound = false, isDataRead = false;

         int eventType = xpp.getEventType();
         while (eventType != XmlPullParser.END_DOCUMENT) {
            final String tagName = xpp.getName();
            if (eventType == XmlPullParser.START_TAG) {
               if (tagName.equals("Table")) {
                  tableName = xpp.getAttributeValue(0);
                  xpp.next();
               }

               if (tagName.equals("TableData")) {
                  isDataFound = true;
                  xpp.next();
               }

               if (haveGotDataElement(tagName)) {
                  switch (tagName) {
                     case "Assignment":
                        dataModels = readAssignmenData(xpp);
                        break;
                     case "Registered-Course":
                        dataModels = readCourseData(xpp);
                        break;
                     case "Exam":
                        dataModels = readExamData(xpp);
                        break;
                     case "Timetable":
                     case "Scheduled-Timetable":
                        dataModels = readTimetableData(xpp);
                        break;
                     default:
                        throw new IllegalStateException(dataTagNode + " is not supported");
                  }
               }

            } else if (eventType == XmlPullParser.END_TAG) {
               if (haveGotDataElement(tagName)) {
                  dataTagNode = null;
               }
            }

            eventType = xpp.next();
         }

      } catch (XmlPullParserException | IOException e) {
         // debug
         Log.e(Transformer.class.getSimpleName(), e.getMessage());
         return null;
      }

      return dataModels;
   }

   private static boolean haveGotDataElement(String tagName) {
      String[] dataTags = { "Assignment", "Exam", "Registered-Course", "Scheduled-Timetable", "Timetable" };
      return Arrays.binarySearch(dataTags, tagName) >= 0;
   }

   private static List<CourseModel> readCourseData(XmlPullParser xpp) throws XmlPullParserException, IOException {
      List<CourseModel> courseModels = new ArrayList<>();

      CourseModel courseModel = null;
      boolean isDataFound = false;
      String dataTagName = "";
      int eventType = xpp.getEventType();

      while (eventType != XmlPullParser.END_DOCUMENT) {
         if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("Registered-Course")) {
            // course model found
            isDataFound = true;
            courseModel = new CourseModel();
         } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equals("Registered-Course")) {
            // reset flag and prepare to read another course model
            isDataFound = false;
            courseModels.add(courseModel);
         } else if (isDataFound) {
            // START_TAG event is emitted, so the next should be a TEXT event. Store the start tag's name and
            // then prepare to read the text in-between the start and end tags
            if (eventType == XmlPullParser.START_TAG) dataTagName = xpp.getName();

            if (eventType == XmlPullParser.TEXT) {
               // dataTagName can never be empty at this point, but to just be on a safer side, still check
               if (!TextUtils.isEmpty(dataTagName)) {
                  switch (dataTagName) {
                     case "id":
                        courseModel.setId(Integer.parseInt(xpp.getText()));
                        break;
                     case "position":
                        courseModel.setPosition(Integer.parseInt(xpp.getText()));
                        break;
                     case "Semester":
                        courseModel.setSemester(xpp.getText());
                        break;
                     case "Credits":
                        courseModel.setCredits(Integer.parseInt(xpp.getText()));
                        break;
                     case "Course-Code":
                        courseModel.setCourseCode(xpp.getText());
                        break;
                     case "Course-Title":
                        courseModel.setCourseName(xpp.getText());
                        break;
                  }
               }
            }
         }

         eventType = xpp.next();
      }

      return courseModels;
   }

   private static List<AssignmentModel> readAssignmenData(XmlPullParser xpp) throws XmlPullParserException, IOException {
      List<AssignmentModel> assignmentModels = new ArrayList<>();

      AssignmentModel assignmentModel = null;
      boolean isDataFound = false;
      String dataTagName = "";
      int eventType = xpp.getEventType();

      while (eventType != XmlPullParser.END_DOCUMENT) {
         if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("Assignment")) {
            // assignment model found
            isDataFound = true;
            assignmentModel = new AssignmentModel();
         } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equals("Assignment")) {
            // reset flag and prepare to read another assignment model
            isDataFound = false;
            assignmentModels.add(assignmentModel);
         } else if (isDataFound) {
            // START_TAG event is emitted, so the next should be a TEXT event. Store the start tag's name and
            // then prepare to read the text in-between the start and end tags
            if (eventType == XmlPullParser.START_TAG) dataTagName = xpp.getName();

            if (eventType == XmlPullParser.TEXT) {
               // dataTagName can never be empty at this point, but to just be on a safer side, still check
               if (!TextUtils.isEmpty(dataTagName)) {
                  switch (dataTagName) {
                     case "id":
                        assignmentModel.setId(Integer.parseInt(xpp.getText()));
                        break;
                     case "position":
                        assignmentModel.setPosition(Integer.parseInt(xpp.getText()));
                        break;
                     case "Submission-Status":
                        assignmentModel.setSubmitted(Boolean.parseBoolean(xpp.getText()));
                        break;
                     case "Description":
                        assignmentModel.setDescription(xpp.getText());
                        break;
                     case "Course-Code":
                        assignmentModel.setCourseCode(xpp.getText());
                        break;
                     case "Lecturer-Name":
                        assignmentModel.setLecturerName(xpp.getText());
                        break;
                     case "Submission-Date": {
                        // FIXME: 3/24/2022 merge submission date into a single instance variable
                        assignmentModel.setDate(xpp.getText());
                        assignmentModel.setSubmissionDate(xpp.getText());
                        break;
                     }
                     case "Title":
                        assignmentModel.setTitle(xpp.getText());
                        break;
                  }
               }
            }

         }

         eventType = xpp.next();

      }

      return assignmentModels;
   }

   private static List<ExamModel> readExamData(XmlPullParser xpp) throws XmlPullParserException, IOException {
      List<ExamModel> examModels = new ArrayList<>();

      ExamModel examModel = null;
      boolean isDataFound = false;
      String dataTagName = "";
      int eventType = xpp.getEventType();

      while (eventType != XmlPullParser.END_DOCUMENT) {
         if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("Exam")) {
            // exam model found
            isDataFound = true;
            examModel = new ExamModel();
         } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equals("Exam")) {
            // reset flag and prepare to read another exam model
            isDataFound = false;
            examModels.add(examModel);
         } else if (isDataFound) {
            // START_TAG event is emitted, so the next should be a TEXT event. Store the start tag's name and
            // then prepare to read the text in-between the start and end tags
            if (eventType == XmlPullParser.START_TAG) dataTagName = xpp.getName();

            if (eventType == XmlPullParser.TEXT) {
               // dataTagName can never be empty at this point, but to just be on a safer side, still check
               if (!TextUtils.isEmpty(dataTagName)) {
                  switch (dataTagName) {
                     case "id":
                        examModel.setId(Integer.parseInt(xpp.getText()));
                        break;
                     case "position":
                        examModel.setPosition(Integer.parseInt(xpp.getText()));
                        break;
                     case "Week":
                        examModel.setWeek(xpp.getText());
                        break;
                     case "Day":
                        examModel.setDay(xpp.getText());
                        break;
                     case "Course-Code":
                        examModel.setCourseCode(xpp.getText());
                        break;
                     case "Course-Title":
                        examModel.setCourseName(xpp.getText());
                        break;
                     case "Start-Time":
                        examModel.setStart(xpp.getText());
                        break;
                     case "End-Time":
                        examModel.setEnd(xpp.getText());
                        break;
                  }
               }
            }
         }

         eventType = xpp.next();
      }

      return examModels;
   }

   private static List<TimetableModel> readTimetableData(XmlPullParser xpp) throws XmlPullParserException, IOException {
      List<TimetableModel> timetableModels = new ArrayList<>();

      TimetableModel timetableModel = null;
      boolean isDataFound = false;
      String dataTagName = "";
      int eventType = xpp.getEventType();

      while (eventType != XmlPullParser.END_DOCUMENT) {
         if (eventType == XmlPullParser.START_TAG
                 && (xpp.getName().equals("Scheduled-Timetable") || xpp.getName().equals("Timetable"))) {
            // timetable model found
            isDataFound = true;
            timetableModel = new TimetableModel();
         } else if (eventType == XmlPullParser.END_TAG
                 && (xpp.getName().equals("Scheduled-Timetable") || xpp.getName().equals("Timetable"))) {
            // reset flag and prepare to read another timetable model
            isDataFound = false;
            timetableModels.add(timetableModel);
         } else if (isDataFound) {
            // START_TAG event is emitted, so the next should be a TEXT event. Store the start tag's name and
            // then prepare to read the text in-between the start and end tags
            if (eventType == XmlPullParser.START_TAG) dataTagName = xpp.getName();

            if (eventType == XmlPullParser.TEXT) {
               // dataTagName can never be empty at this point, but to just be on a safer side, still check
               if (!TextUtils.isEmpty(dataTagName)) {
                  switch (dataTagName) {
                     case "id":
                        timetableModel.setId(Integer.parseInt(xpp.getText()));
                        break;
                     case "position":
                        timetableModel.setPosition(Integer.parseInt(xpp.getText()));
                        break;
                     case "Day":
                        timetableModel.setDay(xpp.getText());
                        break;
                     case "Course-Code":
                        timetableModel.setCourseCode(xpp.getText());
                        break;
                     case "Course-Title":
                        timetableModel.setFullCourseName(xpp.getText());
                        break;
                     case "Start-Time":
                        timetableModel.setStartTime(xpp.getText());
                        break;
                     case "End-Time":
                        timetableModel.setEndTime(xpp.getText());
                        break;
                     case "Lecturer-Name":
                        timetableModel.setLecturerName(xpp.getText());
                        break;
                     case "Importance":
                        timetableModel.setImportance(xpp.getText());
                        break;
                  }
               }
            }
         }

         eventType = xpp.next();
      }

      return timetableModels;
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
         return "Scheduled_Timetable";
      }
      throw new IllegalArgumentException("The identifier " + dataModelIdentifier + " doesn't exists in database");

   }

   private static void appendTableData(String identifier, Document doc, Element dataElement, List<DataModel> data) {
      for (DataModel dataModel : data) {
         Element childElement = getElementFrom(identifier, doc, dataModel);
         dataElement.appendChild(childElement);
      }
   }

   private static <T extends DataModel> Element getElementFrom(String identifier, Document doc, T datamodel) {
      if (datamodel instanceof AssignmentModel) {
         return getAssignmentElement(doc, (AssignmentModel) datamodel);
      }
      if (datamodel instanceof CourseModel) {
         return getCourseElement(doc, (CourseModel) datamodel);
      }
      if (datamodel instanceof ExamModel) {
         return getExamElement(doc, (ExamModel) datamodel);
      }
      if (datamodel instanceof TimetableModel) {
         return getTimetableElement(doc, identifier, (TimetableModel) datamodel);
      }
      return null;
   }

   private static Element getAssignmentElement(Document document, AssignmentModel model) {
      Element element = document.createElement("Assignment");
      Element node1 = document.createElement("id");
      node1.setTextContent(String.valueOf(model.getId()));
      Element node2 = document.createElement("position");
      node2.setTextContent(String.valueOf(model.getPosition()));
      Element node3 = document.createElement("Submission-Status");
      node3.setTextContent(String.valueOf(model.isSubmitted()));
      Element node4 = document.createElement("Course-Code");
      node4.setTextContent(model.getCourseCode());
      Element node5 = document.createElement("Description");
      node5.setTextContent(model.getDescription());
      Element node6 = document.createElement("Lecturer-Name");
      node6.setTextContent(model.getLecturerName());
      Element node7 = document.createElement("Title");
      node7.setTextContent(model.getTitle());
      Element node8 = document.createElement("Submission-Date");
      // FIXME: 3/24/2022 merge assignment date into a single instance variable
      String submissionDate = TextUtils.isEmpty(model.getSubmissionDate()) ? model.getDate() : model.getSubmissionDate();
      node8.setTextContent(submissionDate);

      Element[] nodes = { node1, node2, node3, node4, node5, node6, node7, node8 };
      for (Element node : nodes) element.appendChild(node);

      return element;
   }

   private static Element getCourseElement(Document document, CourseModel model) {
      Element element = document.createElement("Registered-Course");
      Element node1 = document.createElement("id");
      node1.setTextContent(String.valueOf(model.getId()));
      Element node2 = document.createElement("position");
      node2.setTextContent(String.valueOf(model.getPosition()));
      Element node3 = document.createElement("Semester");
      node3.setTextContent(String.valueOf(model.getSemester()));
      Element node4 = document.createElement("Credits");
      node4.setTextContent(String.valueOf(model.getCredits()));
      Element node5 = document.createElement("Course-Code");
      node5.setTextContent(model.getCourseCode());
      Element node6 = document.createElement("Course-Title");
      node6.setTextContent(model.getCourseName());

      Element[] nodes = { node1, node2, node3, node4, node5, node6 };
      for (Element node : nodes) element.appendChild(node);

      return element;
   }

   private static Element getExamElement(Document document, ExamModel model) {
      Element element = document.createElement("Exam");
      Element node1 = document.createElement("id");
      node1.setTextContent(String.valueOf(model.getId()));
      Element node2 = document.createElement("position");
      node2.setTextContent(String.valueOf(model.getPosition()));
      Element node3 = document.createElement("Week");
      node3.setTextContent(String.valueOf(model.getWeek()));
      Element node4 = document.createElement("Course-Code");
      node4.setTextContent(model.getCourseCode());
      Element node5 = document.createElement("Day");
      node5.setTextContent(model.getDay());
      Element node6 = document.createElement("Course-Code");
      node6.setTextContent(model.getCourseCode());
      Element node7 = document.createElement("Course-Title");
      node7.setTextContent(model.getCourseName());
      Element node8 = document.createElement("Start-Time");
      node8.setTextContent(model.getStart());
      Element node9 = document.createElement("End-Time");
      node9.setTextContent(model.getEnd());

      Element[] nodes = { node1, node2, node3, node4, node5, node6, node7, node8, node9 };
      for (Element node : nodes) element.appendChild(node);

      return element;
   }

   private static Element getTimetableElement(Document document, String id, TimetableModel model) {
      Element element;
      if (id.equals(Constants.SCHEDULED_TIMETABLE))
         element = document.createElement("Scheduled-Timetable");
      else {
         element = document.createElement("Timetable");
      }

      Element node1 = document.createElement("id");
      node1.setTextContent(String.valueOf(model.getId()));
      Element node2 = document.createElement("position");
      node2.setTextContent(String.valueOf(model.getPosition()));
      Element node3 = document.createElement("Course-Code");
      node3.setTextContent(model.getCourseCode());
      Element node4 = document.createElement("Course-Title");
      node4.setTextContent(model.getFullCourseName());
      Element node5 = document.createElement("Start-Time");
      node5.setTextContent(model.getStartTime());
      Element node6 = document.createElement("End-Time");
      node6.setTextContent(model.getEndTime());
      Element node7 = document.createElement("Day");
      node7.setTextContent(model.getDay());
      Element node8 = document.createElement("Lecturer-Name");
      node8.setTextContent(model.getLecturerName());
      Element node9 = document.createElement("Importance");
      node9.setTextContent(model.getImportance());

      Element[] nodes = { node1, node2, node3, node4, node5, node6, node7, node8, node9 };
      for (Element node : nodes) element.appendChild(node);

      return element;
   }

}