package jdbc_conn;

import java.io.IOException;
import java.sql.*;

public class Check_conn {
	public static void main(String args[])throws IOException
	{
		try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection conn=DriverManager.getConnection("jdbc:mysql://localhost:3306/test","root","");	
			try{
				Statement st=conn.createStatement();
				ResultSet rs = st.executeQuery("select * from emp");
				while(rs.next())
				{
					System.out.println(rs.getString(1));

				}
			}
			catch(SQLException s)
			{
				System.out.println("sql error");
			}
		}
	catch(Exception e)
	{	
	System.out.println("Connection error");
	}	
	}
}
