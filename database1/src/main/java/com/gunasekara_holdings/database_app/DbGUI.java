package com.gunasekara_holdings.database_app;
import netscape.javascript.JSObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;


import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import javax.swing.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

//import org.apache.commons.httpclient.*;
//import org.apache.commons.httpclient.methods.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;


public class DbGUI extends JFrame {
    private static JTextField vehicleNo;
    private JTextField customerName;
    private JTextField quantity;
    private JButton addBtn;
    private JButton showBtn;
    private static String VehicleNum, CustomerName;
    private static int Quantity;

    public DbGUI(){
        super("Customer Information");
        setLayout(new FlowLayout());

        JLabel vehicleLabel=new JLabel("Vehicle No");
        add(vehicleLabel);

        vehicleNo=new JTextField(10);
        add(vehicleNo);

        JLabel customerLabel=new JLabel("Customer Name");
        add(customerLabel);

        customerName=new JTextField(10);
        add(customerName);

        JLabel quantityLabel=new JLabel("Quantity");
        add(quantityLabel);

        quantity=new JTextField(10);
        add(quantity);

        addBtn=new JButton("Add");
        add(addBtn);

        showBtn=new JButton("Show");
        add(showBtn);

        thehandler handler= new thehandler();
        vehicleNo.addActionListener(handler);
        customerName.addActionListener(handler);
        quantity.addActionListener(handler);
        addBtn.addActionListener(handler);
        showBtn.addActionListener(handler);
    }

    private class thehandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {

            if (event.getSource() == vehicleNo) {
                VehicleNum = event.getActionCommand();

            } else if (event.getSource() == customerName) {
                CustomerName = event.getActionCommand();

            } else if (event.getSource() == quantity) {
                Quantity = Integer.parseInt(event.getActionCommand());

            }else if (event.getSource()==addBtn){
                DbGUI.postAction();

            }else if (event.getSource()==showBtn){
                try {
                    DbGUI.getAction();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
     static void postAction(){
        JSONObject entry = new JSONObject();
        entry.put("vehicleNo",VehicleNum);
        entry.put("name",CustomerName);
         entry.put("quantity",Quantity);

         DefaultHttpClient httpClient = new DefaultHttpClient();
         httpClient.getParams().setParameter("http.socket.timeout", 0);

         HttpPost postRequest = new HttpPost("http://localhost:9092/customerinfo/addinfo");

         try {
             postRequest.setEntity(new StringEntity(entry.toString()));
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }

         postRequest.addHeader("Content-Type","application/json");

         HttpResponse response = null;
         try {
             response = httpClient.execute(postRequest);
         } catch (IOException e) {
             e.printStackTrace();
         }
         int reply = response.getStatusLine().getStatusCode();

         httpClient.getConnectionManager().shutdown();
         System.out.println("Field added successfully");
    }

    static void getAction() throws IOException, ParseException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try
        {
            HttpGet getRequest = new HttpGet("http://localhost:9092/customerinfo/getinfo");
            HttpResponse response = httpClient.execute(getRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200)
            {
                throw new RuntimeException("Failed with HTTP error code : " + statusCode);
            }
            HttpEntity httpEntity = response.getEntity();
            String apiOutput = EntityUtils.toString(httpEntity);
            System.out.println("Database - Customer Information");
            try{
                JSONParser parser = new JSONParser();
                Object obj1 = parser.parse(apiOutput);
                JSONArray array = (JSONArray)obj1;
                for (int i=0;i<array.size();i++){
                    Object obj2=array.get(i);
                    System.out.println("Customer "+(i+1)+" : "+((JSONObject) obj2).get("name")+"|"+ ((JSONObject) obj2).get("vehicleNo")+"|"+((JSONObject) obj2).get("quantity"));
                }
            }catch(ParseException pe) {
                System.out.println("position: " + pe.getPosition());
                System.out.println(pe);
            }
        }
        finally
        {
            httpClient.getConnectionManager().shutdown();
        }
    }

    public static void main(String args[]){
        DbGUI DB1=new DbGUI();
        DB1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DB1.setSize(450,200);
        DB1.setVisible(true);
    }
}
