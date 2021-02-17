package sharma.pankaj.launchernow.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import sharma.pankaj.launchernow.MainActivity;
import sharma.pankaj.launchernow.R;
import sharma.pankaj.launchernow.model.HomeModel;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private Context context;
    private List<HomeModel> list;
    private LayoutInflater inflater;

    public HomeAdapter(Context context, List<HomeModel> list) {
        this.context = context;
        this.list = list;
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
            Intent launchIntent = context.getPackageManager()
                    .getLaunchIntentForPackage(list.get(position).getPackageName().toString());
            context.startActivity(launchIntent);
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
