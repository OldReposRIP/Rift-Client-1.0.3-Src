package org.reflections.serializers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.Store;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.Utils;

public class XmlSerializer implements Serializer {

    public Reflections read(InputStream inputStream) {
        Reflections reflections;

        try {
            Constructor e = Reflections.class.getDeclaredConstructor(new Class[0]);

            e.setAccessible(true);
            reflections = (Reflections) e.newInstance(new Object[0]);
        } catch (Exception exception) {
            reflections = new Reflections(new ConfigurationBuilder());
        }

        try {
            Document e1 = (new SAXReader()).read(inputStream);
            Iterator iterator = e1.getRootElement().elements().iterator();

            while (iterator.hasNext()) {
                Object e1 = iterator.next();
                Element index = (Element) e1;
                Iterator iterator1 = index.elements().iterator();

                while (iterator1.hasNext()) {
                    Object e2 = iterator1.next();
                    Element entry = (Element) e2;
                    Element key = entry.element("key");
                    Element values = entry.element("values");
                    Iterator iterator2 = values.elements().iterator();

                    while (iterator2.hasNext()) {
                        Object o3 = iterator2.next();
                        Element value = (Element) o3;

                        reflections.getStore().getOrCreate(index.getName()).put(key.getText(), value.getText());
                    }
                }
            }

            return reflections;
        } catch (DocumentException documentexception) {
            throw new ReflectionsException("could not read.", documentexception);
        } catch (Throwable throwable) {
            throw new RuntimeException("Could not read. Make sure relevant dependencies exist on classpath.", throwable);
        }
    }

    public File save(Reflections reflections, String filename) {
        File file = Utils.prepareFile(filename);

        try {
            Document e = this.createDocument(reflections);
            XMLWriter xmlWriter = new XMLWriter(new FileOutputStream(file), OutputFormat.createPrettyPrint());

            xmlWriter.write(e);
            xmlWriter.close();
            return file;
        } catch (IOException ioexception) {
            throw new ReflectionsException("could not save to file " + filename, ioexception);
        } catch (Throwable throwable) {
            throw new RuntimeException("Could not save to file " + filename + ". Make sure relevant dependencies exist on classpath.", throwable);
        }
    }

    public String toString(Reflections reflections) {
        Document document = this.createDocument(reflections);

        try {
            StringWriter e = new StringWriter();
            XMLWriter xmlWriter = new XMLWriter(e, OutputFormat.createPrettyPrint());

            xmlWriter.write(document);
            xmlWriter.close();
            return e.toString();
        } catch (IOException ioexception) {
            throw new RuntimeException();
        }
    }

    private Document createDocument(Reflections reflections) {
        Store map = reflections.getStore();
        Document document = DocumentFactory.getInstance().createDocument();
        Element root = document.addElement("Reflections");
        Iterator iterator = map.keySet().iterator();

        while (iterator.hasNext()) {
            String indexName = (String) iterator.next();
            Element indexElement = root.addElement(indexName);
            Iterator iterator1 = map.get(indexName).keySet().iterator();

            while (iterator1.hasNext()) {
                String key = (String) iterator1.next();
                Element entryElement = indexElement.addElement("entry");

                entryElement.addElement("key").setText(key);
                Element valuesElement = entryElement.addElement("values");
                Iterator iterator2 = map.get(indexName).get(key).iterator();

                while (iterator2.hasNext()) {
                    String value = (String) iterator2.next();

                    valuesElement.addElement("value").setText(value);
                }
            }
        }

        return document;
    }
}
