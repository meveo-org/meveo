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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link MedinaCache}.
 * 
 * @author Ignas Lelys
 * @created Apr 7, 2009
 * 
 */
public class MedinaCacheTest {
    
    @Test(groups = { "unit" })
    public void testRemoveIndexFromHashCodesTable() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        
        final Method removeIndexFromHashCodesTable = MedinaCache.class.getDeclaredMethod("removeIndexFromHashCodesTable", int.class);
        removeIndexFromHashCodesTable.setAccessible(true);
        
        final Field indexesByHashCodeTable = MedinaCache.class.getDeclaredField("indexesByHashCodeTable");
        indexesByHashCodeTable.setAccessible(true);
        
        final Field hashTable = MedinaCache.class.getDeclaredField("hashTable");
        hashTable.setAccessible(true);
        
        MedinaCache cache = new MedinaCache(5);
        
        byte[] hashTableValues = new byte[]{121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, 121, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        hashTable.set(cache, hashTableValues);
        // hashcodes for those values. changing hashcode algorithm might fail this test.
        Object[] indexesByHashCodeTableValues = new Object[] {null, null, new int[]{1, -1, -1}, new int[]{0, 2, -1}, null, null};
        indexesByHashCodeTable.set(cache, indexesByHashCodeTableValues);
        
        removeIndexFromHashCodesTable.invoke(cache, 0);
        
        assert (Arrays.deepEquals((Object[])indexesByHashCodeTable.get(cache), new Object[] {null, null, new int[]{1, -1, -1}, new int[]{2, -1, -1}, null, null}));
        
        removeIndexFromHashCodesTable.invoke(cache, 2);
        
        assert (Arrays.deepEquals((Object[])indexesByHashCodeTable.get(cache), new Object[] {null, null, new int[]{1, -1, -1}, null, null, null}));
        
    }
    
    @Test(groups = { "unit" })
    public void testInsert() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        final Method insert = MedinaCache.class.getDeclaredMethod("insert", byte[].class);
        insert.setAccessible(true);
        
        final Field currentHashTableIndex = MedinaCache.class.getDeclaredField("currentHashTableIndex");
        currentHashTableIndex.setAccessible(true);
        
        MedinaCache cache = new MedinaCache(3);
        
        byte[] value1 = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        byte[] value2 = { -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                -128 };
        byte[] value3 = { 2, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                2 };
        byte[] value4 = { 3, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                3 };
        
        final Method getValue = MedinaCache.class.getDeclaredMethod("getValue", int.class);
        getValue.setAccessible(true);
        
        insert.invoke(cache, value1);
        byte[] returnedValue1 = (byte[]) getValue.invoke(cache, new Integer(0));
        Assert.assertEquals(returnedValue1, value1);
        Assert.assertEquals(currentHashTableIndex.getInt(cache), 1);

        insert.invoke(cache, value2);
        byte[] returnedValue2 = (byte[]) getValue.invoke(cache, new Integer(1));
        Assert.assertEquals(returnedValue2, value2);
        Assert.assertEquals(currentHashTableIndex.getInt(cache), 2);
        
        insert.invoke(cache, value3);
        byte[] returnedValue3 = (byte[]) getValue.invoke(cache, new Integer(2));
        Assert.assertEquals(returnedValue3, value3);
        Assert.assertEquals(currentHashTableIndex.getInt(cache), 0);

        insert.invoke(cache, value4);
        byte[] returnedValue4 = (byte[]) getValue.invoke(cache, new Integer(0));
        Assert.assertEquals(returnedValue4, value4);
        Assert.assertEquals(currentHashTableIndex.getInt(cache), 1);
        
    }

    @Test(groups = { "unit" })
    public void testResizeBucket() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        final Method resizeBucket = MedinaCache.class.getDeclaredMethod("resizeBucket", int[].class, int.class);
        resizeBucket.setAccessible(true);
        int[] smallerArray = { 0, 1, 2, 3 };
        int[] resizedArray = (int[]) resizeBucket.invoke(new MedinaCache(50), smallerArray, new Integer(2));
        int[] expectedArray = { 0, 1, 2, 3, -1, -1 };
        Assert.assertTrue(Arrays.equals(resizedArray, expectedArray));
    }

    @Test(groups = { "unit" })
    public void testGetValue() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        final Method getValue = MedinaCache.class.getDeclaredMethod("getValue", int.class);
        getValue.setAccessible(true);
        MedinaCache cache = new MedinaCache(50);
        byte[] value1 = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        byte[] value2 = { -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                -128 };
        cache.add(value1);
        cache.add(value2);
        byte[] returnedValue1 = (byte[]) getValue.invoke(cache, new Integer(0));
        byte[] returnedValue2 = (byte[]) getValue.invoke(cache, new Integer(1));
        Assert.assertEquals(value1, returnedValue1);
        Assert.assertEquals(value2, returnedValue2);
    }
    
    @Test(groups = { "unit" })
    public void testShrinkBucketIfNeeded() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final Method shrinkBucketIfNeeded = MedinaCache.class.getDeclaredMethod("shrinkBucketIfNeeded", int[].class);
        shrinkBucketIfNeeded.setAccessible(true);
        int[] bucket = {1, 2, -1, -1, -1};
        int[] shrinkedBucket = (int[]) shrinkBucketIfNeeded.invoke(new MedinaCache(5), bucket);
        int[] expectedBucket = {1, 2, -1};
        assert (Arrays.equals(shrinkedBucket, expectedBucket));
        
        int[] emptyBucket = {-1, -1, -1};
        int[] deletedBucket = (int[]) shrinkBucketIfNeeded.invoke(new MedinaCache(5), emptyBucket);
        assert (deletedBucket == null);
        
        int[] normalBucket = {1, 2, 3, -1, -1};
        int[] notChangedBucket = (int[]) shrinkBucketIfNeeded.invoke(new MedinaCache(5), normalBucket);
        assert (Arrays.equals(normalBucket, notChangedBucket));
    }

    @Test(groups = { "unit" })
    public void testSetValue() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final Method setValue = MedinaCache.class.getDeclaredMethod("setValue", int.class, byte[].class);
        setValue.setAccessible(true);
        MedinaCache cache = new MedinaCache(50);
        byte[] value1 = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        byte[] value2 = { -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                -128 };
        setValue.invoke(cache, new Integer(0), value1);
        setValue.invoke(cache, new Integer(1), value2);
        
        final Method getValue = MedinaCache.class.getDeclaredMethod("getValue", int.class);
        getValue.setAccessible(true);
        byte[] returnedValue1 = (byte[]) getValue.invoke(cache, new Integer(0));
        byte[] returnedValue2 = (byte[]) getValue.invoke(cache, new Integer(1));
        Assert.assertEquals(returnedValue1, value1);
        Assert.assertEquals(returnedValue2, value2);
    }

    @Test(groups = { "unit" })
    public void testEmtySpaceIndex() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        final Method emptySpaceIndex = MedinaCache.class.getDeclaredMethod("emtySpaceIndex", int[].class);
        emptySpaceIndex.setAccessible(true);
        int[] sampleBucket = { 0, 1, 2, 3, -1, -1 };
        int emptySpace = (Integer) emptySpaceIndex.invoke(new MedinaCache(50), sampleBucket);
        Assert.assertEquals(emptySpace, 4);
        int[] fullBucket = { 0, 1, 2, 3 };
        int emptySpaceNotFound = (Integer) emptySpaceIndex.invoke(new MedinaCache(50), fullBucket);
        Assert.assertEquals(emptySpaceNotFound, -1);
    }
    
    @Test(groups = { "unit" })
    public void testLastElementIndex() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        final Method lastElementIndex = MedinaCache.class.getDeclaredMethod("lastElementIndex", int[].class);
        lastElementIndex.setAccessible(true);
        int[] sampleBucket = { 0, 1, 2, -1, -1, -1 };
        int emptySpace = (Integer) lastElementIndex.invoke(new MedinaCache(50), sampleBucket);
        Assert.assertEquals(emptySpace, 2);
        int[] fullBucket = { 0, 1, 2, 3 };
        int emptySpaceNotFound = (Integer) lastElementIndex.invoke(new MedinaCache(50), fullBucket);
        Assert.assertEquals(emptySpaceNotFound, 3);
    }

    @Test(groups = { "unit" })
    public void testSize() {
        int size = 5;
        MedinaCache cache = new MedinaCache(size);
        byte[] value1 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        byte[] value2 = { -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                -128 };
        cache.add(value1);
        cache.add(value2);
        Assert.assertEquals(cache.size(), 2);
        byte[] value3 = { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        byte[] value4 = { -1, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128 };
        byte[] value5 = { 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        byte[] value6 = { -2, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128 };
        cache.add(value3);
        cache.add(value4);
        cache.add(value5);
        cache.add(value6);
        Assert.assertEquals(cache.size(), 5);
    }

    @Test(groups = { "unit" })
    public void testContains() {
        int size = 5;
        MedinaCache cache = new MedinaCache(size);
        byte[] value1 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        byte[] value2 = { -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                -128 };
        cache.add(value2);
        Assert.assertFalse(cache.contains(value1));
        Assert.assertTrue(cache.contains(value2));
    }

    @Test(groups = { "unit" })
    public void testClear() {
        int size = 5;
        MedinaCache cache = new MedinaCache(size);
        byte[] value1 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        byte[] value2 = { -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                -128 };
        cache.add(value1);
        cache.add(value2);
        cache.clear();
        Assert.assertEquals(cache.size(), 0);
        Assert.assertEquals(cache.toArray().length, 0);
        Assert.assertFalse(cache.contains(value1));
        Assert.assertFalse(cache.contains(value2));
    }

    @Test(groups = { "unit" })
    public void testToArray() {
        int size = 5;
        MedinaCache cache = new MedinaCache(size);
        byte[] value1 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        cache.add(value1);
        Assert.assertEquals((byte[]) cache.toArray()[0], value1);
        Assert.assertEquals(cache.toArray().length, 1);
    }

    /**
     * Fill cache and then test its behavior after it starts adding elements
     * from start again.
     */
    @Test(groups = { "unit" })
    public void testPutUntilFilled() {
        byte[] firstHash = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        byte[] firstHashAfterRestart = { -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128, -128,
                -128, -128, -128 };
        int size = 50;
        MedinaCache cache = new MedinaCache(size);
        cache.add(firstHash);
        Random random = new Random();
        while (cache.size() < size) {
            byte[] randomBytes = new byte[16];
            random.nextBytes(randomBytes);
            // make sure random bytes not equals to
            // bytes we want to insert right after restart.
            if (!Arrays.equals(randomBytes, firstHashAfterRestart) && !Arrays.equals(randomBytes, firstHash))
                cache.add(randomBytes);
        }
        Assert.assertTrue(cache.contains(firstHash));
        cache.add(firstHashAfterRestart);
        // make sure next firstHashAfterRestart value replaced firstHash
        Assert.assertTrue(cache.contains(firstHashAfterRestart));
        // make sure firstHash is not in the cache anymore
        Assert.assertFalse(cache.contains(firstHash));
        // make sure we can add first hash again
        Assert.assertTrue(cache.add(firstHash));
    }

    @Test(groups = { "unit" })
    public void testPutSame() {
        MedinaCache cache = new MedinaCache(50);
        String hashAsString = "112233445566778899aabbccddeefff0";
        Assert.assertTrue(cache.add(hashAsString));
        Assert.assertFalse(cache.add(hashAsString));
        byte[] hashAsArray = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
        Assert.assertTrue(cache.add(hashAsArray));
        Assert.assertFalse(cache.add(hashAsArray));
        Assert.assertEquals(cache.size(), 2);
    }

}
