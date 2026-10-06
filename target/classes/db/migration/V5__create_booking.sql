CREATE TABLE seat_inventory (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    show_id UUID NOT NULL REFERENCES shows(id) ON DELETE CASCADE,
    seat_id UUID NOT NULL REFERENCES seats(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    hold_id UUID,
    hold_expires_at TIMESTAMP WITH TIME ZONE,
    booking_id UUID,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE(show_id, seat_id)
);

CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id),
    show_id UUID NOT NULL REFERENCES shows(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
    total_amount DECIMAL(10, 2) NOT NULL,
    seat_count INTEGER NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE,
    confirmed_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    cancellation_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE booking_seats (
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    seat_id UUID NOT NULL REFERENCES seats(id) ON DELETE CASCADE,
    show_id UUID NOT NULL REFERENCES shows(id) ON DELETE CASCADE,
    price DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (booking_id, seat_id)
);

CREATE INDEX idx_seat_inventory_show ON seat_inventory(show_id);
CREATE INDEX idx_seat_inventory_seat ON seat_inventory(seat_id);
CREATE INDEX idx_seat_inventory_status ON seat_inventory(status);
CREATE INDEX idx_seat_inventory_hold ON seat_inventory(hold_id);
CREATE INDEX idx_seat_inventory_show_status ON seat_inventory(show_id, status);
CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_show ON bookings(show_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_idempotency ON bookings(idempotency_key);
CREATE INDEX idx_bookings_created ON bookings(created_at);
CREATE INDEX idx_booking_seats_show ON booking_seats(show_id);
CREATE INDEX idx_booking_seats_seat ON booking_seats(seat_id);