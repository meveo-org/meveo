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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.manaty.utils.MagicNumberConverter;

/**
 * Class for caching in Medina. This cache is much more memory efficient
 * as other {@link Set} implementations. It keeps byte arrays (by default
 * size is 16 bytes) as values. Method add() also allows to put strings
 * to cache. Those strings must represent encoded hash value so they 
 * can contain only characters [a-f0-9].
 * 
 * @author Ignas Lelys
 * @created Apr 3, 2009
 * 
 * P.S maybe to rename this class to something like MemoryEfficientFixedSizeSet? :)
 */
@SuppressWarnings("rawtypes")
public class MedinaCache implements Set {

    private static final int BUCKET_SIZE = 3;

    private static final int BUCKET_RESIZE_BY = 2;

    /**
     * Prime numbers used for hashcode calculation.
     */
    private static int[] PRIMES = { 244217, 244219, 244243, 244247, 244253, 244261, 244291, 244297, 244301, 244303,
            244313, 244333, 244339, 244351, 244357, 244367 };

    /**
     * Number of elements in hashTable. 
     * Table size is number of elements multiplied with length of one element.
     * 
     */
    private int numberOfElementsInTable;

    /**
     * Is true when hashTable is full, and is filling hash values from start
     */
    private boolean isFilled = false;
    
    /**
     * Length of one element.
     */
    private int valueSize;
    
    /**
     * Index where next value will be put in hash table. After reaching end of
     * hash table it restarts to 0.
     */
    private int currentHashTableIndex;

    /**
     * Hash table where all hash values(byte arrays) are kept.
     */
    private byte[] hashTable;

    /**
     * Hash codes table, where each index represents hash code. Each field saves
     * bucket of indexes (array of integers), those are indexes of {@link hashTable}.
     * So in this table there are pointers where values with provided hash code
     * are kept in hash table.
     */
    private Object[] indexesByHashCodeTable;

    /**
     * Constructor. Because no length of value is provided it assumes that length is 16.
     * @param size Maximum number of elements in cache.
     */
    public MedinaCache(int size) {
        this(size, 16);
    }
    
    /**
     * Constructor.
     * 
     * @param size Maximum number of elements in cache.
     * @param valueSize Value length.
     */
    public MedinaCache(int size, int valueSize) {
        super();
        this.valueSize = valueSize;
        this.numberOfElementsInTable = size;
        indexesByHashCodeTable = new Object[numberOfElementsInTable];
        int tableSize = size*valueSize;
        hashTable = new byte[tableSize];
    }
    
    /**
     * Put value to cache. Value must be represented as string or byte array.
     * 
     * @param hashValue
     *            Value of the hash which has to be put in cache.
     * @return True if hash value was put successfully and false if such value
     *         already was in cache.
     */
    public boolean add(Object o) {
        byte[] hashValue;
        if (o instanceof byte[]) {
            hashValue = (byte[])o;
        } else if (o instanceof String) {
            hashValue = MagicNumberConverter.convertToArray((String)o);
        } else {
            throw new IllegalArgumentException();
        }
        if (contains(hashValue)) {
            return false;
        }
        insert(hashValue);
        return true;
    }

    /**
     * Finds out if there is same hash value in hash table.
     * 
     * @param hashValue
     *            Hash value which we compare with other values with same
     *            hashCode.
     * @return true if same hash value in hash table is found and false
     *         otherwise.
     */
    public boolean contains(Object o) {
        byte[] hashValue = (byte[])o;
        int hashCode = hashCode(hashValue);
        if (indexesByHashCodeTable[hashCode] != null) {
            int[] indexesBucket = (int[]) indexesByHashCodeTable[hashCode];
            int i = 0;
            while (i < indexesBucket.length && indexesBucket[i] != -1) {
                if (Arrays.equals(hashValue, getValue(indexesBucket[i]))) {
                    return true;
                }
                i++;
                if (i >= indexesBucket.length) {
                    indexesBucket = resizeBucket(indexesBucket, BUCKET_RESIZE_BY);
                    indexesByHashCodeTable[hashCode] = indexesBucket;
                }
            }
        }
        return false;
    }
    
    /**
     * Clears cache.
     */
    public void clear() {
        isFilled = false;
        Arrays.fill(indexesByHashCodeTable, null);
        currentHashTableIndex = 0;
    }
    
    /**
     * Return array of hashes which are kept in cache.
     * 
     * @return Array of hashes represented as String.
     */
    public Object[] toArray() {
        int numberOfElements = isFilled ? numberOfElementsInTable : currentHashTableIndex;
        Object[] hashes = new Object[numberOfElements];
        for (int i = 0; i < numberOfElements; i++) {
            byte[] hashValue = getValue(i);
            if (hashValue != null) {
                hashes[i] = hashValue;
            }
        }
        return hashes;
    }
    
    /**
     * Returns the number of elements in this set.
     * 
     * @return Number of elements.
     */
    public int size() {
        return isFilled ? numberOfElementsInTable : currentHashTableIndex;
    }
    
    /**
     * @see java.util.Set#addAll(java.util.Collection)
     */
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Set#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Set#isEmpty()
     */
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Set#iterator()
     */
    public Iterator<byte[]> iterator() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Set#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * @see java.util.Set#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Set#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.util.Set#toArray(T[])
     */
    public Object[] toArray(Object[] a) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Gets value by index from hashTable.
     * 
     * @param index Value index in hashTable.
     * @return Value.
     */
    private byte[] getValue(int index) {
        byte[] value = new byte[valueSize];
        System.arraycopy(hashTable, index*valueSize, value, 0, valueSize);
        return value;
    }
    
    /**
     * Sets value by index to hashTable.
     * 
     * @param index Index where value should be put.
     * @param value Value to set.
     */
    private void setValue(int index, byte[] value) {
        System.arraycopy(value, 0, hashTable, index*valueSize, valueSize);
    }
    

    /**
     * Inserts hash value in {@link hashTable}. It also inserts index of hash
     * table to {@link hashCodesTable}, so the value could be easily found by
     * its hash code. Of course removes old index from {@link hashCodesTable}
     * before inserting new. ({@link hashCodesTable} will always have index i
     * 
     * @param hashValue
     *            Value to insert.
     */
    private void insert(byte[] hashValue) {

        if (isFilled)
            removeIndexFromHashCodesTable(currentHashTableIndex);

        int hashCode = hashCode(hashValue);
        setValue(currentHashTableIndex, hashValue);

        if (indexesByHashCodeTable[hashCode] == null) {
            int[] newIndexesBucket = new int[BUCKET_SIZE];
            Arrays.fill(newIndexesBucket, -1);
            indexesByHashCodeTable[hashCode] = newIndexesBucket;
            newIndexesBucket[0] = currentHashTableIndex;
        } else {
            int[] indexesBucket = (int[]) indexesByHashCodeTable[hashCode];
            int index = emtySpaceIndex(indexesBucket);
            // if bucket is full resize it
            if (index == -1) {
                indexesBucket = resizeBucket(indexesBucket, BUCKET_RESIZE_BY);
                indexesByHashCodeTable[hashCode] = indexesBucket;
                index = emtySpaceIndex(indexesBucket);
            }
            indexesBucket[index] = currentHashTableIndex;
        }
        currentHashTableIndex++;
        if (currentHashTableIndex >= numberOfElementsInTable) {
            currentHashTableIndex = 0;
            isFilled = true;
        }
    }

    /**
     * Removes index from indexesByHashCodeTable. Finds value in hash table,
     * calculates its hash code, finds index in bucket
     * indexesByHashCodeTable[hashcode] and removes that index.
     * 
     * @param index
     *            Index to remove.
     */
    private void removeIndexFromHashCodesTable(int index) {
        int hashCode = hashCode(getValue(index));
        int[] indexesBucket = (int[]) indexesByHashCodeTable[hashCode];
        if (indexesBucket != null) {
            int i = 0;
            while ((i < indexesBucket.length) && (indexesBucket[i] != -1 && indexesBucket[i] != index)) {
                i++;
            }
            if (i == indexesBucket.length || indexesBucket[i] == index) {
                int lastElementIndex = lastElementIndex(indexesBucket);
                // if bucket is full take last element
                if (lastElementIndex == -1) {
                    lastElementIndex = indexesBucket.length - 1;
                }
                indexesBucket[i] = indexesBucket[lastElementIndex];
                indexesBucket[lastElementIndex] = -1;
                indexesBucket = shrinkBucketIfNeeded(indexesBucket);
                indexesByHashCodeTable[hashCode] = indexesBucket;
            }
        }
    }

    /**
     * When removing index from {@link indexesByHashCodeTable} sometimes it is
     * needed to shrink the bucket because it was big, and a lot of indexes were
     * removed from it. This method do that to save memory. Also if bucket
     * becomes empty it is removed.
     * 
     * @param bucket
     *            Array of indexes.
     * @return Null if bucket is empty and should be removed, and smaller array
     *         with same values if bucket had to be shrinked. If nothing had to
     *         be done returns same bucket.
     */
    private int[] shrinkBucketIfNeeded(int[] bucket) {
        if (bucket.length > BUCKET_SIZE) {
            int freeSpace = 0;
            for (int i = 0; i < bucket.length; i++) {
                if (bucket[i] == -1) {
                    freeSpace++;
                }
            }
            if (freeSpace >= (BUCKET_RESIZE_BY + 1)) {
                return resizeBucket(bucket, -1 * BUCKET_RESIZE_BY);
            }
        } else {
            if (isBucketEmty(bucket)) {
                return null;
            }
        }
        return bucket;
    }

    /**
     * Checks is bucket is empty.
     * 
     * @param bucket
     *            Array to check.
     * @return True if bucket is empty and false if it has at least one value.
     */
    private boolean isBucketEmty(int[] bucket) {
        for (int i = 0; i < bucket.length; i++) {
            if (bucket[i] != -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Searches bucket for a empty space index in bucket.
     * 
     * @param bucket
     *            Bucket to check.
     * @return Index of the last element in indexes bucket. If bucket is full
     *         returns -1.
     */
    private int emtySpaceIndex(int[] bucket) {
        try {
            int index = 0;
            while (bucket[index] != -1)
                index++;
            return index;
        } catch (IndexOutOfBoundsException e) {
            return -1; // bucket is full
        }
    }

    /**
     * Searches last element index in bucket.
     * 
     * @param bucket
     *            Bucket to check.
     * @return Last element index.
     */
    private int lastElementIndex(int[] bucket) {
        int index = 0;
        while (index < bucket.length && bucket[index] != -1)
            index++;
        return index - 1;
    }

    /**
     * Resize bucket of indexes.
     * 
     * @param bucket
     *            Full bucket that need resize.
     * @return Bucket bigger size with all new fields filled with -1, or bucket
     *         smaller size.
     */
    private int[] resizeBucket(int[] bucket, int sizeDifference) {
        int length = bucket.length;
        int newLength = length + sizeDifference;
        int[] resizedBucket = new int[newLength];
        System.arraycopy(bucket, 0, resizedBucket, 0, sizeDifference < 0 ? newLength : length);
        if (sizeDifference > 0) {
            for (int i = bucket.length; i < bucket.length + sizeDifference; i++) {
                resizedBucket[i] = -1;
            }
        }
        return resizedBucket;
    }

    /**
     * Calculates hash code for provided hash value represented as byte array.
     * 
     * @param hashValue
     *            Sequence of bytes that represents hash value.
     * @return HashCode.
     */
    private int hashCode(byte[] value) {
        int hash = 1;
        for (int i = 0; i < value.length; i++) {
            int unsignedByte = value[i] < 0 ? ((int) (-1 * value[i])) : value[i];
            int primeNumberIndex = i < PRIMES.length ? i : i % PRIMES.length;
            hash += unsignedByte * PRIMES[primeNumberIndex];
        }
        // hash ^= (hash >>> 20) ^ (hash >>> 12);
        // hash = hash ^ (hash >>> 7) ^ (hash >>> 4);
        return hash % numberOfElementsInTable;
    }
    
}