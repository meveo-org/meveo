package org.meveo.service.storage;

import org.junit.Test;
import org.meveo.model.storage.Repository;

/**
 * @author Edward P. Legaspi
 */
public class RepositoryPathTest {

	@Test
	public void testPath() {
		Repository path1 = new Repository();
		path1.setId(1L);

		Repository path2 = new Repository();
		path2.setId(2L);
		path2.setParentRepository(path1);

		Repository path3 = new Repository();
		path3.setId(3L);
		path3.setParentRepository(path2);

		RepositoryService rs = new RepositoryService();
		String path = rs.computePath(path3);

		System.out.println(path);
	}
}
