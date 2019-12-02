import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class CurrencySummer {

    private String xmlFile;
    private ArrayList<String> dataArray;
    private HashSet<String> dataSet;
    private Double sum;
    private Logger logger;

    public CurrencySummer(String[] args) throws IndexOutOfBoundsException{
        logger = Logger.getLogger("TaskLog");
        FileHandler fh;
        try {
            fh = new FileHandler("logs.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        xmlFile = args[0];
        dataSet = new HashSet<String>();
        dataArray = new ArrayList<String>();
        sum = 0.0;
        for (int i = 1; i < args.length; i++) {
            dataArray.add(args[i]);
            dataSet.add(args[i]);
        }
    }

    public void doWork(){
        try {
            processXml();
        } catch (SAXException e) {
            logger.warning("Error occurred while reading xml");
            e.printStackTrace();
            System.exit(1);
        } catch (XPathExpressionException e) {
            logger.warning("Error occurred while reading xml");
            e.printStackTrace();
            System.exit(1);
        } catch (ParserConfigurationException e) {
            logger.warning("Error occurred while reading xml");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            logger.warning("Error occurred while reading xml");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            Double value = processJson();
            logger.info("Total sum of all acceptable items after transferring to euro " + sum/value);
        }catch (ArithmeticException e){
            logger.warning("При пересчёте курса произошло деление на ноль. Проверьте наличие " +
                    "ядерного гриба за окном и запаситесь крышками, они вам пригодятся");
            e.printStackTrace();
            System.exit(1);
        }
        catch (IOException e) {
            logger.warning("Error occurred while processing currency exchange rate");
            e.printStackTrace();
            System.exit(1);
        }

    }

    private void processXml() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList result = (NodeList) xPath.compile("//item[not(@exclude='true')]/text()").evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < result.getLength(); i++) {
            Node tmp = result.item(i);
            if (dataSet.contains(tmp.getParentNode().getAttributes().item(0).getNodeValue())){
                sum+=new Double(tmp.getNodeValue().replace(',','.'));
            }
        }
    }

    private Double processJson() throws IOException {
        String response;
        try{
           response = sendGetCurrencyRate();
        } catch (IOException e) {
            logger.warning("Error occurred while processing currency exchange rate");
            e.printStackTrace();
            throw e;
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(response);
        return jsonNode.get("Valute").get("EUR").get("Value").asDouble();
    }

    private String sendGetCurrencyRate() throws IOException {
        String address = "https://www.cbr-xml-daily.ru/daily_json.js";
        URL url = new URL(address);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuffer response = new StringBuffer();
        while ((line = input.readLine())!= null){
            response.append(line);
        }
        input.close();
        return response.toString();
    }

    public String getXmlFile() {
        return xmlFile;
    }

    public void setXmlFile(String xmlFile) {
        this.xmlFile = xmlFile;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public HashSet<String> getDataSet(){
        return dataSet;
    }

    public void setDataSet(HashSet<String> dataSet) {
        this.dataSet = dataSet;
    }

    public ArrayList<String> getDataArray(){
        return dataArray;
    }

    public void setDataArray(ArrayList<String> dataArray) {
        this.dataArray = dataArray;
    }
}
