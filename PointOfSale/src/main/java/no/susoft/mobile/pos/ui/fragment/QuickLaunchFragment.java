package no.susoft.mobile.pos.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.Utilities;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.QuickLaunchMenuCell;
import no.susoft.mobile.pos.data.QuickLaunchMenuGrid;
import no.susoft.mobile.pos.json.JSONFactory;
import no.susoft.mobile.pos.network.Protocol.*;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;
import no.susoft.mobile.pos.ui.activity.util.MainTopBarMenu;

public class QuickLaunchFragment extends Fragment {

    @InjectView(R.id.rvQlmMenuView)
    RecyclerView rvQlmMenuView;
    @InjectView(R.id.browse_input_field)
    EditText inputField;
    private MenuItem overflowMenuItem;

    private QuickLaunchMenuAdapter qlmAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        qlmAdapter = new QuickLaunchMenuAdapter(getActivity().getApplicationContext());
		if (MainActivity.getInstance().isConnected()) {
			new QuickLaunchMenuLoadGridByShopTaskWorker().execute(AccountManager.INSTANCE.getAccount().getShop().getID());
		} else {
			new QuickLaunchMenuOfflineLoadGridByShopTaskWorker().execute(AccountManager.INSTANCE.getAccount().getShop().getID());
		}
        MainActivity.getInstance().setQuickLaunchFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.quick_launch_fragment, container, false);
        ButterKnife.inject(this, view);

        rvQlmMenuView.setAdapter(qlmAdapter);
        if (qlmAdapter.getGrid() != null) {
            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(qlmAdapter.getGrid().getSizeX(), StaggeredGridLayoutManager.VERTICAL);
            rvQlmMenuView.setLayoutManager(layoutManager);
            rvQlmMenuView.setVisibility(View.VISIBLE);
        } else {
            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
            rvQlmMenuView.setLayoutManager(layoutManager);
            rvQlmMenuView.setVisibility(View.INVISIBLE);
        }

        final GestureDetector gesture = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                           float velocityY) {
                        final int SWIPE_MIN_DISTANCE = 120;
                        final int SWIPE_MAX_OFF_PATH = 250;
                        final int SWIPE_THRESHOLD_VELOCITY = 200;
                        try {
                            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                                return false;
                            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                // Log.i(Constants.APP_TAG, "Right to Left");
                                getSwipedFragment("right");
                            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                //Log.i(Constants.APP_TAG, "Left to Right");
                                getSwipedFragment("left");
                            }
                        } catch (Exception e) {
                            // nothing
                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }

        });

        rvQlmMenuView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }

        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void addInputListener() {

    }

    private void getSwipedFragment(String side) {
        if (side.equalsIgnoreCase("left")) {
            MainTopBarMenu.getInstance().toggleScanView();
        } else if (side.equalsIgnoreCase("right")) {
            MainTopBarMenu.getInstance().toggleSearchView();
        }
    }

    public void refreshGridView() {
        if (qlmAdapter.getGrid() == null || qlmAdapter.getGrid().getParentCellId() > 0) {
			if (MainActivity.getInstance().isConnected()) {
				new QuickLaunchMenuLoadGridByShopTaskWorker().execute(AccountManager.INSTANCE.getAccount().getShop().getID());
			} else {
				new QuickLaunchMenuOfflineLoadGridByShopTaskWorker().execute(AccountManager.INSTANCE.getAccount().getShop().getID());
			}
        }
    }

    public void doEnterClick() {
        if (inputFieldHasText()) {
            loadProductFromServer();
        }

        clearInputField();
        inputFieldRequestFocus();
        hideKeyboard();
    }

    private void loadProductFromServer() {
        MainActivity.getInstance().getServerCallMethods().loadProductByBarcode(getInputFieldText());
    }

    private String getInputFieldText() {
        return inputField.getText().toString();
    }

    protected void hideKeyboard() {
        View currentView = MainActivity.getInstance().getCurrentFocus();
        if (currentView != null) {
            InputMethodManager imm = (InputMethodManager) MainActivity.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentView.getWindowToken(), 0);
        }
    }

    private void clearInputField() {
        inputField.setText("");
    }

    public void inputFieldRequestFocus() {
        MainActivity.getInstance().findViewById(R.id.root).requestFocusFromTouch();
        inputField.requestFocusFromTouch();
    }

    private boolean inputFieldHasText() {
        return inputField.getText().toString().length() > 0;
    }

    public void setInputFieldToString(String text) {
        inputField.requestFocus();
        inputField.setText(text);
    }

    /**
     * Worker to load the qlm grid from server and populate the adapter
     */
    public class QuickLaunchMenuLoadGridByShopTaskWorker extends AsyncTask<String, Void, String> {

        QuickLaunchMenuGrid grid;
        String shopId;

        @Override
        protected String doInBackground(String... shop) {
            shopId = shop[0];
			try {
                Request request = Server.INSTANCE.getEncryptedPreparedRequest();
                request.appendState(State.AUTHORIZED);
                request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getToken());
                request.appendOperation(OperationCode.REQUEST_QUICK_LAUNCH_MENU);
                request.appendEntity(SearchEntity.QLM_GRID);
                request.appendParameter(Parameters.SHOP, shop[0]);

                // Send and Receive
                String json = Server.INSTANCE.doGet(request);
                if (json != null) {
                    Gson gson = JSONFactory.INSTANCE.getFactory();
                    JsonParser parser = new JsonParser();
                    JsonObject obj = parser.parse(json).getAsJsonObject();
                    if (obj != null) {
                        grid = gson.fromJson(obj, QuickLaunchMenuGrid.class);
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
            if (grid != null) {
                qlmAdapter.setGrid(grid);
                StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(grid.getSizeX(), StaggeredGridLayoutManager.VERTICAL);
                rvQlmMenuView.setLayoutManager(layoutManager);
                rvQlmMenuView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    }
                });
                rvQlmMenuView.setVisibility(View.VISIBLE);
                rvQlmMenuView.setAdapter(qlmAdapter);
                qlmAdapter.notifyDataSetChanged();
                rvQlmMenuView.setVisibility(View.VISIBLE);
            } else {
				if (MainActivity.getInstance().isConnected()) {
					rvQlmMenuView.setVisibility(View.INVISIBLE);
				} else {
					new QuickLaunchMenuOfflineLoadGridByShopTaskWorker().execute(shopId);
				}
            }
        }
    }

    public class QuickLaunchMenuLoadGridByIdTaskWorker extends AsyncTask<String, Void, String> {

        QuickLaunchMenuGrid grid;
        String id;

        @Override
        protected String doInBackground(String... gridId) {
			id = gridId[0];
            try {
                Request request = Server.INSTANCE.getEncryptedPreparedRequest();
                request.appendState(State.AUTHORIZED);
                request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getToken());
                request.appendOperation(OperationCode.REQUEST_QUICK_LAUNCH_MENU);
                request.appendEntity(SearchEntity.QLM_GRID);
                request.appendParameter(Parameters.ID, gridId[0]);

                // Send and Receive
                String json = Server.INSTANCE.doGet(request);
                if (json != null) {
                    Gson gson = JSONFactory.INSTANCE.getFactory();
                    JsonParser parser = new JsonParser();
                    JsonObject obj = parser.parse(json).getAsJsonObject();
                    if (obj != null) {
                        grid = gson.fromJson(obj, QuickLaunchMenuGrid.class);
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
            if (grid != null) {
                qlmAdapter.setGrid(grid);
                StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(grid.getSizeX(), StaggeredGridLayoutManager.VERTICAL);
                rvQlmMenuView.setLayoutManager(layoutManager);
                rvQlmMenuView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    }
                });
                rvQlmMenuView.setVisibility(View.VISIBLE);
                rvQlmMenuView.setAdapter(qlmAdapter);
                qlmAdapter.notifyDataSetChanged();
                rvQlmMenuView.setVisibility(View.VISIBLE);
            } else {
				if (MainActivity.getInstance().isConnected()) {
					rvQlmMenuView.setVisibility(View.INVISIBLE);
				} else {
					new QuickLaunchMenuOfflineLoadGridByIdTaskWorker().execute(id);
				}
            }
        }
    }

    public class QuickLaunchMenuOfflineLoadGridByShopTaskWorker extends AsyncTask<String, Void, String> {

        QuickLaunchMenuGrid grid;

        @Override
        protected String doInBackground(String... shop) {
            try {

				StringBuilder sql = new StringBuilder();
				sql.append("SELECT ");
				sql.append("	ID, ");
				sql.append("	X_SIZE, ");
				sql.append("	Y_SIZE, ");
				sql.append("	PARENT_CELL_ID ");
				sql.append("FROM ");
				sql.append("	QLM_MENU_GRID ");
				sql.append("WHERE ");
				sql.append("	PARENT_CELL_ID = 0; ");

				SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
				Cursor rs = db.rawQuery(sql.toString(), null);
				if (rs != null && rs.getCount() > 0) {
					rs.moveToFirst();
					grid = new QuickLaunchMenuGrid();
					grid.setId(rs.getLong(rs.getColumnIndex("ID")));
					grid.setSizeX(rs.getInt(rs.getColumnIndex("X_SIZE")));
					grid.setSizeY(rs.getInt(rs.getColumnIndex("Y_SIZE")));
					grid.setParentCellId(rs.getLong(rs.getColumnIndex("PARENT_CELL_ID")));
				}
				if (rs != null) {
					rs.close();
				}

				if (grid != null) {
					sql = new StringBuilder();
					sql.append("SELECT ");
					sql.append("	QLM_CELL.ID AS ID, ");
					sql.append("	PARENT_GRID_ID, ");
					sql.append("	IDX, ");
					sql.append("	TEXT, ");
					sql.append("	PRODUCT_ID, ");
					sql.append("	BARCODE, ");
					sql.append("	QLM_MENU_GRID.ID AS GRID_ID ");
					sql.append("FROM ");
					sql.append("	QLM_CELL ");
					sql.append("LEFT OUTER JOIN ");
					sql.append("	QLM_MENU_GRID ");
					sql.append("ON ");
					sql.append("	QLM_MENU_GRID.PARENT_CELL_ID = QLM_CELL.ID ");
					sql.append("WHERE ");
					sql.append("    PARENT_GRID_ID = ? ");
					sql.append("ORDER BY ");
					sql.append("    IDX ASC;");

					rs = db.rawQuery(sql.toString(), new String[]{"" + grid.getId()});
					if (rs != null && rs.getCount() > 0) {
						ArrayList<QuickLaunchMenuCell> cells = new ArrayList<>();
						rs.moveToFirst();
						do {
							QuickLaunchMenuCell cell = new QuickLaunchMenuCell();
							cell.setId(rs.getLong(rs.getColumnIndex("ID")));
							cell.setParentGridId(rs.getLong(rs.getColumnIndex("PARENT_GRID_ID")));
							cell.setIdx(rs.getInt(rs.getColumnIndex("IDX")));
							cell.setText(rs.getString(rs.getColumnIndex("TEXT")));
							cell.setProductId(rs.getString(rs.getColumnIndex("PRODUCT_ID")));
							cell.setBarcode(rs.getString(rs.getColumnIndex("BARCODE")));
							cell.setChildGridId(rs.getLong(rs.getColumnIndex("GRID_ID")));
							cells.add(cell);

						} while (rs.moveToNext());

						grid.setCells(cells);
					}
					if (rs != null) {
						rs.close();
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
            if (grid != null) {
                qlmAdapter.setGrid(grid);
                StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(grid.getSizeX(), StaggeredGridLayoutManager.VERTICAL);
                rvQlmMenuView.setLayoutManager(layoutManager);
                rvQlmMenuView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    }
                });
                rvQlmMenuView.setVisibility(View.VISIBLE);
                rvQlmMenuView.setAdapter(qlmAdapter);
                qlmAdapter.notifyDataSetChanged();
                rvQlmMenuView.setVisibility(View.VISIBLE);
            } else {
                rvQlmMenuView.setVisibility(View.INVISIBLE);
            }
        }
    }

    public class QuickLaunchMenuOfflineLoadGridByIdTaskWorker extends AsyncTask<String, Void, String> {

        QuickLaunchMenuGrid grid;

        @Override
        protected String doInBackground(String... gridId) {
            try {

				System.out.println("gridId = " + gridId);

				StringBuilder sql = new StringBuilder();
				sql.append("SELECT ");
				sql.append("	ID, ");
				sql.append("	X_SIZE, ");
				sql.append("	Y_SIZE, ");
				sql.append("	PARENT_CELL_ID ");
				sql.append("FROM ");
				sql.append("	QLM_MENU_GRID ");
				sql.append("WHERE ");
				sql.append("	ID = ?; ");

				SQLiteDatabase db = MainActivity.getInstance().getDbHelper().getReadableDatabase();
				Cursor rs = db.rawQuery(sql.toString(), new String[]{gridId[0]});
				if (rs != null && rs.getCount() > 0) {
					rs.moveToFirst();
					grid = new QuickLaunchMenuGrid();
					grid.setId(rs.getLong(rs.getColumnIndex("ID")));
					grid.setSizeX(rs.getInt(rs.getColumnIndex("X_SIZE")));
					grid.setSizeY(rs.getInt(rs.getColumnIndex("Y_SIZE")));
					grid.setParentCellId(rs.getLong(rs.getColumnIndex("PARENT_CELL_ID")));
				}
				if (rs != null) {
					rs.close();
				}

				if (grid != null) {
					sql = new StringBuilder();
					sql.append("SELECT ");
					sql.append("	QLM_CELL.ID AS ID, ");
					sql.append("	PARENT_GRID_ID, ");
					sql.append("	IDX, ");
					sql.append("	TEXT, ");
					sql.append("	PRODUCT_ID, ");
					sql.append("	BARCODE, ");
					sql.append("	QLM_MENU_GRID.ID AS GRID_ID ");
					sql.append("FROM ");
					sql.append("	QLM_CELL ");
					sql.append("LEFT OUTER JOIN ");
					sql.append("	QLM_MENU_GRID ");
					sql.append("ON ");
					sql.append("	QLM_MENU_GRID.PARENT_CELL_ID = QLM_CELL.ID ");
					sql.append("WHERE ");
					sql.append("    PARENT_GRID_ID = ? ");
					sql.append("ORDER BY ");
					sql.append("    IDX ASC;");

					rs = db.rawQuery(sql.toString(), new String[]{"" + grid.getId()});
					if (rs != null && rs.getCount() > 0) {
						ArrayList<QuickLaunchMenuCell> cells = new ArrayList<>();
						rs.moveToFirst();
						do {
							QuickLaunchMenuCell cell = new QuickLaunchMenuCell();
							cell.setId(rs.getLong(rs.getColumnIndex("ID")));
							cell.setParentGridId(rs.getLong(rs.getColumnIndex("PARENT_GRID_ID")));
							cell.setIdx(rs.getInt(rs.getColumnIndex("IDX")));
							cell.setText(rs.getString(rs.getColumnIndex("TEXT")));
							cell.setProductId(rs.getString(rs.getColumnIndex("PRODUCT_ID")));
							cell.setBarcode(rs.getString(rs.getColumnIndex("BARCODE")));
							cell.setChildGridId(rs.getLong(rs.getColumnIndex("GRID_ID")));
							cells.add(cell);

							System.out.println("cell.getText() = " + cell.getText() + " 	ChildGridId = " + cell.getChildGridId());

						} while (rs.moveToNext());

						grid.setCells(cells);
					}
					if (rs != null) {
						rs.close();
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
            if (grid != null) {
                qlmAdapter.setGrid(grid);
                StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(grid.getSizeX(), StaggeredGridLayoutManager.VERTICAL);
                rvQlmMenuView.setLayoutManager(layoutManager);
                rvQlmMenuView.setOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    }
                });
                rvQlmMenuView.setVisibility(View.VISIBLE);
                rvQlmMenuView.setAdapter(qlmAdapter);
                qlmAdapter.notifyDataSetChanged();
                rvQlmMenuView.setVisibility(View.VISIBLE);
            } else {
                rvQlmMenuView.setVisibility(View.INVISIBLE);
            }
        }
    }

    public class QuickLaunchMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final Context context;

        private QuickLaunchMenuGrid grid;
        private List<QuickLaunchMenuCell> cells = new ArrayList<>();
        private int cellSize;

        public QuickLaunchMenuAdapter(Context context) {
            this.context = context;
        }

        public QuickLaunchMenuAdapter(Context context, QuickLaunchMenuGrid grid) {
            this.context = context;
            setGrid(grid);
        }

        public void setGrid(QuickLaunchMenuGrid grid) {
            this.grid = grid;
            if (grid != null && grid.getSizeX() != 0) {
                cellSize = Utilities.getScreenWidth(context) / grid.getSizeX();
                cells = new ArrayList<>();
                if (grid.getCells() != null && !grid.getCells().isEmpty()) {
                    int idx = 0;
                    for (int y = 1; y <= grid.getSizeY(); y++) {
                        for (int x = 1; x <= grid.getSizeX(); x++) {
                            idx++;
                            boolean exists = false;
                            for (QuickLaunchMenuCell gridCell : grid.getCells()) {
                                if (gridCell.getIdx() == idx) {
                                    cells.add(gridCell);
                                    exists = true;
                                }
                            }
                            if (!exists) {
                                QuickLaunchMenuCell cell = new QuickLaunchMenuCell();
                                cell.setId(-1);
                                cell.setText("");
                                cells.add(cell);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.quick_launch_grid, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.height = cellSize;
            layoutParams.width = cellSize;
            layoutParams.setFullSpan(false);
            view.setLayoutParams(layoutParams);
            return new QuickLaunchMenuGridViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            bindGrid((QuickLaunchMenuGridViewHolder) holder, position);
        }

        private void bindGrid(final QuickLaunchMenuGridViewHolder holder, final int position) {
            if (cells != null && cells.size() > position) {

                if (cells.get(position).getText() != null && !cells.get(position).getText().isEmpty()) {
                    if (cells.get(position).getChildGridId() > 0) {
                        holder.rlTextHolder.setBackgroundColor(Color.parseColor("#EA9C51"));
                    } else {
                        holder.rlTextHolder.setBackgroundColor(Color.parseColor("#E69E9E9E"));
                    }
                    holder.tvCellName.setText(cells.get(position).getText());
                } else {
                    holder.rlTextHolder.setBackgroundColor(Color.TRANSPARENT);
                    holder.tvCellName.setText("");
                }

                holder.llGridCell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (cells.get(position) != null) {
                            QuickLaunchMenuCell cell = cells.get(position);
                            if (cell.getChildGridId() > 0) {
								if (MainActivity.getInstance().isConnected()) {
									new QuickLaunchMenuLoadGridByIdTaskWorker().execute("" + cell.getChildGridId());
								} else {
									new QuickLaunchMenuOfflineLoadGridByIdTaskWorker().execute("" + cell.getChildGridId());
								}
                            } else if (cell.getProductId() != null && cell.getProductId().length() > 0) {
                                MainActivity.getInstance().getServerCallMethods().loadProductByID(cell.getProductId());
                            }
                        }
                    }
                });

				if (MainActivity.getInstance().isConnected()) {
					Request request = Server.INSTANCE.getEncryptedPreparedRequest();
					request.appendState(State.AUTHORIZED);
					request.appendParameter(Parameters.TOKEN, AccountManager.INSTANCE.getToken());
					request.appendOperation(OperationCode.REQUEST_IMAGE);
					request.appendParameter(Parameters.TYPE, ImageType.QLM_CELL.ordinal());
					request.appendParameter(Parameters.ID, "" + grid.getId());
					request.appendParameter(Parameters.IMAGE, "" + cells.get(position).getId());

					Picasso.with(context).load(request.get()).resize(cellSize, cellSize).centerCrop().into(holder.ivCell, new Callback() {
						@Override
						public void onSuccess() {
						}

						@Override
						public void onError() {
						}
					});
				}
            }
        }

        @Override
        public int getItemCount() {
            return cells.size();
        }

        public QuickLaunchMenuGrid getGrid() {
            return grid;
        }

        class QuickLaunchMenuGridViewHolder extends RecyclerView.ViewHolder {

            @InjectView(R.id.flRoot)
            FrameLayout flRoot;
            @InjectView(R.id.llGridCell)
            LinearLayout llGridCell;
            @InjectView(R.id.ivCell)
            ImageView ivCell;
            @InjectView(R.id.rlTextHolder)
            RelativeLayout rlTextHolder;
            @InjectView(R.id.tvCellName)
            TextView tvCellName;

            public QuickLaunchMenuGridViewHolder(View view) {
                super(view);
                ButterKnife.inject(this, view);
            }
        }
    }

}
