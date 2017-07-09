package no.susoft.mobile.pos.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import no.susoft.mobile.pos.R;
import no.susoft.mobile.pos.Utilities;
import no.susoft.mobile.pos.account.AccountManager;
import no.susoft.mobile.pos.data.QuickLaunchMenuCell;
import no.susoft.mobile.pos.data.QuickLaunchMenuGrid;
import no.susoft.mobile.pos.network.Protocol.ImageType;
import no.susoft.mobile.pos.network.Protocol.OperationCode;
import no.susoft.mobile.pos.network.Protocol.Parameters;
import no.susoft.mobile.pos.network.Protocol.State;
import no.susoft.mobile.pos.network.Request;
import no.susoft.mobile.pos.network.Server;
import no.susoft.mobile.pos.ui.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class QuickLaunchMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int MAX_GRID_ANIMATION_DELAY = 600;
    private static final Interpolator INTERPOLATOR = new DecelerateInterpolator();

    private final Context context;

    private QuickLaunchMenuGrid grid;
    private List<QuickLaunchMenuCell> cells = new ArrayList<>();

    private int cellSize;
    private boolean lockedAnimations = false;
    private long animationStartTime = 0;
    private int lastAnimatedItem = 0;

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
                holder.rlTextHolder.setBackgroundColor(Color.parseColor("#E69E9E9E"));
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
                        if (cell.getProductId() != null && cell.getProductId().length() > 0) {
                            MainActivity.getInstance().getServerCallMethods().loadProductByID(cell.getProductId());
                        } else if (cell.getChildGridId() > 0) {

                        }
                    }
                }
            });

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
                    //					animateGrid(holder);
                }

                @Override
                public void onError() {
                    //					animateGrid(holder);
                }
            });

            //			if (lastAnimatedItem < position)
            //				lastAnimatedItem = position;
        }
    }

    private void animateGrid(QuickLaunchMenuGridViewHolder viewHolder) {
        if (!lockedAnimations) {

            //			if (lastAnimatedItem == viewHolder.getPosition()) {
            //				setLockedAnimations(true);
            //			}

            long animationDelay = animationStartTime + MAX_GRID_ANIMATION_DELAY - System.currentTimeMillis();
            if (animationStartTime == 0) {
                animationDelay = viewHolder.getPosition() * 30 + MAX_GRID_ANIMATION_DELAY;
            } else if (animationDelay < 0) {
                animationDelay = viewHolder.getPosition() * 30;
            } else {
                animationDelay += viewHolder.getPosition() * 30;
            }

            viewHolder.flRoot.setScaleY(0);
            viewHolder.flRoot.setScaleX(0);
            viewHolder.flRoot.animate().scaleY(1).scaleX(1).setDuration(200).setInterpolator(INTERPOLATOR).setStartDelay(animationDelay).start();
        }
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    public QuickLaunchMenuGrid getGrid() {
        return grid;
    }

    public void setLockedAnimations(boolean lockedAnimations) {
        this.lockedAnimations = lockedAnimations;
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
