/*
 * This file is part of NixNote 
 * Copyright 2009 Randy Baumgarte
 * 
 * This file may be licensed under the terms of of the
 * GNU General Public License Version 2 (the ``GPL'').
 *
 * Software distributed under the License is distributed
 * on an ``AS IS'' basis, WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the GPL for the specific language
 * governing rights and limitations.
 *
 * You should have received a copy of the GPL along with this
 * program. If not, go to http://www.gnu.org/licenses/gpl.html
 * or write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
*/

package cx.fbn.nevernote.sql.driver;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class NSqlQuery {
	
	private final Connection connection;
	private String lastError;
	private ResultSet resultSet;
	private PreparedStatement preparedStatement;
	private final HashMap<String, Integer>	positionMap;
	private ByteArrayInputStream fis;
	
	
	public NSqlQuery(Connection c) {
		connection = c;
		positionMap = new HashMap<String, Integer>();
	}
	
	
	public boolean next() {
		lastError = null;
		
		if (resultSet == null) {
			lastError = "Result set is null";
			return false;
		}
		try {
			return resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
			lastError = e.getMessage();
			return false;
		}
	}
	
	
	public boolean exec(String sql) {
		Statement st;
		boolean retVal = false;
		lastError = "";
		resultSet = null;
		try {
			st = connection.createStatement();
			retVal = st.execute(sql);
			resultSet = st.getResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
			lastError = e.getMessage();
		}
		return retVal;
	}
	
	public boolean exec() {
		lastError = "";
		resultSet = null;
		
		if (preparedStatement == null) {
			lastError = "No SQL statement prepared";
			return false;
		}

		try {
			preparedStatement.execute();
			resultSet = preparedStatement.getResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
			lastError = e.getMessage();
			return false;
		}
		
		
		return true;
	}
	
	
	public String lastError() {
		if (lastError == null)
			return "";
		return lastError;
	}
	
	
	public Object valueObject(int position) {
		lastError = null;
		if (resultSet == null) {
			lastError = "ResultSet is null";
			return null;
		}
		
		try {
			return resultSet.getObject(position+1);
		} catch (SQLException e) {
			e.printStackTrace();
			lastError = e.getMessage();
			return null;
		}
	}
	
	
	public String valueString(int position) {
		lastError = null;
		if (resultSet == null) {
			lastError = "ResultSet is null";
			return null;
		}
		
		try {
			return resultSet.getString(position+1);
		} catch (SQLException e) {
			e.printStackTrace();
			lastError = e.getMessage();
			return null;
		}
	}
	
	public  boolean valueBoolean(int position, boolean unknown) {
		try {
			return resultSet.getBoolean(position+1);
		} catch (SQLException e) {
			e.printStackTrace();
			return unknown;
		}
	}

	public  long valueLong(int position) {
		try {
			return resultSet.getLong(position+1);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public int valueInteger(int position) {
		try {
			return resultSet.getInt(position+1);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public void bindValue(String field, String value) {
		Integer position = positionMap.get(field.toLowerCase());
		lastError = null;
		if (preparedStatement == null) {
			lastError = "No prepared statement exists";
			return;
		}
		if (position != null && position > 0) {
			try {
				preparedStatement.setString(position, value);
			} catch (SQLException e) {
				e.printStackTrace();
				lastError = e.getMessage();
			}
			return;
		}	
	}
	
	public void bindValue(String field, boolean value) {
		Integer position = positionMap.get(field.toLowerCase());
		lastError = null;
		if (preparedStatement == null) {
			lastError = "No prepared statement exists";
			return;
		}
		if (position > 0) {
			try {
				preparedStatement.setBoolean(position, value);
			} catch (SQLException e) {
				e.printStackTrace();
				lastError = e.getMessage();
			}
			return;
		}	
	}
	
	

	public void bindValue(String field, int value) {
		Integer position = positionMap.get(field.toLowerCase());
		lastError = null;
		if (preparedStatement == null) {
			lastError = "No prepared statement exists";
			return;
		}
		if (position > 0) {
			try {
				preparedStatement.setInt(position, value);
			} catch (SQLException e) {
				e.printStackTrace();
				lastError = e.getMessage();
			}
			return;
		}	
	}
	
	

	public void bindValue(String field, double value) {
		Integer position = positionMap.get(field.toLowerCase());
		lastError = null;
		if (preparedStatement == null) {
			lastError = "No prepared statement exists";
			return;
		}
		if (position > 0) {
			try {
				preparedStatement.setDouble(position, value);
			} catch (SQLException e) {
				e.printStackTrace();
				lastError = e.getMessage();
			}
			return;
		}	
	}
	
	
	
	public void bindValue(String field, byte[] value) {
		Integer position = positionMap.get(field.toLowerCase());
		lastError = null;
		if (preparedStatement == null) {
			lastError = "No prepared statement exists";
			return;
		}
		if (position > 0) {
			try {
				preparedStatement.setBytes(position, value);
			} catch (SQLException e) {
				e.printStackTrace();
				lastError = e.getMessage();
			}
			return;
		}	
	}
	
	
	
	public boolean prepare(String statement) {
		positionMap.clear();
		preparedStatement = null;
		lastError = null;
		
		int position = 1;
		for (int i=statement.indexOf(":"); i>0; i=statement.indexOf(":",i)) {
			int endField = statement.indexOf(" ",i+1);
			int nextComma = statement.indexOf(",",i+1);
			int nextBracket = statement.indexOf(")",i+1);
			
			if (nextComma > 0 && nextComma < endField)
				endField = nextComma;
			if (endField == -1)
				endField = nextBracket;
			if (nextBracket > 0 && nextBracket < endField)
				endField = nextBracket;
			
			String fieldName = null;
			if (endField > 0) {
				fieldName = statement.substring(i,endField);
			}
			else {
				fieldName = statement.substring(i);
				endField = statement.length();
			}
			statement = statement.substring(0,i)+"?" +statement.substring(endField);
			positionMap.put(fieldName.toLowerCase(), new Integer(position));
			position++;
		}
		
		
		try {
			preparedStatement = connection.prepareStatement(statement);
		} catch (SQLException e) {
			e.printStackTrace();
			lastError = e.getMessage();
			return false;
		}
		
		
		return true;
	}
	
	
	public void bindBlob(String field, byte[] value) {
		Integer position = positionMap.get(field.toLowerCase());
		lastError = null;
		if (preparedStatement == null) {
			lastError = "No prepared statement exists";
			return;
		}
		if (position != null && position > 0) {
			try {
				fis = new ByteArrayInputStream(value);
				preparedStatement.setBinaryStream(position, fis);
			} catch (SQLException e) {
				e.printStackTrace();
				lastError = e.getMessage();
			}
			return;
		}	
	}
	
	
	public byte[] getBlob(int position) {
		Blob dataBinary;
		try {
			dataBinary = resultSet.getBlob(position+1);
			byte[] b;
			if (dataBinary == null)
				return null;
			b = dataBinary.getBytes(1, (int) dataBinary.length());
			return b;
		} catch (SQLException e) {
			e.printStackTrace();
			lastError = e.getMessage();
		}
		return null;
	}
	
	

}
