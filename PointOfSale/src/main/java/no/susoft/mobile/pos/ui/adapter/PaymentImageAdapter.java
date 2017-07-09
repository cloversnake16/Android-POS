package no.susoft.mobile.pos.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import no.susoft.mobile.pos.R;

public class PaymentImageAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	
	public PaymentImageAdapter(Context c) {
		mInflater = LayoutInflater.from(c);
		mContext = c;
	}
	
	// create a new ImageView for each item referenced by the
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {  // if it's not recycled,
			convertView = mInflater.inflate(R.layout.payments_more_dialog_button, null);
//			convertView.setLayoutParams(new GridView.LayoutParams(200, 200));
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.paymentText);
			holder.icon = (ImageView) convertView.findViewById(R.id.paymentImage);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
//		holder.icon.setAdjustViewBounds(true);
//		holder.icon.setScaleType(ScaleType.FIT_CENTER);
//		holder.icon.setPadding(8, 8, 8, 8);
		holder.title.setText(paymentNames[position]);
		holder.icon.setImageResource(paymentThumbIds[position]);
		return convertView;
	}
	
	class ViewHolder {
		
		TextView title;
		ImageView icon;
	}
	
	public int getCount() {
		return paymentThumbIds.length;
	}
	
	public Object getItem(int position) {
		return null;
	}
	
	public long getItemId(int position) {
		return 0;
	}
	
	// references to our images
	private Integer[] paymentThumbIds = {R.drawable.ic_invoice, R.drawable.ic_cheque, R.drawable.ic_bonus, R.drawable.ic_giftcard};
	private String[] paymentNames = {"Invoice", "Cheque", "Bonus", "External Giftcard"};
}