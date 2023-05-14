package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.constants.ErrorMessages;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.User;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    TicketService ticketService;
    @Spy
    TicketPaymentService ticketPaymentService;
    @Spy
    SeatReservationService seatReservationService;

    @BeforeEach
    void setUp() {
        ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
    }

    @Test
    void purchaseTicketsInvalidAccountException_1() {
        User user = null;
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 21);

        InvalidPurchaseException thrown = Assertions.assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(user, ticketTypeRequest);
        }, "InvalidPurchaseException was expected");

        Assertions.assertEquals(ErrorMessages.INVALID_ACCT_ID_ERR_MSG, thrown.getMessage());
    }

    @Test
    void purchaseTicketsInvalidAccountException_2() {
        User user = new User(-1l);
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 21);

        InvalidPurchaseException thrown = Assertions.assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(user, ticketTypeRequest);
        }, "InvalidPurchaseException was expected");

        Assertions.assertEquals(ErrorMessages.INVALID_ACCT_ID_ERR_MSG, thrown.getMessage());
    }


    @Test
    void purchaseTicketsMaxTicketsException() {
        User user = new User(1l);
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 21);

        InvalidPurchaseException thrown = Assertions.assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(user, ticketTypeRequest);
        }, "InvalidPurchaseException was expected");

        Assertions.assertEquals(ErrorMessages.MAX_TICKETS_ERR_MSG, thrown.getMessage());
    }

    @Test
    void purchaseTicketsAdultTicketsNotFoundException() {
        User user = new User(1l);
        TicketTypeRequest ticketTypeRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        InvalidPurchaseException thrown = Assertions.assertThrows(InvalidPurchaseException.class, () -> {
            ticketService.purchaseTickets(user, ticketTypeRequest1, ticketTypeRequest2);
        }, "InvalidPurchaseException was expected");

        Assertions.assertEquals(ErrorMessages.ADULT_TICKETS_NOT_FOUND_ERR_MSG, thrown.getMessage());
    }

    @Test
    void purchaseTicketsCalculateTicketPrice() throws IllegalAccessException, NoSuchFieldException {
        User user = new User(1l);
        TicketTypeRequest ticketTypeRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 5);
        TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5);
        TicketTypeRequest ticketTypeRequest3 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        ticketService.purchaseTickets(user, ticketTypeRequest1, ticketTypeRequest2, ticketTypeRequest3);
        Field finalTicketPriceField = TicketServiceImpl.class.getDeclaredField("finalTicketPrice");
        finalTicketPriceField.setAccessible(true);
        assertEquals(150,finalTicketPriceField.get(ticketService) );
    }

    @Test
    void purchaseTicketsCalculateTotalNoOfTickets() throws IllegalAccessException, NoSuchFieldException {
        User user = new User(1l);
        TicketTypeRequest ticketTypeRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 5);
        TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5);
        TicketTypeRequest ticketTypeRequest3 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        ticketService.purchaseTickets(user, ticketTypeRequest1, ticketTypeRequest2, ticketTypeRequest3);
        Field finalTicketPriceField = TicketServiceImpl.class.getDeclaredField("totalNoOfTickets");
        finalTicketPriceField.setAccessible(true);
        assertEquals(11,finalTicketPriceField.get(ticketService) );
    }
}
