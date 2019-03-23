/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.store.filesystem;

import static de.lgblaumeiser.ptm.util.Utils.getOnlyFromCollection;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import de.lgblaumeiser.ptm.datamanager.model.Booking;

public class FileStoreBookingTest {
    private static final Long TESTACTID = 105L;
    private static final LocalDate TESTDATE = LocalDate.of(2018, 9, 3);
    private static final LocalTime TESTSTARTTIME = LocalTime.of(8, 15);
    private static final LocalTime TESTENDTIME = LocalTime.of(17, 00);
    private static final String TESTCOMMENT = "TestComment";
    private static final String USERNAME = "UserX";

    private FileStore<Booking> testee;

    private final TestFilesystemAbstraction stubAccess = new TestFilesystemAbstraction();

    @Before
    public void setUp() {
        testee = new FileStore<Booking>(stubAccess, () -> Booking.class);
    }

    private static final Booking testData = Booking
            .newBooking()
            .setActivity(TESTACTID)
            .setBookingday(TESTDATE)
            .setComment(TESTCOMMENT)
            .setEndtime(TESTENDTIME)
            .setStarttime(TESTSTARTTIME)
            .setUser(USERNAME)
            .build();

    @Test
    public void testStore() {
        testee.store(testData);
        assertEquals("booking", getExtension(stubAccess.getStorageFile().getName()));
        assertTrue(stubAccess.getStorageContent().contains("comment"));
        assertTrue(stubAccess.getStorageContent().contains(TESTCOMMENT));
        assertTrue(stubAccess.getStorageContent().contains("activity"));
        assertTrue(stubAccess.getStorageContent().contains(TESTACTID.toString()));
        assertTrue(stubAccess.getStorageContent().contains("id"));
    }

    @Test
    public void testRetrieveAll() {
        testee.store(testData);
        Collection<Booking> foundObjs = testee.retrieveAll();
        assertEquals(1, foundObjs.size());
        Booking foundObj = getOnlyFromCollection(foundObjs);
        assertEquals(testData, foundObj);
    }

    @Test
    public void testRetrieveById() {
        Booking returnedObject = testee.store(testData);
        Long id = returnedObject.getId();
        Booking foundObj = testee.retrieveById(id).get();
        assertEquals(testData, foundObj);
    }

    @Test
    public void testDeleteById() {
        Booking returnedObject = testee.store(testData);
        Long id = returnedObject.getId();
        assertNotNull(stubAccess.getStorageContent());
        assertNotNull(stubAccess.getStorageFile());
        testee.deleteById(id);
        assertNull(stubAccess.getStorageContent());
        assertNull(stubAccess.getStorageFile());
    }
}
