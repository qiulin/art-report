/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */

package art.dbutils;

import java.sql.Connection;
import java.sql.SQLException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * 
 * @author Timothy Anyona
 */
public class DbServiceTest {
	
	public DbServiceTest() {
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testNullConnectionToUpdate() throws SQLException{
		DbService dbService=new DbService();
		Connection conn=null;
		String sql=null;
		dbService.update(conn,sql);
	}

}
