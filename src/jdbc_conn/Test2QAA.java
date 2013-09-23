package jdbc_conn;

import java.io.IOException;
import java.sql.*;

public class Test2QAA {
	public static void main(String args[])throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException
	{
		//try{
			int x,y,z,a,b,c;
			String str3 = "select * from emp3";
			String str4 = "select * from emp4";
			x=5;a=0;
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/test","root","");	
			Statement st=conn.createStatement();
			st.executeQuery("select * from emp1");
			while(a<x) {
				a = a+1;
			}
			st.executeQuery("select * from emp2");
			if(x==1)
			st.executeQuery(str3);
			else
			st.executeQuery(str4);
		//}
		//catch(Exception e)
		//{	
		//	System.out.println("Connection error");
		//}	
	}
}
