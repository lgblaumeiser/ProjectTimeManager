/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.datamanager.service;

import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.store.ObjectStore;

/**
 * Provider for Booking Service Implementation
 */
public class BookingServiceProvider {
    public BookingService getBookingService(final ObjectStore<Booking> store) {
        return new BookingService(store);
    }
}
