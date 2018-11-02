package com.apiherd.waggle;

import com.apiherd.api.APIable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class JaredAPI {
    private String name;
    private String classPath;
    private String dbName;
    private int maxCaches;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public int getMaxCaches() {
        return maxCaches;
    }

    public void setMaxCaches(int maxCaches) {
        this.maxCaches = maxCaches;
    }

    public static void parseJaredAPI(BuffedInvokePool threads, String bookFile) throws Exception {
        File file = new File(bookFile);
        if (!file.exists())
            return ;

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(false);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(bookFile);
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        NodeList mNodes = (NodeList)xpath.evaluate("/Jars/Jar", doc, XPathConstants.NODESET);
        for (int i = 0; i < mNodes.getLength(); i++) {
            String jarPath = (String)xpath.evaluate("JarPath/text()", mNodes.item(i), XPathConstants.STRING);
            File jar = new File(jarPath);
            if (!jar.exists())
                continue;
            ArrayList<JaredAPI> functions = parseAPIFunctions(mNodes.item(i));
            URLClassLoader urlLoader = new URLClassLoader (new URL[]{jar.toURI().toURL()}, JaredAPI.class.getClassLoader());
            for (JaredAPI func:functions) {
                Class classToLoad = Class.forName(func.getClassPath(), true, urlLoader);
                Object obj = classToLoad.newInstance();
                threads.registerAPI(func.getName(), (APIable)obj);
            }
        }
    }

    private static ArrayList<JaredAPI> parseAPIFunctions(Node jarNode)
            throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {

        ArrayList<JaredAPI> list = new ArrayList<>();
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        NodeList mNodes = (NodeList)xpath.evaluate("APIFunctions/APIFunction", jarNode, XPathConstants.NODESET);
        for (int i = 0; i < mNodes.getLength(); i++)
            list.add(JaredAPI.parseNode(mNodes.item(i)));

        return list;
    }

    public static JaredAPI parseNode(Node xmlNode) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        String name = (String)xpath.evaluate("FunctionName/text()", xmlNode, XPathConstants.STRING);
        String path = (String)xpath.evaluate("ClassName/text()", xmlNode, XPathConstants.STRING);
        String dbName = (String)xpath.evaluate("DBName/text()", xmlNode, XPathConstants.STRING);
        String maxCaches = (String)xpath.evaluate("CachedNumber/text()", xmlNode, XPathConstants.STRING);

        JaredAPI func = new JaredAPI();
        func.setName(name);
        func.setClassPath(path);
        func.setDbName(dbName);
        func.setMaxCaches(Integer.valueOf(maxCaches));

        return func;
    }
}
