package sharma.pankaj.launchernow.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sharma.pankaj.launchernow.R;
import sharma.pankaj.launchernow.listener.ItemClickListener;
import sharma.pankaj.launchernow.model.HomeModel;

import static sharma.pankaj.launchernow.util.Constants.UNINSTALL;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private Context context;
    private List<HomeModel> list;
    private LayoutInflater inflater;
    private ItemClickListener listener;

    public HomeAdapter(Context context, List<HomeModel> list, ItemClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setGravity(Gravity.CENTER_HORIZONTAL);
        viewParams.setMargins(16, 16, 16, 16);
        view.setLayoutParams(viewParams);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeAdapter.ViewHolder holder, int position) {
        holder.imageView.setImageDrawable(list.get(position).getIcon());
        holder.textView.setText(list.get(position).getName().toString());
        holder.view.setOnClickListener(v -> {
            try {
                Intent launchIntent = context.getPackageManager()
                        .getLaunchIntentForPackage(list.get(position).getPackageName().toString());
                context.startActivity(launchIntent);
            }catch (Exception e){
                e.printStackTrace();
            }

        });

        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setItemOpen(v, position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void filterList(List<HomeModel> filteredList) {
        list = filteredList;
        notifyDataSetChanged();
    }

    private void setItemOpen(View view, int position) {
        PopupMenu menu = new PopupMenu(context, view);
        menu.getMenu().add("App Info");
        menu.getMenu().add("Uninstall");
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getTitle().toString().equalsIgnoreCase("App Info")) {
                    try {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + list.get(position).getPackageName()));
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                        context.startActivity(intent);
                    }
                }else {
                   listener.onItemClick(UNINSTALL, list.get(position).getPackageName().toString(), position);
                }
                return true;
            }
        });
        menu.show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout view;
        public ImageView imageView;
        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = (LinearLayout) itemView;
            imageView = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            params.bottomMargin = 12;
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.getLayoutParams().height = 70;
            imageView.getLayoutParams().width = 70;
            view.addView(imageView);
            textView = new TextView(context);
            LinearLayout.LayoutParams tv = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            textView.setTextSize((float) 12.0);
            textView.setMaxLines(1);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTextColor(context.getResources().getColor(R.color.white));
            textView.setLayoutParams(tv);
            textView.getLayoutParams().height = 23;
            view.addView(textView);
        }
    }
}
