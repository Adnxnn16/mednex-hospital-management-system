package com.mednex.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class NotFoundExceptionTest {

    @Test
    void constructor_sets_message_correctly() {
        String message = "Entity not found";
        NotFoundException ex = new NotFoundException(message);
        assertThat(ex.getMessage()).isEqualTo(message);
    }

    @Test
    void getMessage_returns_the_message_passed_in() {
        String message = "Custom error";
        NotFoundException ex = new NotFoundException(message);
        assertThat(ex.getMessage()).isEqualTo(message);
    }
}
