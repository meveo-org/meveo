/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.manaty.telecom.mediation.cache;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import org.manaty.model.mediation.CurrentCell;
import org.manaty.model.resource.telecom.AccessPoint;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Tests transactional cache.
 * 
 * @author Ignas
 *
 */
public class TransactionalCellCacheTest {
    
    /**
     * Tests addCell, getCell and finalize transaction with multiple threads.
     * After all 3 threads start same time and adding same cells with different date,
     * all gets should return all cells with latest date. When transaction with latest cells is finalized,
     * all threads should return second latest cells.
     */
    @Test(groups = { "db" })
    public void testMultithreaded() throws InterruptedException {
        
        final CountDownLatch startGate = new CountDownLatch(1);
        final CountDownLatch endGate = new CountDownLatch(3);
        
        Thread thread1 = new Thread() {
            public void run() {
                try {
                    startGate.await();
                    Calendar c1 = Calendar.getInstance();
                    c1.set(2011, Calendar.JANUARY, 1, 0, 0, 0);
                    c1.set(Calendar.MILLISECOND, 0);
                    TransactionalCellCache.getInstance().beginTransaction();
                    for (long i = 1; i < 8; i++) {
                        TransactionalCellCache.getInstance().addCellToCache(createCurrentCell(i, "cell1", c1.getTime()));
                    }
                    TransactionalCellCache.getInstance().commitTransaction();
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                } finally {
                    endGate.countDown();
                }
            }
        };
        thread1.start();
        Thread thread2 = new Thread() {
            public void run() {
                try {
                    startGate.await();
                    Calendar c2 = Calendar.getInstance();
                    c2.set(2011, Calendar.JANUARY, 1, 1, 0, 0);
                    c2.set(Calendar.MILLISECOND, 0);
                    TransactionalCellCache.getInstance().beginTransaction();
                    for (long i = 1; i < 8; i++) {
                        TransactionalCellCache.getInstance().addCellToCache(createCurrentCell(i, "cell2", c2.getTime()));
                    }
                    TransactionalCellCache.getInstance().commitTransaction();
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                } finally {
                    endGate.countDown();
                }
            }
        };
        thread2.start();
        Thread thread3 = new Thread() {
            public void run() {
                try {
                    startGate.await();
                    Calendar c3 = Calendar.getInstance();
                    c3.set(2011, Calendar.JANUARY, 1, 2, 0, 0);
                    c3.set(Calendar.MILLISECOND, 0);
                    TransactionalCellCache.getInstance().beginTransaction();
                    for (long i = 1; i < 8; i++) {
                        TransactionalCellCache.getInstance().addCellToCache(createCurrentCell(i, "cell3", c3.getTime()));
                    }
                    TransactionalCellCache.getInstance().commitTransaction();
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                }  finally {
                    endGate.countDown();
                }
            }
        };
        thread3.start();
        
        startGate.countDown();
        endGate.await(); // wait until all finishes
        
        for (long i = 1; i < 8; i++) {
            Assert.assertNotNull(TransactionalCellCache.getInstance().getCellDataByPA(i));
            Assert.assertEquals(TransactionalCellCache.getInstance().getCellDataByPA(i).cellId, "cell3");
        }
        
    }
    
    private CurrentCell createCurrentCell(Long paId, String cellId, Date date) {
        CurrentCell cell = new CurrentCell();
        AccessPoint pa = new AccessPoint();
        pa.setId(paId);
        cell.setAccessPoint(pa);
        cell.setCellId(cellId);
        cell.setCellChangeDate(date);
        cell.setPartyId(1L);
        return cell;
    }

}
