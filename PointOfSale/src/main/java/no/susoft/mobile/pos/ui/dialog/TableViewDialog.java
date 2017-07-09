package no.susoft.mobile.pos.ui.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.SusoftPOSApplication;
import no.susoft.mobile.pos.Utilities;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.Area;
import no.susoft.mobile.pos.data.Table;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol.OperationCode;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Protocol.State;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.adapter.utils.TableItemOnDragListener;
import no.susoft.mobile.pos.ui.adapter.utils.TablePassObject;
import no.susoft.mobile.pos.ui.fragment.utils.Cart;

public class TableViewDialog extends DialogFragment {

    private AreaAdapter areaAdapter;
    private TableViewAdapter tableAdapter;
    Spinner spArea;
    RecyclerView rvTableView;
    SharedPreferences preferences;
    int areaSelectedIndex = 0;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        preferences = SusoftPOSApplication.getContext().getSharedPreferences(TableViewDialog.class.toString(), Context.MODE_PRIVATE);
        new TableViewLoadOfflineAsync().execute(AccountManager.INSTANCE.getAccount().getShop().getID());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = (inflater.inflate(R.layout.table_view_dialog, null));

        builder.setView(view).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        setFullScreenDialog();
        spArea = (Spinner) getDialog().findViewById(R.id.spArea);
        rvTableView = (RecyclerView) getDialog().findViewById(R.id.rvTableView);

        areaSelectedIndex = spArea.getSelectedItemPosition();
        spArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (areaSelectedIndex != i) {
                    Area area = (Area) spArea.getSelectedItem();
                    final SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("AREA", area.getId());
                    editor.commit();
                    areaSelectedIndex = i;
                    new TableViewLoadOfflineAsync().execute(AccountManager.INSTANCE.getAccount().getShop().getID());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }

        });
    }

    private void setFullScreenDialog() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    public void setupAdapters(final ArrayList<Area> areas) {
        if (getActivity() != null && areas != null && !areas.isEmpty()) {

            areaAdapter = new AreaAdapter(getActivity(), 0, areas);
            areaAdapter.setDropDownViewResource(R.layout.area_spinner);
            spArea.setAdapter(areaAdapter);

            Area selectedArea = areas.get(0);
            int defaultAreaId = preferences.getInt("AREA", 0);
            if (defaultAreaId > 0) {
                for (Area area : areas) {
                    if (area.getId() == defaultAreaId) {
                        selectedArea = area;
                        spArea.setSelection(areaAdapter.getPosition(area));
                    }
                }
            }

            tableAdapter = new TableViewAdapter(getActivity().getApplicationContext(), selectedArea.getTables());
            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL);
            rvTableView.setLayoutManager(layoutManager);
            rvTableView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                }
            });
            rvTableView.setVisibility(View.VISIBLE);
            rvTableView.setAdapter(tableAdapter);

            areaAdapter.notifyDataSetChanged();
            tableAdapter.notifyDataSetChanged();
            rvTableView.setVisibility(View.VISIBLE);
        } else {
            rvTableView.setVisibility(View.INVISIBLE);
        }
    }

    public void updateAdapters() {
        areaAdapter.notifyDataSetChanged();
        tableAdapter.notifyDataSetChanged();
    }

    /**
     * Worker to load areas and tables from server and populate the adapter
     */
    public class TableViewLoadAsync extends AsyncTask<String, Void, String> {

        ArrayList<Area> areas = new ArrayList<>();

        @Override
        protected String doInBackground(String... shop) {
            try {
                Request request = Server.INSTANCE.getEncryptedPreparedRequest();
                request.appendState(State.AUTHORIZED);
                request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getToken());
                request.appendOperation(OperationCode.REQUEST_TABLE_FORMATION);
                request.appendParameter(Parameters.SHOP, shop[0]);

                String json = Server.INSTANCE.doGet(request);
                if (json != null) {
                    Gson gson = JSONFactory.INSTANCE.getFactory();
                    JsonParser parser = new JsonParser();
                    JsonArray array = parser.parse(json).getAsJsonArray();
                    if (array != null) {
                        for (JsonElement element : array) {
                            Area area = gson.fromJson(element, Area.class);
                            areas.add(area);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setupAdapters(areas);
        }
    }
    /**
     * Worker to load areas and tables from server and populate the adapter
     */
    public class TableViewLoadOfflineAsync extends AsyncTask<String, Void, String> {

        ArrayList<Area> results = null;

        @Override
        protected String doInBackground(String... shop) {

			try {
				StringBuilder sql = new StringBuilder();
				sql.append("SELECT ");
				sql.append("	SHOP_AREA.ID AS AREAID, ");
				sql.append("	SHOP_AREA.NAME, ");
				sql.append("	SHOP_TABLE.AREAID AS TABLEAREA, ");
				sql.append("	SHOP_TABLE.NUMBER, ");
				sql.append("	SHOP_TABLE.CAPACITY, ");
				sql.append("	COUNT(DISTINCT TMPORDERHEADER.ID) AS ORDERCNT, ");
				sql.append("	SUM(TMPORDERLINE.PRICE * TMPORDERLINE.QTY) AS AMOUNT, ");
				sql.append("	CUSTOMER.FIRSTNAME, ");
				sql.append("	CUSTOMER.LASTNAME ");
				sql.append("FROM ");
				sql.append("	SHOP_AREA, ");
				sql.append("	SHOP_TABLE ");
				sql.append("LEFT OUTER JOIN ");
				sql.append("	TMPORDERHEADER ");
				sql.append("ON ");
				sql.append("	SHOP_TABLE.AREAID = TMPORDERHEADER.AREAID ");
				sql.append("AND SHOP_TABLE.NUMBER = TMPORDERHEADER.TABLEID ");
				sql.append("LEFT OUTER JOIN ");
				sql.append("	TMPORDERLINE ");
				sql.append("ON ");
				sql.append("	TMPORDERHEADER.SHOP = TMPORDERLINE.SHOP ");
				sql.append("AND TMPORDERHEADER.ID = TMPORDERLINE.ORDERID ");
				sql.append("LEFT OUTER JOIN ");
				sql.append("    CUSTOMER ");
				sql.append("ON ");
				sql.append("	CUSTOMER.ID = TMPORDERHEADER.CUSTOMERID ");
				sql.append("WHERE ");
				sql.append("	SHOP_AREA.ID = SHOP_TABLE.AREAID ");
				sql.append("GROUP BY ");
				sql.append("	SHOP_TABLE.AREAID, ");
				sql.append("	SHOP_TABLE.NUMBER ");
				sql.append("ORDER BY ");
				sql.append("	SHOP_TABLE.AREAID ASC, ");
				sql.append("	SHOP_TABLE.NUMBER ASC; ");

				SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getWritableDatabase();
				HashMap<Integer, ArrayList<Table>> tables = new HashMap<>();
				results = new ArrayList<>();

				Cursor rs = db.rawQuery(sql.toString(), null);
				if (rs != null && rs.getCount() > 0) {
					rs.moveToFirst();
					do {
						ArrayList<Table> areaTables = null;
						int areaId = rs.getInt(rs.getColumnIndex("AREAID"));
						if (!tables.containsKey(areaId)) {
							Area area = new Area();
							area.setShopId(AccountManager.INSTANCE.getAccount().getShop().getID());
							area.setId(areaId);
							area.setName(rs.getString(rs.getColumnIndex("NAME")));
							results.add(area);
							areaTables = new ArrayList<>();
						} else {
							areaTables = tables.get(areaId);
						}

						Table table = new Table();
						table.setShopId(AccountManager.INSTANCE.getAccount().getShop().getID());
						table.setAreaId(areaId);
						table.setNumber(rs.getInt(rs.getColumnIndex("NUMBER")));
						table.setCapacity(rs.getInt(rs.getColumnIndex("CAPACITY")));
						table.setOrders(rs.getInt(rs.getColumnIndex("ORDERCNT")));
						table.setAmount(rs.getDouble(rs.getColumnIndex("AMOUNT")));
						if (rs.getString(rs.getColumnIndex("LASTNAME")) != null) {
							table.setCustomerName(rs.getString(rs.getColumnIndex("FIRSTNAME")) + " " + rs.getString(rs.getColumnIndex("LASTNAME")));
						}
						areaTables.add(table);
						tables.put(areaId, areaTables);
					} while (rs.moveToNext());
				}
				if (rs != null) {
					rs.close();
				}

				for (Area area : results) {
					for (Integer areaId : tables.keySet()) {
						if (area.getId() == areaId) {
							area.setTables(tables.get(areaId));
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setupAdapters(results);
        }
    }

    public class AreaAdapter extends ArrayAdapter<Area> {

        public AreaAdapter(Context context, int textViewResourceId, ArrayList<Area> objects) {
            super(context, textViewResourceId, objects);
        }

        private class ViewHolder {

            private TextView tvArea;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.area_spinner, parent, false);
            }

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.tvArea = (TextView) convertView.findViewById(R.id.spArea);
            viewHolder.tvArea.setText(getItem(position).getName());
            convertView.setTag(viewHolder);

            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.area_spinner, parent, false);
            }
            ((TextView) convertView).setText(getItem(position).getName());

            return convertView;
        }

        @Override
        public Area getItem(int position) {
            return super.getItem(position);
        }

    }

    public class TableViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final Context context;
        private List<Table> tables = new ArrayList<>();
        private int cellSize;

        public TableViewAdapter(Context context) {
            this.context = context;
        }

        public TableViewAdapter(Context context, List<Table> tables) {
            this.context = context;
            this.tables = tables;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.table_view_cell, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            cellSize = Utilities.getScreenWidth(context) / 5;
            layoutParams.height = cellSize;
            layoutParams.width = cellSize;
            layoutParams.setFullSpan(false);
            view.setLayoutParams(layoutParams);
            return new TableViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            bindGrid((TableViewHolder) holder, position);
        }

        private void bindGrid(final TableViewHolder holder, final int position) {
            if (tables != null && tables.size() > position) {

                if (tables.get(position).getOrders() > 0) {
                    holder.llHolder.setBackgroundColor(Color.parseColor("#ff33b5e5"));
                    holder.tvNumber.setText(String.valueOf(tables.get(position).getNumber()));
                    holder.tvAmount.setText(String.valueOf(tables.get(position).getAmount()));
                    holder.tvOrdersCount.setText(String.valueOf(tables.get(position).getOrders()));
                    holder.tvCustomerName.setText(tables.get(position).getCustomerName() != null ? tables.get(position).getCustomerName() : "");
                } else {
                    holder.llHolder.setBackgroundColor(Color.parseColor("#ff99cc00"));
                    holder.tvNumber.setText(String.valueOf(String.valueOf(tables.get(position).getNumber())));
                    holder.tvAmount.setText("0,00");
                    holder.tvOrdersCount.setText("0");
                    holder.tvCustomerName.setText("");
                }
	
				holder.llHolder.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (tables.get(position) != null) {
							Table table = tables.get(position);
							if (table.getOrders() == 0) {
								if (!Cart.INSTANCE.hasActiveOrder()) {
									Cart.INSTANCE.resetCart();
								}
								if (Cart.INSTANCE.hasActiveOrder() && Cart.INSTANCE.getOrder().getTable() == 0) {
									Cart.INSTANCE.setSelectedAreaTable(table.getAreaId(), table.getNumber());
								}
							}
							if (Cart.INSTANCE.hasOrdersWithLines()) {
								MainActivity.getInstance().getServerCallMethods().queueLoadOrderByTableAfterPark(table.getAreaId(), table.getNumber());
								MainActivity.getInstance().getServerCallMethods().parkOrders();
							} else {
								MainActivity.getInstance().getServerCallMethods().loadOrdersByTable(table.getAreaId(), table.getNumber());
							}
							dismiss();
						}
					}
				});

                holder.llHolder.setOnDragListener(new TableItemOnDragListener(tables.get(position)));
                holder.llHolder.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        try {
                            Table selectedItem = (Table) (v.getTag());

                            if (selectedItem.getOrders() > 0) {
                                TablePassObject passObj = new TablePassObject(v, selectedItem, (Area) spArea.getSelectedItem());

                                ClipData data = ClipData.newPlainText("", "");
                                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                                v.startDrag(data, shadowBuilder, passObj, 0);
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return true;
                    }
                });

                holder.llHolder.setTag(tables.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return tables.size();
        }

        class TableViewHolder extends RecyclerView.ViewHolder {

            @InjectView(R.id.llHolder)
            LinearLayout llHolder;
            @InjectView(R.id.tvNumber)
            TextView tvNumber;
//            @InjectView(R.id.ivIcon)
//            ImageView ivIcon;
            @InjectView(R.id.tvAmount)
            TextView tvAmount;
            @InjectView(R.id.tvOrdersCount)
            TextView tvOrdersCount;
            @InjectView(R.id.tvCustomerName)
            TextView tvCustomerName;

            public TableViewHolder(View view) {
                super(view);
                ButterKnife.inject(this, view);
            }
        }
    }

}

