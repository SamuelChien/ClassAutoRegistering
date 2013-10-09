package register;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.http.cookie.Cookie;

public class ClientCookieStore {

	List<Cookie> cookieStore;

	public ClientCookieStore()
	{

		cookieStore = new ArrayList<Cookie>();

	}

	public List<Cookie> getCookieStore()
	{
		return cookieStore;
	}
	public void addCookie(Cookie cookie)
	{
		cookieStore.add(cookie);
	}
	public void removeCookie(String cookie)
	{
		ListIterator<Cookie> clist = cookieStore.listIterator();
		while(clist.hasNext()){
			if(clist.next().getName().contains(cookie)){
				clist.remove();
			}
		}		
	}

}
