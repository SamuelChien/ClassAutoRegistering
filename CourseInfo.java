import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class CourseInfo {
	private Scanner newInput;
	private String department;
	private String courseNumber;
	private String init_Course_URL;
	private String specific_Section_URL;
	private String line_contains_prof;
	private String line_contains_section;
	private URL website;
	private URLConnection conn;
	private BufferedReader in;
	private ArrayList<String> courseDataEntries;
	
	public CourseInfo() throws Exception {
		newInput              = new Scanner(System.in);
		department            = newInput.next();
		courseNumber          = newInput.next();
		website               = new URL(init_Course_URL);
		conn                  = website.openConnection();
		in                    = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		courseDataEntries     = new ArrayList<String>();
		init_Course_URL       = "https://courses.students.ubc.ca/cs/main?pname=subjarea&tname=subjareas&req=3&dept="
		+ department + "&course=" + courseNumber;
		line_contains_prof    = "<td nowrap><a href=\"/cs/main;";
		line_contains_section = "onmouseover=\"cancelHide=1; popup(); setColor(new Array";
	}
	
	// Input: Department + Course Number
	// Effect: Store Strings of "Department+Course+Section+Professor" to the ArrayList courseDataEntries
	public void doSomeMagic() throws Exception {
		
		// get section
		String whole_html = html_to_str(init_Course_URL, website, conn, in);
		while (ind_data_value(whole_html, line_contains_section) != 0) {
			String sec = getInfo(whole_html, line_contains_section);
			sec        = deleteSpaces(sec);
			// get prof
			specific_Section_URL = getSpecSectionURL(sec, init_Course_URL, department,
													 courseNumber);
			String prof = "";
			
			if (specific_Section_URL != "Lab") {
				String whole_html2 = html_to_str(specific_Section_URL, website, conn, in);
				prof = " " + getInfo(whole_html2, line_contains_prof);
			}
			
			String entry = sec + prof;
			courseDataEntries.add(entry);
			whole_html = whole_html.substring(
											  ind_data_value(whole_html, line_contains_section) + 1,
											  whole_html.length());
		}
		// System.out.println(sec);
		// System.out.println(prof);
		for (String entry : courseDataEntries) {
			System.out.println(entry);
		}
		in.close();
	}
	
	// Getters
	public Scanner get_newInput() {
		return newInput;
	}
	
	public String get_department() {
		return department;
	}
	
	public String get_courseNumb() {
		return courseNumber;
	}
	
	public String get_registerPage() {
		return specific_Section_URL;
	}
	
	public String get_line_with_prof() {
		return line_contains_prof;
	}
	
	public String get_line_with_section() {
		return line_contains_section;
	}
	
	public URL get_website() {
		return website;
	}
	
	public URLConnection get_conn() {
		return conn;
	}
	
	public BufferedReader get_BufferedReader() {
		return in;
	}
	
	public ArrayList<String> get_courseData() {
		return courseDataEntries;
	}
	
	// For grabbing "course section" or "professor name"
	private String getInfo(String whole_html, String target) {
		String info = "";
		int index = ind_data_value(whole_html, target);
		if (index == 0) {
			return "";
			// System.out.println("Wanted Info is not found on the page.");
			// System.exit(0);
		}
		StringBuffer name_buffer = new StringBuffer();
		boolean start_of_value = false;
		boolean parse_now = false;
		for (int i = index; i < whole_html.length(); i++) {
			// finds the char right before the name
			if (whole_html.charAt(i) == '>')
				start_of_value = true;
			if (start_of_value && !(whole_html.charAt(i) == '<') && parse_now) {
				name_buffer.append(whole_html.charAt(i));
				if (whole_html.charAt(i + 1) == '<') {
					info = name_buffer.toString();
					break;
				}
			}
			if (start_of_value && !parse_now)
				parse_now = true;
		}
		return info;
	}
	
	// Find the index where professor name or section number starts in the HTML string 
	private int ind_data_value(String s1, String s2) {
		int len1 = s1.length();
		int len2 = s2.length();
		int j = 0;
		for (int i = len2; i <= len1; i++) {
			if (s1.substring(j, i).equals(s2)) {
				// System.out.println(s1);
				return i;
			}
			j++;
		}
		return 0;
	}
	
	// Separates section number from "Course Number Section" and append it to
	// the main URL to goto the registering page.
	private String getSpecSectionURL(String s1, String currentURL, String dep,
									 String courseNumb) {
		int space = 0;
		String sec = "";
		for (int i = 0; i < s1.length(); i++) {
			if (s1.charAt(i) == ' ' && space < 2) {
				space++;
			} else if (space >= 2)
				sec += s1.charAt(i);
		}
		// System.out.println(sec);
		if (isSectionNumber(sec))
			return "https://courses.students.ubc.ca/cs/main?pname=subjarea&tname=subjareas&req=5&dept="
			+ dep + "&course=" + courseNumb + "&section=" + sec;
		else
			return "Lab";
		// return
		// "https://courses.students.ubc.ca/cs/main?pname=subjarea&tname=subjareas&req=5&dept=CPSC&course=310&section=911";//+sec;
		
	}
	
	// Check whether the three digit number is Section number or Lab number
	private boolean isSectionNumber(String sec) {
		for (int i = 0; i < sec.length(); i++) {
			if (!Character.isDigit(sec.charAt(i)))
				return false;
		}
		return true;
	}
	// Return a string that contains the whole html of a given URL
	private String html_to_str(String url, URL website, URLConnection conn, BufferedReader in) throws Exception {
		String whole_html = "";
		website = new URL(url);
		conn    = website.openConnection();
		in      = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			whole_html += inputLine;
			// System.out.println(inputLine);
		}
		return whole_html;
	}
	
	// Cleans up the String by deleting unnecessary space
	private String deleteSpaces(String s1) {
		int start  = 0;
		int end    = s1.length();
		boolean sf = false;
		
		for (int i = 0; i < s1.length(); i++) {
			if (Character.isLetter(s1.charAt(i)) && !sf) {
				sf    = true;
				start = i;
			} 
			else if (s1.charAt(i) == ' ' && s1.charAt(i + 1) == ' ' && sf) {
				end = i;
				break;
			}
		}
		return s1.substring(start, end);
	}
	
}
