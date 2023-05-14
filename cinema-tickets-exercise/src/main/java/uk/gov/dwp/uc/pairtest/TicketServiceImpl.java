package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.constants.ErrorMessages;
import uk.gov.dwp.uc.pairtest.constants.TicketConstants;
import uk.gov.dwp.uc.pairtest.domain.User;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;


public class TicketServiceImpl implements TicketService {

    private Integer totalNoOfTickets;
    private Integer finalTicketPrice;

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.seatReservationService = seatReservationService;
        this.ticketPaymentService = ticketPaymentService;
    }
    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(User user, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateTicketRequest(user, ticketTypeRequests);
        finalTicketPrice = calculateTicketPrice(ticketTypeRequests);
        ticketPaymentService.makePayment(user.getAccountId(), finalTicketPrice);
        seatReservationService.reserveSeat(user.getAccountId(),totalNoOfTickets);
    }


    private void validateTicketRequest(User user, TicketTypeRequest... ticketTypeRequests) {
        if (user == null || user.getAccountId() < 0) {
            throw new InvalidPurchaseException(ErrorMessages.INVALID_ACCT_ID_ERR_MSG);
        }
        totalNoOfTickets = Arrays.stream(ticketTypeRequests).mapToInt((ticketTypeRequest) -> ticketTypeRequest.getNoOfTickets()).sum();
        if (totalNoOfTickets > TicketConstants.MAX_TICKETS) {
            throw new InvalidPurchaseException(ErrorMessages.MAX_TICKETS_ERR_MSG);
        }
        Long adultTicketsCount = Arrays.stream(ticketTypeRequests).filter(ticketTypeRequest -> ticketTypeRequest.getTicketType() == TicketTypeRequest.Type.ADULT).count();
        if (adultTicketsCount == 0) {
            throw new InvalidPurchaseException(ErrorMessages.ADULT_TICKETS_NOT_FOUND_ERR_MSG);
        }
    }

    private Integer calculateTicketPrice(TicketTypeRequest... ticketTypeRequests) {
        final Integer[] finalTicketPrice = {0};
        Arrays.stream(ticketTypeRequests).forEach(ticketTypeRequest -> {
            if (ticketTypeRequest.getTicketType() == TicketTypeRequest.Type.ADULT) {
                finalTicketPrice[0] = finalTicketPrice[0] + ticketTypeRequest.getNoOfTickets() * TicketConstants.ADULT_TICKET_PRICE;
            } else if (ticketTypeRequest.getTicketType() == TicketTypeRequest.Type.CHILD) {
                finalTicketPrice[0] = finalTicketPrice[0] + ticketTypeRequest.getNoOfTickets() * TicketConstants.CHILD_TICKET_PRICE;
            } else if (ticketTypeRequest.getTicketType() == TicketTypeRequest.Type.INFANT) {
                finalTicketPrice[0] = finalTicketPrice[0] + ticketTypeRequest.getNoOfTickets() * TicketConstants.INFANT_TICKET_PRICE;
            }
        });
        return finalTicketPrice[0];
    }
}
