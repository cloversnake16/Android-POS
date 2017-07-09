package no.susoft.mobile.pos.hardware.printer;

import java.util.List;

import no.susoft.mobile.pos.data.Order;
import no.susoft.mobile.pos.data.Prepaid;
import no.susoft.mobile.pos.data.ReceiptPrintType;

public interface Printer {

    void printOrder(Order order, ReceiptPrintType receiptPrintType);

    void printPrepaid(List<Prepaid> giftCards);

    void printPrepaidJoined(Order order, List<Prepaid> giftCards);

    void printReturn(Order order);

    void cutPaper();

    void printLine(String s);
}
