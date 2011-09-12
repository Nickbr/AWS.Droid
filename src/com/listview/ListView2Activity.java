package com.listview;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ListView2Activity extends ListActivity {
	
	private ProgressDialog m_ProgressDialog = null; 
	private ArrayList<Order> m_orders = null;
	private OrderAdapter m_adapter;
	private Runnable viewOrders;

	private static AmazonSQS simpleQueue = null;
	private static List<com.amazonaws.services.sqs.model.Message> lastRecievedMessages = null;
	public static final String QUEUE_URL = "_queue_url"; 
	public static final String MESSAGE_INDEX = "_message_index";
	public static final String MESSAGE_ID = "_message_id";
	public static BasicAWSCredentials credentials = null;	
	private boolean credentials_found;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	try
    	{
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	        startGetCredentials();
	        
	        String o = "Hello second commit";
	        
	        m_orders = new ArrayList<Order>();
	        this.m_adapter = new OrderAdapter(this, R.layout.row, m_orders);
	                setListAdapter(this.m_adapter);
	        
	              
	        viewOrders = new Runnable(){
	            @Override
	            public void run() {
	                getOrders();
	            }
	        };
		    Thread thread =  new Thread(null, viewOrders, "MagentoBackground");
		        thread.start();
		        m_ProgressDialog = ProgressDialog.show(ListView2Activity.this,    
		              "Please wait...", "Retrieving data ...", true);
    	}
    	catch( Exception ex)
    	{
    		String g = ex.getMessage(); 
    	}
    }
    
    private Runnable returnRes = new Runnable() {

        @Override
        public void run() {
            if(m_orders != null && m_orders.size() > 0){
                m_adapter.notifyDataSetChanged();
                int arrayUBound = m_orders.size();
                for(int i=0;i<arrayUBound;i++)
                m_adapter.add(m_orders.get(i));
            }
            m_ProgressDialog.dismiss();
            m_adapter.notifyDataSetChanged();
        }
      };
      
    private void getOrders(){
        try{
            //m_orders = new ArrayList<Order>();
            //Order o1 = new Order();
            //o1.Message="SF services";
            //o1.Subject="Pending";
            //Order o2 = new Order();
            //o2.Message="SF Advertisement";
            //o2.Subject="Completed";
            //m_orders.add(o1);
            //m_orders.add(o2);
        	//recieveMessageBodies("https://queue.amazonaws.com/484583698755/testqueue");
        	

        	ReceiveMessageRequest req = new ReceiveMessageRequest("https://queue.amazonaws.com/484583698755/testqueue");
			req.setRequestCredentials(credentials);
			lastRecievedMessages =  getInstance().receiveMessage(req).getMessages();
			
			for(com.amazonaws.services.sqs.model.Message m : lastRecievedMessages){  
				
				String jsonData = jsonEscape(m.getBody());
				
				GsonBuilder gsonb = new GsonBuilder();
				Gson gson = gsonb.create();
				 
				Order mb = null;
			
				try
				{
				    mb = gson.fromJson(jsonData, Order.class);
				}
				catch(Exception e)
				{
				    e.printStackTrace();
				}
				
				//String messageText = mb.Message;
				m_orders.add(mb);
				DeleteMessageRequest dmr = new DeleteMessageRequest();
				dmr.setReceiptHandle(m.getReceiptHandle());
				dmr.setQueueUrl("https://queue.amazonaws.com/484583698755/testqueue");
				dmr.setRequestCredentials(credentials);
				getInstance().deleteMessage(dmr);
			}

            Log.i("ARRAY", ""+ m_orders.size());
          } 
			catch (Exception e) { 
            Log.e("BACKGROUND_PROC", e.getMessage());
          }
          runOnUiThread(returnRes);
      }
    


	@SuppressWarnings("unchecked")
	public void recieveMessageBodies(String queueUrl){
		try
		{
			ReceiveMessageRequest req = new ReceiveMessageRequest(queueUrl);
			req.setRequestCredentials(credentials);
			lastRecievedMessages =  getInstance().receiveMessage(req).getMessages();
			
			for(com.amazonaws.services.sqs.model.Message m : lastRecievedMessages){  
				
				String jsonData = jsonEscape(m.getBody());
				
				GsonBuilder gsonb = new GsonBuilder();
				Gson gson = gsonb.create();
				 
				Order mb = null;
			
				try
				{
				    mb = gson.fromJson(jsonData, Order.class);
				}
				catch(Exception e)
				{
				    e.printStackTrace();
				}
				
				//String messageText = mb.Message;
				m_orders.add(mb);
				DeleteMessageRequest dmr = new DeleteMessageRequest();
				dmr.setReceiptHandle(m.getReceiptHandle());
				dmr.setQueueUrl("https://queue.amazonaws.com/484583698755/testqueue");
				dmr.setRequestCredentials(credentials);
				getInstance().deleteMessage(dmr);
			}
		}
		catch( Exception exception){
			String bob = exception.getMessage();
			bob = "";
		}
	}
	

    public static String testJsonString(){
    	String x = "{  \"Type\" : \"Notification\",  \"MessageId\" : \"24964a7a-b92a-4b90-9f9b-ed6a80241eee\",  \"TopicArn\" : \"arn:aws:sns:us-east-1:484583698755:TestTopic\",  \"Subject\" : \"Test JSON\",  \"Message\" : \"Known good format not from aws\",  \"Timestamp\" : \"2011-09-07T23:17:53.127Z\",  \"SignatureVersion\" : \"1\",  \"Signature\" : \"e1dCO8WtVnYC+P26erAhhZrS1CVDx1sPi0NikDOIA02mRKpEjuKdxXnOKXsoILeE5U8IgFTOYJxngxFafxQkWScz6c8QAFJpQkcCNBsSI4gpnNrUCpoaLjiaPtjoQhRhGcG3PAT6zj+fO1oh/XZoRqoxAjS8F5xO9ZK9sZhgYp0=\",  \"SigningCertURL\" : \"https://sns.us-east-1.amazonaws.com/SimpleNotificationService-f3ecfb7224c7233fe7bb5f59f96de52f.pem\",  \"UnsubscribeURL\" : \"https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:484583698755:TestTopic:0c97fbe6-e486-445d-8606-e866ff43cb78\"}";
        return x;
    }
	
	public static String jsonEscape(String str)  {
	    String intString = str.replace("\n", "").replace("\r", "").replace("\t", "");
	    String intString4 = intString + "&SubscriptionArn=arn:aws:sns:us-east-1:484583698755:TestTopic:0c97fbe6-e486-445d-8606-e866ff43cb78" + "\"" + "}";
	    return intString4;
	}

	public static AmazonSQS getInstance() {
        if ( simpleQueue == null ) {
		    simpleQueue = new AmazonSQSClient(credentials);
        }
        return simpleQueue;
	}

    private void startGetCredentials() {
    	Thread t = new Thread() {
    		@Override
    		public void run(){
    	        try {            
    	            Properties properties = new Properties();
    	            properties.load( getClass().getResourceAsStream( "AwsCredentials.properties" ) );
    	            
    	            String accessKeyId = properties.getProperty( "accessKey" );
    	            String secretKey = properties.getProperty( "secretKey" );
    	            
    	            if ( ( accessKeyId == null ) || ( accessKeyId.equals( "" ) ) ||
    	            	 ( accessKeyId.equals( "CHANGEME" ) ) ||( secretKey == null )   || 
    	                 ( secretKey.equals( "" ) ) || ( secretKey.equals( "CHANGEME" ) ) ) {
    	                Log.e( "AWS", "Aws Credentials not configured correctly." );                                    
        	            credentials_found = false;
    	            } else {
    	            credentials = new BasicAWSCredentials( properties.getProperty( "accessKey" ), properties.getProperty( "secretKey" ) );
        	        credentials_found = true;
    	            }

    	        }
    	        catch ( Exception exception ) {
    	            Log.e( "Loading AWS Credentials", exception.getMessage() );
    	            credentials_found = false;
    	        }
    			//HelloWorldActivity.this.mHandler.post(postResults);
    		}
    	};
    	t.start();
    }
    
    protected void displayCredentialsIssueAndExit() {
        AlertDialog.Builder confirm = new AlertDialog.Builder( this );
        confirm.setTitle("Credential Problem!");
        confirm.setMessage( "AWS Credentials not configured correctly.  Please review the README file." );
        confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() {
                public void onClick( DialogInterface dialog, int which ) {
                	ListView2Activity.this.finish();
                }
        } );
        confirm.show().show();                
    }
}