package jdbc_conn;

import java.io.IOException;
import java.sql.*;

public class TestQAA {
	public static void main(String args[])throws IOException
	{
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String str3 = "select * from emp3";
			Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/test","root","");	
			Statement st=conn.createStatement();
			st.executeQuery("select * from emp1");
			String str4 = "select * from emp4";
			st.executeQuery("select * from emp2");
			int x=2;
			if(x==1)
			st.executeQuery(str3);
			else
			st.executeQuery(str4);
			
		}
		catch(Exception e)
		{	
			System.out.println("Connection error");
		}	
	}
}
