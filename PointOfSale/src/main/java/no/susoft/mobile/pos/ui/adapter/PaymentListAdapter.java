package no.susoft.mobile.pos.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.data.Decimal;
import no.susoft.mobile.pos.data.Payment;
import no.susoft.mobile.pos.error.ErrorReporter;
import no.susoft.mobile.pos.hardware.terminal.CardTerminal;
import no.susoft.mobile.pos.hardware.terminal.CardTerminalFactory;
import no.susoft.mobile.pos.hardware.terminal.TerminalRequest;
import no.susoft.mobile.pos.ui.activity.MainActivity;

public class PaymentListAdapter extends ArrayAdapter<Payment> {

    private ArrayList<Payment> list;

    public PaymentListAdapter(Context context, int textViewResourceId, ArrayList<Payment> objects) {
        super(context, textViewResourceId, objects);
        list = objects;
    }

    private class ViewHolder {

        private TextView paymentType;
        private TextView paymentAmount;
        private TextView deleteLine;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.payments_list, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.paymentType = (TextView) convertView.findViewById(R.id.payment_type_text);
        viewHolder.paymentAmount = (TextView) convertView.findViewById(R.id.payment_type_amount);
        viewHolder.deleteLine = (TextView) convertView.findViewById(R.id.payment_delete_line);

        switch (getItem(position).getType()) {
            case CASH: {
                viewHolder.paymentType.setText(R.string.cash);
                break;
            }
            case CARD: {
                if(getItem(position).getCardName() != null && getItem(position).getCardName().length() > 0) {
                    viewHolder.paymentType.setText(getItem(position).getCardName());
                } else {
                    viewHolder.paymentType.setText(R.string.card);
                }
                break;
            }
            case GIFT_CARD: {
                if(getItem(position).getNumber() != null) {
                    viewHolder.paymentType.setText(MainActivity.getInstance().getString(R.string.gift_card) + " " + MainActivity.getInstance().getString(R.string.number) + " " + getItem(position).getNumber());
				} else {
                    viewHolder.paymentType.setText(R.string.gift_card);
                }
                break;
            }
            case INVOICE: {
                viewHolder.paymentType.setText(R.string.invoice);
                break;
            }

        }

        viewHolder.paymentAmount.setText(getItem(position).getAmount().toString());

        final int i = position;
        viewHolder.deleteLine.setVisibility(View.VISIBLE);
        viewHolder.deleteLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final Payment payment = getItem(i);

                    switch (payment.getType()) {

                        case CARD:

                            final CardTerminal cardTerminal = CardTerminalFactory.getInstance().getCardTerminal(payment.getCardTerminalType());

							if (cardTerminal.isConnected()) {

								switch (cardTerminal.open()) {

									case CardTerminal.SUCCESS:
										// no action so far
										ErrorReporter.INSTANCE.filelog("PaymentListAdapter", "Connected... calling reversal");
										cardTerminal.reversal(new TerminalRequest().setOperID("0000") // todo operatorId ?
																.setTransferType(TerminalRequest.TRANSFER_TYPE_REVERSAL)
                                                .setTotalAmount(payment.getAmount().multiply(Decimal.HUNDRED).toInteger()) // total amount to reverse
                                                .setTotalPurchaseAmount(0));
										break;
									case CardTerminal.FAILURE:
										ErrorReporter.INSTANCE.filelog("PaymentListAdapter", "Failed to connect.");

										ErrorReporter.INSTANCE.filelog("PaymentListAdapter", "No connection to card terminal. Can't process reversal call.");
										Toast.makeText(MainActivity.getInstance(), "No connection to card terminal. Can't process reversal call." + cardTerminal.decode(cardTerminal.getMethodRejectCode()), Toast.LENGTH_LONG).show();
										break;
								}
							} else {
								MainActivity.getInstance().getNumpadPayFragment().deletePayment(payment);
							}

                            break;

                        default:
                            MainActivity.getInstance().getNumpadPayFragment().deletePayment(payment);
                            break;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        convertView.setTag(viewHolder);

        return convertView;
    }

    @Override
    public Payment getItem(int position) {
        return super.getItem(position);
    }

}






