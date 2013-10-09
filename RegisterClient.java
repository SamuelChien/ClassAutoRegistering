package register;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;



public class RegisterClient {

	public static final String COOKIE_HEADER = "Cookie :";
	public static final String SET_COOKIE_HEADER = "Set-Cookie";

	public TrustManager[] tm;
	public ClientCookieStore cookieStore = new ClientCookieStore();

	public RegisterClient(){
		tm  = new X509TrustManager[]{
				new X509TrustManager(){
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					public void checkClientTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
					}
					public void checkServerTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
					}
				}
		};
		// Install the all-trusting trust manager	
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, tm, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}
	}


	public boolean login (String userName, String password) throws ClientProtocolException, IOException{
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("https://www.auth.cwl.ubc.ca/auth/login?serviceName=ytestssc&serviceURL=https://ssc.adm.ubc.ca/sscportal/servlets/SRVSSCFramework");

		httpget.setHeader("Content-Type=", "application/x-www-form-urlencoded");
		httpget.setHeader("Accept","image/jpeg=, application/x-ms-application, image/gif, application/xaml+xml, image/pjpeg, application/x-ms-xbap, application/msword, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/x-shockwave-flash, */*");		
		httpget.setHeader("Accept-Encoding","gzip, deflate" );
		httpget.setHeader("User-Agent=", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Tablet PC 2.0)");
		//This gets us an Initial JSESSIONID
		HttpResponse response = httpclient.execute(httpget);

		Header[] h = response.getAllHeaders();
		BasicClientCookie cookie;		
		
		CharSequence SetCookieRegex = SET_COOKIE_HEADER;
		String[] idValCombo;
		String redirectURL=null;
		for( int i = 0 ; i < h.length; i ++){
			String n = h[i].getName();
			String v = h[i].getValue();			
			if(n.contains(SetCookieRegex)){
				idValCombo = v.split("=");
				cookie = new BasicClientCookie(idValCombo[0],idValCombo[1].substring(0,idValCombo[1].indexOf(";")));
				cookieStore.addCookie(cookie);
			}			
		}
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent()));
		
		//Don't login if already logged in.
		String line;
		while ((line = rd.readLine()) != null) {
			if(line.contains("Student No.:")){
				return true;
			}
		}
		if(cookieStore.getCookieStore().size()>0){					
			return doLogin(redirectURL,userName,password);
		}else 
			System.out.println("No JSESSIONID to send back to the server");
			return false;
	}

	public boolean doLogin (String redirectURL,String userName, String password ) {
		
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("https://www.auth.cwl.ubc.ca/auth/login?");
		post.setHeader("User-Agent=", 
				"Mozilla/4.0 (compatible; " +
						"			  	MSIE 8.0; " +
						"				Windows NT 6.1; " +
						"				Trident/4.0; SLCC2;" +
						"				.NET CLR 2.0.50727; " +
						"				.NET CLR 3.5.30729; " +
						"				.NET CLR 3.0.30729; " +
						"			   Media Center PC 6.0; " +
				"				Tablet PC 2.0)");
		
		Iterator<Cookie> cookieIterator = cookieStore.getCookieStore().iterator();
		while(cookieIterator.hasNext()){
			Cookie c = cookieIterator.next();
			post.setHeader(COOKIE_HEADER,c.getName()+c.getValue());
		}		

		List<NameValuePair> params = new ArrayList<NameValuePair>(1);
		params.add(new BasicNameValuePair("loginName",userName));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("serviceName", "es_coursesched_psa"));
		params.add(new BasicNameValuePair("serviceURL","/cs/main"));
		params.add(new BasicNameValuePair("serviceParams","null"));
		params.add(new BasicNameValuePair("action","Continue >"));
		try {
			post.setEntity(new UrlEncodedFormEntity(params));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HttpResponse response = null;
		try {
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		BufferedReader rd = null;
		try {
			rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(response.getStatusLine());
		//Print the HTML code to the console for debug;
		String line = "";
		try {
			while ((line = rd.readLine()) != null) {
				System.out.println(line);			
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Header[] h = response.getAllHeaders();
		String locationURL = "127.0.0.1";//default so we don't null shit
		Cookie cookie;
		String[] idValCombo;
		for ( int i = 0; i < h.length; i ++){
			if(h[i].getName().contains("Location")){
				locationURL = h[i].getValue();
			}
			if(h[i].getName().contains("SET_COOKIE_HEADER")){				
				idValCombo = h[i].getValue().split("=");
				cookie = new BasicClientCookie(idValCombo[0],idValCombo[1].substring(0,idValCombo[1].indexOf(";")));
				cookieStore.addCookie(cookie);
			}
			System.out.println(h[i].getName() + " "+h[i].getValue());
		}
		
		HttpGet httpget = new HttpGet(locationURL);
		printCookieStore();		
		
		try {
			response = client.execute(httpget);
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Prints the response from the server
		//printResponse(response);

		//Need to remove the previous JSESSIONID's from the CookieStore
		cookieStore.removeCookie("JSESSIONID");
		
		printCookieStore();		
		System.out.println(response.getStatusLine());
		//Now add the update JESSIONID to the cookieStore;
		Header[] cks = response.getAllHeaders();
		Cookie c;
		for ( int j = 0; j < cks.length; j ++){
			
			if(cks[j].getName().equals("Set-Cookie")){
				
				idValCombo = cks[j].getValue().split("=", 2);
				
				c = new BasicClientCookie(idValCombo[0],idValCombo[1].substring(0,idValCombo[1].indexOf(";")));
				//System.out.println(c.getName()+c.getValue());
				cookieStore.addCookie(c);				
			}
			System.out.println(cks[j].getName() + " "+cks[j].getValue());
		}
		//Print all current cookies
		printCookieStore();
		return true;
	}

	public void printResponse(HttpResponse response){
		System.out.println(response.getStatusLine());
		Header[] h = response.getAllHeaders();
		for ( int i = 0; i < h.length; i ++){
			System.out.println(h[i].getName() + " "+h[i].getValue());
		}
	}
	
	public void printCookieStore()
	{
		System.out.println("---------------current cookie store---------------");
		Cookie print;
		List<Cookie> cs = cookieStore.getCookieStore();
		Iterator<Cookie> csIterator=cs.iterator();
		while (csIterator.hasNext()){
			print = csIterator.next();
			System.out.println(print.getName()+" "+print.getValue());
		}
		System.out.println("--------------------------------------------------");
	}

	public static void main (String args[]){
		RegisterClient rc = new RegisterClient();
		try {
			rc.login(args[0],args[1]);
			
			//wc.login();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
